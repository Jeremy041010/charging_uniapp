package cn.tedu.charging.order.controller;


import cn.tedu.charging.order.server.points.WebsocketServerPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MsgController {
    @Autowired
    private WebsocketServerPoint websocketServerPoint;
    @GetMapping("/push/msg")
    public String pushMsg(Integer userId,String msg){
        websocketServerPoint.pushMsg(userId,msg);
        return "堆送消息成功!";
    }
}
