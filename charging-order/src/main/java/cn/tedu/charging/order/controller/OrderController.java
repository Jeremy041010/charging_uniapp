package cn.tedu.charging.order.controller;

import cn.tedu.charging.common.pojo.param.OrderAddParam;
import cn.tedu.charging.common.protocol.JsonResult;
import cn.tedu.charging.order.pojo.po.ChargingBillEndPO;
import cn.tedu.charging.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    //结束订单
    @PostMapping("/order/end")
    public JsonResult<String> endOrder(@RequestBody ChargingBillEndPO endData) {
        //调用业务层处理订单结束逻辑
        orderService.endOrder(endData.getBillId(), endData);
        return JsonResult.ok("订单结束成功");
    }

    //查询结束订单信息
    @PostMapping("/order/end/info")
    public JsonResult<ChargingBillEndPO> getEndOrder(@RequestBody Map<String, String> request) {
        String billId = request.get("billId");
        //调用业务层查询结束订单信息
        ChargingBillEndPO endOrder = orderService.getEndOrder(billId);
        return JsonResult.ok(endOrder);
    }
}