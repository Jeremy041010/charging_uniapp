package cn.tedu.charging.user.service;

import cn.tedu.charging.common.pojo.param.ChargeParam;
import cn.tedu.charging.common.pojo.param.VehicleBindParam;
import cn.tedu.charging.common.pojo.param.VehicleUnbindParam;
import cn.tedu.charging.common.pojo.param.WxLoginParam;
import cn.tedu.charging.common.pojo.vo.BalanceVO;
import cn.tedu.charging.common.pojo.vo.VehicleVO;
import cn.tedu.charging.common.pojo.vo.WxLoginVO;

public interface UserService {
    WxLoginVO wxLogin(WxLoginParam param);

    BalanceVO myBalance(Integer userId);

    void charge(ChargeParam param);

    VehicleVO bindedVehicle(Integer userId);

    void bindVehicle(VehicleBindParam param);

    void unbindVehicle(VehicleUnbindParam param);
}
