package cn.tedu.charging.order.clients;

import cn.tedu.charging.common.protocol.JsonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="charging-user")
public interface UserClient {
    @GetMapping("user/charge/check")
        //扫码下单检查车主是否可用
    JsonResult<Boolean> checkUser(
            @RequestParam("userId") Integer userId,
            @RequestParam("gunId") Integer gunId
    );
}
