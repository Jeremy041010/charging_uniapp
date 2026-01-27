package cn.tedu.charging.user.dao.repository.impl;

import cn.tedu.charging.common.pojo.param.ChargeParam;
import cn.tedu.charging.user.dao.mapper.UserMapper;
import cn.tedu.charging.user.dao.mapper.UserVehicleBindMapper;
import cn.tedu.charging.user.dao.mapper.VehicleMapper;
import cn.tedu.charging.user.dao.repository.UserRepository;
import cn.tedu.charging.user.pojo.po.ChargingUserInfoPO;
import cn.tedu.charging.user.pojo.po.ChargingUserVehicleBindPO;
import cn.tedu.charging.user.pojo.po.ChargingVehiclePO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    @Autowired
    private UserMapper userMapper;//user_info
    @Autowired
    private VehicleMapper vehicleMapper;//vehicle
    @Autowired
    private UserVehicleBindMapper userVehicleBindMapper;//user_vehicle_bind

    public ChargingUserInfoPO getUserByOpenId(String openId) {
        //select * from charging_user_info where wx_open_id=?
        QueryWrapper queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("wx_open_id",openId);
        return userMapper.selectOne(queryWrapper);
    }

    public void saveUser(ChargingUserInfoPO po) {
        userMapper.insert(po);
    }

    public ChargingUserInfoPO getById(Integer userId) {
        return userMapper.selectById(userId);
    }

    public void updateBalanceByUserId(ChargeParam param) {
        //update charging_user_info set balance=balance+? where id=?
        UpdateWrapper updateWrapper=new UpdateWrapper();
        updateWrapper.eq("id",param.getUserId());
        updateWrapper.setSql("balance=balance+"+param.getAmount());
        userMapper.update(null,updateWrapper);
    }

    public ChargingUserVehicleBindPO getBindedVehicle(Integer userId, Integer state) {
        //select * from charging_user_vehicle_bind where user_id=? and state=?
        QueryWrapper<ChargingUserVehicleBindPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("state",state);
        return userVehicleBindMapper.selectOne(queryWrapper);
    }

    public ChargingVehiclePO getVehicleById(Integer vehicleId) {
        //select * from charging_vehicle where id=?
        return vehicleMapper.selectById(vehicleId);
    }

    public ChargingVehiclePO getVehicleByLicense(String license) {
        //select * from charging_vehicle where license=?
        QueryWrapper<ChargingVehiclePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("license",license);
        return vehicleMapper.selectOne(queryWrapper);
    }

    public void saveVehicle(ChargingVehiclePO vehiclePO) {
        //insert into charging_vehicle values(?,?,?,?,?,?,?,?)
        vehicleMapper.insert(vehiclePO);
    }

    public void deleteUserBindVehicle(Integer userId, Integer vehicleId) {
        //delete from charging_user_vehicle_bind where user_id=? and vehicle_id=?
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("vehicle_id",vehicleId);
        userVehicleBindMapper.delete(queryWrapper);
    }

    public void bindUserVehicle(ChargingUserVehicleBindPO bindPo) {
        //insert into  charging_user_vehicle_bind values(?,?,?,?,?,?)
        userVehicleBindMapper.insert(bindPo);
    }

    public ChargingUserInfoPO getUserByNickname(String nickName) {
        //select * from charging_user_info where nick_name=?
        QueryWrapper<ChargingUserInfoPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("nick_name",nickName);
        return userMapper.selectOne(queryWrapper);
    }
}
