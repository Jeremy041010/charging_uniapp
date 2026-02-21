package cn.tedu.charging.order.clients;

import cn.tedu.charging.common.protocol.JsonResult;
import cn.tedu.charging.device.pojo.po.ChargingGunInfoPO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 给订单模块准备的设备调用入口
 */
@FeignClient(name="charging-device")
public interface DeviceClient {
    //调用设备 检查枪 是否允许充电
    @GetMapping("/device/gun/check")
    JsonResult<Boolean> checkGun(@RequestParam("gunId") Integer gunId);
    
    @GetMapping("/device/gun/using")
    JsonResult<Boolean> gunUsing(@RequestParam("gunId") Integer gunId);
    
    // 新增：释放充电枪
    @GetMapping("/device/gun/release")
    JsonResult<Boolean> releaseGun(@RequestParam("gunId") Integer gunId);
    
    // 新增：获取枪信息（用于获取pileId）
    @GetMapping("/device/gun/info/{gunId}")
    JsonResult<ChargingGunInfoPO> getGunInfo(@PathVariable("gunId") Integer gunId);
}