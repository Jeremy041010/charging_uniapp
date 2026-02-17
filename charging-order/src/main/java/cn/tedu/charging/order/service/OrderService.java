package cn.tedu.charging.order.service;

import cn.tedu.charging.common.pojo.param.OrderAddParam;
import cn.tedu.charging.order.pojo.po.ChargingBillEndPO;

public interface OrderService {
    String createOrder(OrderAddParam param);

    void checkOrderStatus(String billId);
    
    // 新增：结束订单
    void endOrder(String billId, ChargingBillEndPO endData);
    
    ChargingBillEndPO getEndOrder(String billId);
}