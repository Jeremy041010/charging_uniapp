package cn.tedu.charging.order.controller;

import cn.tedu.charging.common.pojo.param.OrderAddParam;
import cn.tedu.charging.common.protocol.JsonResult;
import cn.tedu.charging.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;
    //扫码下单
    @PostMapping("/order/create")
    public JsonResult<String> createOrder(@RequestBody OrderAddParam param){
        //调用业务层 接收扫码下单参数返回一个订单的编号,在当前项目业务中,订单编号orderNo billId一个意思
        String billId = orderService.createOrder(param);
        return JsonResult.ok(billId);
    }

}
