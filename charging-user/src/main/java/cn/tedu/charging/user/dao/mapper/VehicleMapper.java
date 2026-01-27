package cn.tedu.charging.user.dao.mapper;

import cn.tedu.charging.user.pojo.po.ChargingVehiclePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对接持久层 操作charging_user_info表格
 * 单表CRUD
 */
@Mapper
public interface VehicleMapper extends BaseMapper<ChargingVehiclePO> {
}
