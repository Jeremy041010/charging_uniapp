package cn.tedu.charging.device.dao.mapper;

import cn.tedu.charging.device.pojo.po.ChargingStationPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作站场表格 增删查改
 */
@Mapper
public interface StationMapper extends BaseMapper<ChargingStationPO> {
}
