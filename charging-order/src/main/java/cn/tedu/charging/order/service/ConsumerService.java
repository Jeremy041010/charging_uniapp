package cn.tedu.charging.order.service;

import cn.tedu.charging.common.pojo.message.CheckResultMessage;
import cn.tedu.charging.common.pojo.message.DelayCheckMessage;
import cn.tedu.charging.common.pojo.message.ProgressMessage;

public interface ConsumerService {

    void handleCheckNoRes(DelayCheckMessage msg);

    void handleCheckResult(CheckResultMessage msg);

    void handleChargingProgress(ProgressMessage msg);
}
