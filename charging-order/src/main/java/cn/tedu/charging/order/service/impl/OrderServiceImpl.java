package cn.tedu.charging.order.service.impl;


import cn.tedu.charging.common.constant.MqttTopicConst;
import cn.tedu.charging.common.exception.OrderBusinessException;
import cn.tedu.charging.common.exception.ServiceException;
import cn.tedu.charging.common.pojo.message.DelayCheckMessage;
import cn.tedu.charging.common.pojo.message.StartCheckMessage;
import cn.tedu.charging.common.pojo.param.OrderAddParam;
import cn.tedu.charging.common.protocol.JsonResult;
import cn.tedu.charging.common.protocol.WebSocketResult;
import cn.tedu.charging.common.utils.CronUtil;
import cn.tedu.charging.common.utils.SnowflakeIdGenerator;

import cn.tedu.charging.common.utils.XxlJobTaskUtil;

import cn.tedu.charging.order.amqp.AmqpDelayProducer;
import cn.tedu.charging.order.clients.CostClient;
import cn.tedu.charging.order.clients.DeviceClient;
import cn.tedu.charging.order.clients.UserClient;
import cn.tedu.charging.order.dao.repository.BillRepository;
import cn.tedu.charging.order.mqtt.MqttProducer;
import cn.tedu.charging.order.pojo.po.ChargingBillEndPO;
import cn.tedu.charging.order.pojo.po.ChargingBillExceptionPO;
import cn.tedu.charging.order.pojo.po.ChargingBillSuccessPO;
import cn.tedu.charging.order.server.points.WebsocketServerPoint;
import cn.tedu.charging.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    @Autowired
    private CostClient costClient;

    @Override
    public String createOrder(OrderAddParam param) {
        try {
            //1. 扫码下单的设备是否可以正常充电 检查后端数据--轻做
            checkGun(param.getGunId());
            //2. 用户身份 车辆关系是否正常 检查用户和车辆--轻做
            checkUser(param.getUserId(), param.getGunId());
            //3. 为整个流程生成一个唯一有序的订单编号
            String billId = generateId();
            //4. 订单和设备进行通信 命令开始充电
            String topic = MqttTopicConst.START_GUN_CHECK_PREFIX + param.getPileId();
            StartCheckMessage message = new StartCheckMessage();
            message.setOrderNo(billId);
            message.setUserId(param.getUserId());
            message.setGunId(param.getGunId());
            mqttProducer.sendDefault(topic, message);
            //5. 订单负责为启动阶段结果 兜底,发送延迟消息
            //5.1组织一个满足消费业务费延迟消息数据
            DelayCheckMessage delayMsg = new DelayCheckMessage();
            delayMsg.setOrderNo(billId);
            delayMsg.setUserId(param.getUserId());
            delayMsg.setGunId(param.getGunId());
            amqpDelayProducer.sendDelay("DELAY_EX", delayMsg, 60000);
            //6. 订单要为充电结束负责,创建定时任务 为异常结束兜底
            //6.1 计算订单检查时间cron表达式 考虑实际业务,订单组织参数调用充电最大时长计算逻辑 得到时间
            //目前只考虑课程因素,等待3分钟
            String cron = CronUtil.delayCron(1000 * 60 * 3);
            //6.2使用工具类发送创建定时任务的请求 cron 执行器名称 订单编号
            XxlJobTaskUtil.createJobTask(cron, "order-executor", billId);
            //7. 扫码下单流程步骤 最后 枪是一定被用户占用了 修改枪状态--轻做
            // 新增：校验修改枪状态接口调用结果
            JsonResult<?> gunUsingResult = deviceClient.gunUsing(param.getGunId());
            if (gunUsingResult.getCode() != 0) {
                throw new ServiceException(5001, "修改充电枪状态失败：" + gunUsingResult.getMessage());
            }
            return billId;
        } catch (ServiceException e) {
            // 新增：捕获自定义业务异常（包含订单专属异常），直接抛出交给全局处理器
            throw e;
        } catch (Exception e) {
            // 新增：捕获所有未知异常，打印日志并包装为订单专属异常
            log.error("创建订单失败，param={}", param, e);
            throw new OrderBusinessException(4003, "创建订单失败，请稍后重试");
        }
    }

    @Override
    public void checkOrderStatus(String billId) {
        try {
            //1.使用仓储层利用billId查询success
            ChargingBillSuccessPO success = billRepository.getSuccessByBillId(billId);
            //判断存在
            if (success != null) {
                log.debug("定时检查订单状态,success订单存在,success:{}", success);
                //判断状态是否==1
                if (success.getBillStatus() != null && success.getBillStatus() == 1) {
                    //说明订单有问题 还是正在充电中
                    log.error("订单异常,依然正在充电中,billId:{}", billId);
                    //2.修改订单状态为3
                    billRepository.updateSuccessStatus(success.getId(), 3);
                    //3.组织一个异常单对象(对象 能补充什么属性就补充什么)
                    ChargingBillExceptionPO exceptionPO = new ChargingBillExceptionPO();
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
                    webSocketResult.setData("抱歉,您的订单:[" + billId + "],充电过程有异常,请结束充电,送您一张优惠券http://8CEFD93f");
                    websocketServerPoint.pushMsg(success.getUserId(), webSocketResult);
                    //TODO 5.通知用户别等了,等不到结果了
                }
            } else {
                log.debug("定时检查订单状态,success订单不存在,billId:{}", billId);
            }
        } catch (Exception e) {
            // 新增：兜底捕获所有异常，打印日志并包装
            log.error("检查订单状态失败，billId={}", billId, e);
            throw new ServiceException(5002, "检查订单状态异常：" + e.getMessage());
        }
    }

    // 结束订单业务方法实现
    @Override
    public void endOrder(String billId, ChargingBillEndPO endData) {
        try {
            log.debug("开始处理订单结束业务, billId={}", billId);

            // 1. 验证订单是否存在且状态为充电中
            ChargingBillSuccessPO success = billRepository.getSuccessByBillId(billId);
            if (success == null) {
                throw new OrderBusinessException(4004, "订单不存在，无法结束订单", billId);
            }

            if (success.getBillStatus() == null || success.getBillStatus() != 1) {
                throw new OrderBusinessException(4005, "订单状态不是充电中，无法结束订单", billId);
            }

            // 2. 更新成功订单状态为已结束（状态2）
            billRepository.updateSuccessStatus(billId, 2);

            // 3. 更新结束订单记录
            ChargingBillEndPO endRecord = new ChargingBillEndPO();
            endRecord.setBillId(billId);

            endRecord.setConsumeAmount(new BigDecimal("15.50"));
            
            billRepository.saveEnd(endRecord);

            // 4. 同步释放充电枪状态为"空闲"
            JsonResult<Boolean> releaseResult = deviceClient.releaseGun(success.getGunId());
            if (releaseResult.getCode() != 0) {
                log.warn("释放充电枪状态失败，但订单已结束，billId={}", billId);
            } else if (!releaseResult.getData()) {
                log.warn("充电枪释放操作未成功，billId={}", billId);
            }

            log.debug("订单结束处理完成, billId={}", billId);
        } catch (Exception e) {
            log.error("处理订单结束业务失败，billId={}", billId, e);
            throw new ServiceException(5019, "处理订单结束业务异常：" + e.getMessage());
        }
    }

    @Override
    public ChargingBillEndPO getEndOrder(String billId) {
        try {
            return billRepository.getEndByBillId(billId);
        } catch (Exception e) {
            log.error("查询结束订单失败，billId={}", billId, e);
            throw new ServiceException(5020, "查询结束订单异常：" + e.getMessage());
        }
    }


    private String generateId() {
        return idGenerator.nextId() + "";
    }

    private void checkUser(Integer userId, Integer gunId) {
        try {
            JsonResult<Boolean> jsonResult = userClient.checkUser(userId, gunId);
            // 新增：校验用户服务调用是否成功
            if (jsonResult.getCode() != 0) {
                throw new ServiceException(5003, "调用用户服务失败：" + jsonResult.getMessage());
            }
            Boolean available = jsonResult.getData();
            if (!available) {
                log.error("当前用户和车辆关系不可用,扫码下单失败,userId={},gunId={}", userId, gunId);
                // 关键改：替换原有RuntimeException为订单专属异常
                throw new OrderBusinessException(4002, "当前用户和车辆关系不可用，无法下单");
            }
            log.debug("当前用户和车辆关系可用,jsonResult:{}", jsonResult);
        } catch (ServiceException e) {
            // 新增：捕获自定义业务异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 新增：兜底捕获未知异常，打印日志并包装
            log.error("校验用户与车辆关系失败，userId={},gunId={}", userId, gunId, e);
            throw new ServiceException(5004, "校验用户与车辆关系异常：" + e.getMessage());
        }
    }


    private void checkGun(Integer gunId) {
        try {
            //检查业务:1 发起调用 2 根据结果判断是否继续流程
            JsonResult<Boolean> jsonResult = deviceClient.checkGun(gunId);
            // 新增：校验设备服务调用是否成功
            if (jsonResult.getCode() != 0) {
                throw new ServiceException(5005, "调用设备服务失败：" + jsonResult.getMessage());
            }
            Boolean available = jsonResult.getData();
            if (!available) {
                log.error("当前枪状态不可用,扫码下单我失败,gunId={}", gunId);
                // 关键改：替换原有RuntimeException为订单专属异常
                throw new OrderBusinessException(4001, "当前充电枪状态不可用，无法下单");
            }
            log.debug("当前枪状态可用,jsonResult:{}", jsonResult);
        } catch (ServiceException e) {
            // 新增：捕获自定义业务异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 新增：兜底捕获未知异常，打印日志并包装
            log.error("校验充电枪状态失败，gunId={}", gunId, e);
            throw new ServiceException(5006, "校验充电枪状态异常：" + e.getMessage());
        }
    }
}
