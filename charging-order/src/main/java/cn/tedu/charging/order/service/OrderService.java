package cn.tedu.charging.order.service;

import cn.tedu.charging.common.pojo.param.OrderAddParam;

public interface OrderService {
    String createOrder(OrderAddParam param);

    void checkOrderStatus(String billId);
}
