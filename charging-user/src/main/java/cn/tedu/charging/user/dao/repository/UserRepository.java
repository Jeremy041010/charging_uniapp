package cn.tedu.charging.user.dao.repository;

import cn.tedu.charging.common.pojo.param.ChargeParam;
import cn.tedu.charging.user.pojo.po.ChargingUserInfoPO;
import cn.tedu.charging.user.pojo.po.ChargingUserVehicleBindPO;
import cn.tedu.charging.user.pojo.po.ChargingVehiclePO;

/**
 * 处理的是车主数据读写
 */
public interface UserRepository {
   ChargingUserInfoPO getUserByOpenId(String openId);

   void saveUser(ChargingUserInfoPO po);

   ChargingUserInfoPO getById(Integer userId);

   void updateBalanceByUserId(ChargeParam param);

   ChargingUserVehicleBindPO getBindedVehicle(Integer userId, Integer state);

   ChargingVehiclePO getVehicleById(Integer vehicleId);

   ChargingVehiclePO getVehicleByLicense(String license) ;

   void saveVehicle(ChargingVehiclePO vehiclePO) ;

   void deleteUserBindVehicle(Integer userId, Integer vehicleId);

   void bindUserVehicle(ChargingUserVehicleBindPO bindPo);

   ChargingUserInfoPO getUserByNickname(String nickName) ;
}
