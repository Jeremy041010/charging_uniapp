package cn.tedu.charging.user.service.impl;

import cn.tedu.charging.common.enums.VehicleUserBindEnum;
import cn.tedu.charging.common.pojo.param.ChargeParam;
import cn.tedu.charging.common.pojo.param.VehicleBindParam;
import cn.tedu.charging.common.pojo.param.VehicleUnbindParam;
import cn.tedu.charging.common.pojo.param.WxLoginParam;
import cn.tedu.charging.common.pojo.vo.BalanceVO;
import cn.tedu.charging.common.pojo.vo.VehicleVO;
import cn.tedu.charging.common.pojo.vo.WxLoginVO;
import cn.tedu.charging.user.dao.repository.UserRepository;
import cn.tedu.charging.user.pojo.dto.WxLoginDTO;
import cn.tedu.charging.user.pojo.po.ChargingUserInfoPO;
import cn.tedu.charging.user.pojo.po.ChargingUserVehicleBindPO;
import cn.tedu.charging.user.pojo.po.ChargingVehiclePO;
import cn.tedu.charging.user.service.UserService;
import cn.tedu.charging.user.utils.WxOpenIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public WxLoginVO wxLogin(WxLoginParam param) {
        log.debug("微信登录,code:{}",param);
        //1.远程调用开放接口,获取openId微信账号标识
        String openId=getOpenId(param);
        //判断
        if (openId!=null&&openId.length()>0){
            log.debug("获取到openId:{}",openId);
            //2.使用openId唯一标识查询当前小程序用户数据
            ChargingUserInfoPO userPO = userRepository.getUserByOpenId(openId);
            //判断userPO是否存在
            if (userPO==null){
                log.debug("用户不存在,登录既注册实现");
                //3.组织一个userPO写入到数据库 补充nickname wxOpenId 别的属性 职业 年龄 性别 时间默认
                userPO=new ChargingUserInfoPO();
                userPO.setNickName("李俊宇");
                userPO.setWxOpenId(openId);
                userRepository.saveUser(userPO);
            }
            //4.使用现在的userPO组织vo范围
            WxLoginVO vo=new WxLoginVO();
            vo.setUserId(userPO.getId());
            vo.setNickName(userPO.getNickName());
            return vo;
        }else{
            log.debug("获取openId失败");
            throw new RuntimeException("获取openId失败");
        }
    }


    private String getOpenId(WxLoginParam param) {
        // 只修改这个私有方法，其他所有代码保持原样！！！
            // ============ 新增：开发调试绕过微信40029限制 开始 ============
            // 写死一个固定的openid，直接返回，跳过微信接口调用，完美解决40029
           /* String fixedOpenId = "o4s1234567890abcdefghijklmnopqrst";
            log.debug("开发调试模式：绕过微信code校验，使用固定openid:{}", fixedOpenId);
            return fixedOpenId;
            // ============ 新增：开发调试绕过微信40029限制 结束 ============
*/
            // ============ 以下是你原来的微信接口调用代码，注释掉即可，不用删除 ============
        //直接调用已封装好发送http请求的工具
        WxLoginDTO wxLoginDTO = WxOpenIdUtils.getWxLoginDTO(param.getCode());
        //如果可以获取openId属性非空 不能获取 errorCode errorMsg非空
        return wxLoginDTO.getOpenid();
    }

    /**
     * 如果企业输出日志有要求,有格式,一般打印日志位置
     * 方法进入
     * 方法返回
     * 业务分支
     * @param userId
     * @return
     */
    @Override
    public BalanceVO myBalance(Integer userId) {
        log.debug("查询用户余额,userId:{}",userId);
        //1.余额数据 在数据库表 字段balance 先查询userPO
        ChargingUserInfoPO po=userRepository.getById(userId);
        //2.生成vo 根据非空数据封装
        BalanceVO vo=null;
        if (po!=null){
            log.debug("查询到用户数据:{}",po);
            vo=new BalanceVO();
            vo.setBalance(po.getBalance());
        }
        log.debug("查询用户余额,userId:{},查到结果:{}",userId,vo==null?"null":vo);
        return vo;
    }

    @Override
    public void charge(ChargeParam param) {
        //1.严谨的思路 判断用户在不在 不在抛异常
        //调用仓储层 执行更新
        userRepository.updateBalanceByUserId(param);
    }

    @Override
    public VehicleVO bindedVehicle(Integer userId) {
        //1.使用userId,以及状态state=1 查询查询用户绑定车辆
        //对于状态 状态码 字典类型 变动不大的数据 可以放到枚举和常量
        //星期 月 地市 非常适合做枚举 但是数据量一旦大了,就应该放到字典表
        ChargingUserVehicleBindPO po=
                userRepository.getBindedVehicle(userId, VehicleUserBindEnum.BINDED.getState());
        //2.判断是否有数据 判断数据是否合法 pos.size()>1不合法
        //2.1空 返回空
        VehicleVO vo=null;
        if (po==null){
            log.debug("查询到的用户正在绑定车辆为空");
        }else {
            log.debug("查询到用户正在绑定车辆:{}",po);
            //获取车辆详情
            Integer vehicleId = po.getVehicleId();
            ChargingVehiclePO vehiclePO=userRepository.getVehicleById(vehicleId);
            //封装vo返回
            vo=new VehicleVO();
            BeanUtils.copyProperties(vehiclePO,vo);
        }
        return vo;
    }

    @Override
    public void bindVehicle(VehicleBindParam param) {
        //1.查询该用户是否存在正在使用绑定的车辆
        ChargingUserVehicleBindPO vehicle= userRepository.getBindedVehicle(
                param.getUserId(),
                VehicleUserBindEnum.BINDED.getState());
        //2.判断非空
        if (vehicle!=null){
            throw new RuntimeException("该用户绑定车辆,请先解绑");
        }
        //3.验证车辆相关信息是否正确
        checkVehicleInfo(param);
        //4.使用牌子license查询vehicle中是否存在车辆信息
        ChargingVehiclePO vehiclePO=userRepository.getVehicleByLicense(param.getLicense());
        if(vehiclePO==null){
            //说明从来没有人录入过这个数据
            vehiclePO=new ChargingVehiclePO();
            //四个入参 license brand vin model 时间
            BeanUtils.copyProperties(param,vehiclePO);
            vehiclePO.setCreateTime(new Date());
            vehiclePO.setUpdateTime(new Date());
            userRepository.saveVehicle(vehiclePO);
        }
        //5.生成绑定 有用户 有车辆 只负责绑定 user_id vehicle_id state=1
        //保证如果用户对车辆解绑 state=0 重新绑定 是相同的user_id vehicle_id state=1 不出现重复数据
        //先删除 绑定数据 不一定能删除数据 在绑定
        userRepository.deleteUserBindVehicle(param.getUserId(),vehiclePO.getId());
        ChargingUserVehicleBindPO bindPo=new ChargingUserVehicleBindPO();
        bindPo.setUserId(param.getUserId());
        bindPo.setVehicleId(vehiclePO.getId());
        bindPo.setState(VehicleUserBindEnum.BINDED.getState());
        userRepository.bindUserVehicle(bindPo);
    }

    @Override
    public void unbindVehicle(VehicleUnbindParam param) {
        //1.检查该用户是否绑定了车辆
        ChargingUserVehicleBindPO bindedVehicle = userRepository.getBindedVehicle(param.getUserId(), VehicleUserBindEnum.BINDED.getState());
        //判断 空且id等于 入参id
        if (bindedVehicle != null && bindedVehicle.getVehicleId() == param.getVehicleId()) {
            userRepository.deleteUserBindVehicle(param.getUserId(), param.getVehicleId());
        } else {
            throw new RuntimeException("该用户未绑定此车辆");
        }
    }
    private void checkVehicleInfo(VehicleBindParam param) {
        //调用相关单位接口 如果数据信息有错误 异常结束 拒绝向后流转业务
    }
}
