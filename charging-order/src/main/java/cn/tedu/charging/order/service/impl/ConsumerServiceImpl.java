package cn.tedu.charging.order.service.impl;

import cn.tedu.charging.common.exception.ServiceException;
import cn.tedu.charging.common.pojo.message.CheckResultMessage;
import cn.tedu.charging.common.pojo.message.DelayCheckMessage;
import cn.tedu.charging.common.pojo.message.ProgressData;
import cn.tedu.charging.common.pojo.message.ProgressMessage;
import cn.tedu.charging.common.pojo.param.ProgressCostParam;
import cn.tedu.charging.common.pojo.vo.ProgressCostVO;
import cn.tedu.charging.common.protocol.JsonResult;
import cn.tedu.charging.common.protocol.WebSocketResult;
import cn.tedu.charging.common.utils.SnowflakeIdGenerator;
import cn.tedu.charging.common.utils.TimeConverterUtil;
import cn.tedu.charging.order.clients.CostClient;
import cn.tedu.charging.order.dao.repository.BillRepository;
import cn.tedu.charging.order.dao.repository.ProcessEsRepository;
import cn.tedu.charging.order.pojo.po.ChargingBillFailPO;
import cn.tedu.charging.order.pojo.po.ChargingBillSuccessPO;
import cn.tedu.charging.order.pojo.po.ChargingProgressEsPO;
import cn.tedu.charging.order.server.points.WebsocketServerPoint;
import cn.tedu.charging.order.service.ConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class ConsumerServiceImpl implements ConsumerService {
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private WebsocketServerPoint websocketServerPoint;
    @Autowired
    private CostClient costClient;
    @Autowired
    private SnowflakeIdGenerator generator;
    @Autowired
    private ProcessEsRepository processEsRepository;

    @Override
    public void handleCheckNoRes(DelayCheckMessage msg) {
        try {
            //1.查询成功订单的count
            Long successCont = billRepository.countSuccess(msg.getOrderNo());
            //判断
            if (successCont == 0) {
                log.debug("当前订单没有成功订单,继续检查,订单编号:{}", msg.getOrderNo());
                //2.查询失败订单
                Long failCount = billRepository.countFail(msg.getOrderNo());
                //判断
                if (failCount == 0) {
                    log.debug("当前订单没有失败订单,设备无响应,订单编号:{}", msg.getOrderNo());
                    //3.组织失败对象写入
                    //1.组织失败订单对象
                    ChargingBillFailPO failPO = new ChargingBillFailPO();
                    //1.1补充消息数据
                    failPO.setFailDesc("设备自检无响应");
                    failPO.setBillId(msg.getOrderNo());
                    failPO.setUserId(msg.getUserId());
                    failPO.setGunId(msg.getGunId());
                    //1.2补充业务数据
                    failPO.setCreateTime(new Date());
                    failPO.setUpdateTime(new Date());
                    failPO.setDeleted(0);
                    billRepository.saveFail(failPO);
                    //UNDO4.通知设备服务详细检查设备
                    //5.通知用户 别登录
                    WebSocketResult<String> webSocketResult = new WebSocketResult<>();
                    webSocketResult.setState(1);
                    webSocketResult.setMessage("订单消息");
                    webSocketResult.setData("抱歉,久等了.您的订单:[" + msg.getOrderNo() + "]启动失败,请换枪重试,送您一张优惠券http://3dT8dNG");
                    websocketServerPoint.pushMsg(msg.getUserId(), webSocketResult);
                }
            }
        } catch (Exception e){
            log.error("处理设备无响应延迟检查消息失败，msg={}", msg, e);
            throw new ServiceException(5012, "处理设备无响应延迟检查消息异常：" + e.getMessage());
        }
    }

    @Override
    public void handleCheckResult(CheckResultMessage msg) {
        try {
            log.debug("处理自检结果逻辑");
            //根据推送消息的接口 返回的是WebSocketResult
            WebSocketResult<String> msgResult = new WebSocketResult<>();
            //不管成功失败都是订单消息
            msgResult.setState(1);
            msgResult.setMessage("订单消息");
            //1.判断自检设备结果,需要消息中result属性
            Boolean result = msg.getResult();
            if (result) {
                log.debug("自检成功,订单编号:{}", msg.getOrderNo());
                //2.新增成功
                //读取当前订单编号成功订单个数
                long count = billRepository.countSuccess(msg.getOrderNo());
                if (count > 0) {
                    log.debug("当前成功订单多次执行消费");
                    return;
                }
                saveSuccessBill(msg);
                //组织成功消息
                msgResult.setData("您的订单:[" + msg.getOrderNo() + "]已启动完毕,马上为您的爱车充电");
            } else {
                log.debug("自检失败,订单编号:{}", msg.getOrderNo());
                //3.新增失败
                long count = billRepository.countFail(msg.getOrderNo());
                if (count > 0) {
                    log.debug("当前失败订单多次执行消费");
                    return;
                }
                saveFailBill(msg);
                //UNDO 4.通知设备 再次检查是否故障
                //组织失败消息
                msgResult.setData("您的订单:[" + msg.getOrderNo() + "]启动失败,请换枪重试,送您一张优惠价");
            }
            //5.消息通知用户
            websocketServerPoint.pushMsg(msg.getUserId(), msgResult);
        } catch (Exception e){
            log.error("处理设备自检结果消息失败，msg={}", msg, e);
            throw new ServiceException(5013, "处理设备自检结果消息异常：" + e.getMessage());
        }
    }
    @Override
    public void handleChargingProgress(ProgressMessage msg) {
        try {
            //1.轻做业务,安全检查
            saftyCheck(msg);
            //2.调用一下计价中心计算价格
            ProgressCostVO costVO = calculateCost(msg);
            //3.组织数据存入es
            persistChargingProgress(msg, costVO);
            //TODO 4.组织消息推送给前端
            sendProgress2User(costVO, msg);
            if (msg.getIsFull()) {
                log.debug("订单充电结束,按照充满结束处理");
                //5.更新订单状态为2
                billRepository.updateSuccessStatus(msg.getOrderNo(), 2);
                //UNDO 通知用户走人 通知设备断电停止
            }
        } catch (Exception e){
            log.error("处理充电进度消息失败，msg={}", msg, e);
            throw new ServiceException(5014, "处理充电进度消息异常：" + e.getMessage());
        }
    }

    private ProgressCostVO calculateCost(ProgressMessage msg) {
        try {
            //1.根据订单调用计价接口组织入参
            ProgressCostParam param = new ProgressCostParam();
            BeanUtils.copyProperties(msg, param);
            //2.调用接口
            JsonResult<ProgressCostVO> result = costClient.calculateCost(param);
            //3.返回vo
            if (result.getCode() != 0){
                throw new ServiceException(5007, "调用计价中心失败：" + result.getMessage());
            }

            return result.getData();
        } catch (ServiceException e){
            throw e;
        } catch (Exception e){
            log.error("计算充电费用失败，msg={}", msg, e);
            throw new ServiceException(5008, "计算充电费用异常：" + e.getMessage());
        }
    }

    private void sendProgress2User(ProgressCostVO costVO,ProgressMessage msg) {
        WebSocketResult<ProgressData> result=new WebSocketResult<>();
        result.setState(3);//只有stateu=3 前端小程序才会按照展示充电详情去解析数据 否则会弹窗
        result.setMessage("充电详情");
        //1.组织一个progressData数据放到WebsocketResult里 state=3 data=progressData
        ProgressData data=new ProgressData();
        //1.1计价中心vo的3个属性
        data.setTotalCost(costVO.getTotalCost());
        data.setOneElectricityCost(costVO.getPowerFee());
        data.setChargingCapacity(costVO.getChargingCapacity());
        //1.2充电总度数
        data.setTotalCapacity(msg.getTotalCapacity());
        //1.3时间 使用充电总毫秒数 计算小时 分钟 秒 有工具
        data.setHours(TimeConverterUtil.getHour(msg.getTotalTime()).intValue());
        data.setMinutes(TimeConverterUtil.getMinute(msg.getTotalTime()).intValue());
        data.setSeconds(TimeConverterUtil.getSecond(msg.getTotalTime()).intValue());
        result.setData(data);
        //2.利用当前客户端连接的端点类 将消息推送给用户展示
        websocketServerPoint.pushMsg(msg.getUserId(),result);
    }

    private void persistChargingProgress(ProgressMessage msg,ProgressCostVO vo) {
        try {
            //1.组织一条充电进度数据
            ChargingProgressEsPO po = new ChargingProgressEsPO();
            //1.1po 的id 是业务计算的
            po.setId(generator.nextId() + "");
            //1.2从消息中拷贝属性
            BeanUtils.copyProperties(msg, po);
            //1.3价格计算结果
            po.setChargingCapacity(vo.getChargingCapacity());
            po.setTotalCost(vo.getTotalCost());
            //2.调用es接口新增写入索引中
            processEsRepository.save(po);
        } catch (Exception e) {
            log.error("保存充电进度到ES失败，msg={}", msg, e);
            throw new ServiceException(5009, "保存充电进度异常：" + e.getMessage());
        }
    }



    private void saftyCheck(ProgressMessage msg) {
        log.debug("充电进度数据同步,安全检查结果正常");
    }

    private void saveFailBill(CheckResultMessage msg) {
        //1.组织失败订单对象
        ChargingBillFailPO fail=new ChargingBillFailPO();
        //1.1补充消息数据
        fail.setFailDesc(msg.getFailDesc());
        fail.setBillId(msg.getOrderNo());
        fail.setUserId(msg.getUserId());
        fail.setGunId(msg.getGunId());
        //1.2补充业务数据
        fail.setCreateTime(new Date());
        fail.setUpdateTime(new Date());
        fail.setDeleted(0);
        //2.调用新增
        billRepository.saveFail(fail);
    }

    private void saveSuccessBill(CheckResultMessage msg) {
        //1.组织successPO
        ChargingBillSuccessPO success=new ChargingBillSuccessPO();
        //1.1补充消息内容数据
        success.setBillId(msg.getOrderNo());
        success.setUserId(msg.getUserId());
        success.setGunId(msg.getGunId());
        //充电时长 电价 服务费 充电度数 车辆id 支付金额 支付时间 支付渠道等都不需要在启动阶段补充
        //1.2再补充业务数据 创建修改时间 订单状态
        success.setBillStatus(1);//1 正在充电 2充电结束 3异常充电 4欠费
        success.setCreateTime(new Date());
        success.setUpdateTime(new Date());
        success.setChargingStartTime(new Date());
        success.setDeleted(0);
        //2.调用仓储层新增 最底层 insert
        billRepository.saveSuccess(success);
    }
}
