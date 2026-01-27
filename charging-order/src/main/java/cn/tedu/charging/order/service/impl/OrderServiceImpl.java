package cn.tedu.charging.order.service.impl;


import cn.tedu.charging.common.constant.MqttTopicConst;
import cn.tedu.charging.common.pojo.message.DelayCheckMessage;
import cn.tedu.charging.common.pojo.message.StartCheckMessage;
import cn.tedu.charging.common.pojo.param.OrderAddParam;
import cn.tedu.charging.common.protocol.JsonResult;
import cn.tedu.charging.common.protocol.WebSocketResult;
import cn.tedu.charging.common.utils.CronUtil;
import cn.tedu.charging.common.utils.SnowflakeIdGenerator;

import cn.tedu.charging.common.utils.XxlJobTaskUtil;

import cn.tedu.charging.order.amqp.AmqpDelayProducer;
import cn.tedu.charging.order.clients.DeviceClient;
import cn.tedu.charging.order.clients.UserClient;
import cn.tedu.charging.order.dao.repository.BillRepository;
import cn.tedu.charging.order.mqtt.MqttProducer;
import cn.tedu.charging.order.pojo.po.ChargingBillExceptionPO;
import cn.tedu.charging.order.pojo.po.ChargingBillSuccessPO;
import cn.tedu.charging.order.server.points.WebsocketServerPoint;
import cn.tedu.charging.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private SnowflakeIdGenerator idGenerator;
    @Autowired
    private MqttProducer mqttProducer;
    @Autowired
    private AmqpDelayProducer amqpDelayProducer;
    @Autowired
    private DeviceClient deviceClient;
    @Autowired
    private UserClient userClient;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private WebsocketServerPoint websocketServerPoint;
    @Override
    public String createOrder(OrderAddParam param) {
        //1. 扫码下单的设备是否可以正常充电 设备检查后端数据--轻做
        checkGun(param.getGunId());
        //2. 用户身份 车辆关系是否正常 检查用户和车辆--轻做
        checkUser(param.getUserId(),param.getGunId());
        //3. 为整个流程生成一个唯一有序的订单编号
        String billId= generateId();
        //4. 订单和设备进行通信 命令开始充电
        String topic= MqttTopicConst.START_GUN_CHECK_PREFIX + param.getPileId();
        StartCheckMessage message=new StartCheckMessage();
        message.setOrderNo(billId);
        message.setUserId(param.getUserId());
        message.setGunId(param.getGunId());
        mqttProducer.sendDefault(topic,message);
        //5. 订单负责为启动阶段结果 兜底,发送延迟消息
        //5.1组织一个满足消费业务费延迟消息数据
        DelayCheckMessage delayMsg = new DelayCheckMessage();
        delayMsg.setOrderNo(billId);
        delayMsg.setUserId(param.getUserId());
        delayMsg.setGunId(param.getGunId());
        amqpDelayProducer.sendDelay("DELAY_EX",delayMsg,60000);
        //6. 订单要为充电结束负责,创建定时任务 为异常结束兜底
        //6.1 计算订单检查时间cron表达式 考虑实际业务,订单组织参数调用充电最大时长计算逻辑 得到时间
        //目前只考虑课程因素,等待3分钟
        String cron = CronUtil.delayCron(1000*60*3);
        //6.2使用工具类发送创建定时任务的请求 cron 执行器名称 订单编号
        XxlJobTaskUtil.createJobTask(cron,"order-executor",billId);
        //7. 扫码下单流程步骤 最后 枪是一定被用户占用了 修改枪状态--轻做
        deviceClient.gunUsing(param.getGunId());
        return billId;

    }

    @Override
    public void checkOrderStatus(String billId) {
        //1.使用仓储层利用billId查询success
        ChargingBillSuccessPO success=billRepository.getSuccessByBillId(billId);
        //判断存在
        if(success!=null){
            log.debug("定时检查订单状态,success订单存在,success:{}",success);
            //判断状态是否==1
            if (success.getBillStatus()!=null&&success.getBillStatus()==1){
                //说明订单有问题 还是正在充电中
                log.error("订单异常,依然正在充电中,billId:{}",billId);
                //2.修改订单状态为3
                billRepository.updateSuccessStatus(success.getId(),3);
                //3.组织一个异常单对象(对象 能补充什么属性就补充什么)
                ChargingBillExceptionPO exceptionPO=new ChargingBillExceptionPO();
                //除了设备的电压 电流 温度以外
                exceptionPO.setBillId(billId);
                exceptionPO.setBillStarttime(success.getChargingStartTime());
                exceptionPO.setCreateTime(new Date());
                exceptionPO.setDeleted(0);
                billRepository.saveException(exceptionPO);
                //4.通知设备 检查这个枪桩是否有问题 该维修维修
                WebSocketResult<String> webSocketResult = new WebSocketResult<>();
                webSocketResult.setState(1);
                webSocketResult.setMessage("订单信息");
                webSocketResult.setData("抱歉,您的订单:["+billId+"],充电过程有异常,请结束充电,送您一张优惠券http://8CEFD93f");
                websocketServerPoint.pushMsg(success.getUserId(),webSocketResult);
                //TODO 5.通知用户别等了,等不到结果了
            }
        }else{
            log.debug("定时检查订单状态,success订单不存在,billId:{}",billId);
        }
    }

    private String generateId() {
        return idGenerator.nextId()+"";
    }

    private void checkUser(Integer userId, Integer gunId) {
        JsonResult<Boolean> jsonResult = userClient.checkUser(userId, gunId);
        Boolean available = jsonResult.getData();
        if (!available){
            log.error("当前用户和车辆关系不可用,扫码下单失败,userId={},gunId={}",userId,gunId);
            throw new RuntimeException("当前用户和车辆关系不可用,扫码下单失败");
        }
        log.debug("当前用户和车辆关系可用,jsonResult:{}",jsonResult);
    }


    private void checkGun(Integer gunId) {
        //检查业务:1 发起调用 2 根据结果判断是否继续流程
        JsonResult<Boolean> jsonResult = deviceClient.checkGun(gunId);
        Boolean availabale = jsonResult.getData();
        if (!availabale){
            log.error("当前枪状态不可用,扫码下单失败,gunId={}",gunId);
            throw new RuntimeException("当前枪状态不可用,扫码下单失败");
        }
        log.debug("当前枪状态可用,jsonResult:{}",jsonResult);
    }
}
