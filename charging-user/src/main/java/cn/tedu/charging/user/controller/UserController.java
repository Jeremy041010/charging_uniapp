package cn.tedu.charging.user.controller;

import cn.tedu.charging.common.pojo.param.ChargeParam;
import cn.tedu.charging.common.pojo.param.VehicleBindParam;
import cn.tedu.charging.common.pojo.param.VehicleUnbindParam;
import cn.tedu.charging.common.pojo.param.WxLoginParam;
import cn.tedu.charging.common.pojo.vo.BalanceVO;
import cn.tedu.charging.common.pojo.vo.VehicleVO;
import cn.tedu.charging.common.pojo.vo.WxLoginVO;
import cn.tedu.charging.common.protocol.JsonResult;
import cn.tedu.charging.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 定义所有和车主有关的接口 6
 * 登录
 * 查询余额
 * 充值
 * 查询车辆绑定
 * 绑定 解绑
 */
@RestController
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/sms/code/verify")
    public JsonResult<String> verifyCode(String phone){
        //调用业务层实现验证码的业务 TODO
        return JsonResult.ok(null);
    }
    @PostMapping("/sms/send")
    public JsonResult<String> sendSms(String phone){
        //调用业务层实现发送短信的业务 TODO
        return JsonResult.ok(null);
    }
    //小程序用户微信登录
    @PostMapping("/user/wx/login")
    public JsonResult<WxLoginVO> wxLogin(@RequestBody WxLoginParam param){
        WxLoginVO wxLoginVO = userService.wxLogin(param);
        return JsonResult.ok(wxLoginVO);
    }
    //查询余额
    @GetMapping("/user/balance/{userId}")
    public JsonResult<BalanceVO> myBalance(@PathVariable Integer userId){
        //控制层要什么 业务层就返回什么
        BalanceVO balanceVO = userService.myBalance(userId);
        return JsonResult.ok(balanceVO);
    }
    //给车主充值
    @PostMapping("/user/charge")
    public JsonResult charge(@RequestBody ChargeParam param){
        userService.charge(param);
        return JsonResult.ok(null);
    }
    //查询该用户正在使用绑定的车辆
    @GetMapping("/user/vehicle/binded/{userId}")
    public JsonResult<VehicleVO> bindedVehicle(@PathVariable Integer userId){
        VehicleVO vehicleVO = userService.bindedVehicle(userId);
        return JsonResult.ok(vehicleVO);
    }
    //扫码下单检查车主是否可用
    @GetMapping("/user/charge/check")
    JsonResult<Boolean> checkUser(
            @RequestParam("userId") Integer userId,
            @RequestParam("gunId") Integer gunId){
        return JsonResult.ok(true);
    }
    //用户绑定车辆
    @PostMapping("/user/vehicle/bind")
    public JsonResult bindVehicle(@RequestBody VehicleBindParam param){
        userService.bindVehicle(param);
        return JsonResult.ok(null);
    }
    //解除用户绑定车辆
    @GetMapping("/user/vehicle/unbind")
    public JsonResult unbindVehicle(VehicleUnbindParam param){
        userService.unbindVehicle(param);
        return JsonResult.ok(true);
    }
}
