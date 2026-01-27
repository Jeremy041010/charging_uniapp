package cn.tedu.charging.device.controller;

import cn.tedu.charging.common.pojo.query.NearStationsQuery;
import cn.tedu.charging.common.pojo.vo.StationDetailVO;
import cn.tedu.charging.common.pojo.vo.StationInfoVO;
import cn.tedu.charging.common.protocol.JsonResult;
import cn.tedu.charging.device.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.UserConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备相关http入口 业务包含
 * operater
 * station
 * pile
 * gun
 * author: xiaoxw
 * date: 2025/8/29
 * version: 1.0
 */
@RestController
@Slf4j
public class DeviceController {
    @Autowired
    private DeviceService deviceService;

    /**
     * 查询附近的充电站
     * @param query 充电站查询参数 经纬度 半径
     * @return 充电站列表
     */
    @GetMapping("/device/station/near")
    public JsonResult<List<StationInfoVO>> nearStations(NearStationsQuery query){
        List<StationInfoVO> vos =  deviceService.nearStations(query);
        return JsonResult.ok(vos);
    }
    //查询某个充电站详情包括站场信息以及站场关联的枪数据
    @GetMapping("/device/station/detail/{stationId}")
    public JsonResult<StationDetailVO> detailStation(@PathVariable Integer stationId){
        return JsonResult.ok( deviceService.detailStation(stationId));
    }
    //订单调用设备检查枪是否可用
    @GetMapping("/device/gun/check")
    //订单调用设备检查枪是否可用
    public JsonResult<Boolean> checkGun(@RequestParam("gunId") Integer gunId){
        //1. 如果正常实现这个接口 用gunId 检查是否枪非空闲
        //2. 轻做 业务简化 直接返回true 每次下单都不会因为枪的实际状态影响下单流程
        return JsonResult.ok(true);
    }

    //修改枪状态的方法
    @PostMapping("/device/gun/error")
    public JsonResult<Boolean> updateGunStatus(
            @RequestParam("gunId")Integer gunId){
        //TODO
        return JsonResult.ok();
    }
    @GetMapping("/device/gun/using")
   public JsonResult<Boolean> gunUsing(@RequestParam("gunId") Integer gunId){
        deviceService.updateGunStatus(gunId,1);
        return JsonResult.ok(true);
    }
}
