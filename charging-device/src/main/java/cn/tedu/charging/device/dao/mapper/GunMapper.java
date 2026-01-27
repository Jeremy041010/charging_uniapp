package cn.tedu.charging.device.dao.mapper;

import cn.tedu.charging.device.pojo.po.ChargingGunInfoPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GunMapper extends BaseMapper<ChargingGunInfoPO> {
    /**
     * 当前持久层使用的mybatis-plus + mysql中自定义sql的注解,可以完全替代 xml映射文件.
     * 如果某些业务中要求做关联查询,而且查询条件,sql内容不断使用 动态标签 推荐xml
     */
    @Select("select gun.* from charging_gun_info gun\n" +
            "left join charging_pile_info pile on gun.pile_id=pile.id\n" +
            "where pile.station_id=#{stationId}")
    List<ChargingGunInfoPO> selectByStationId(Integer stationId);
}
