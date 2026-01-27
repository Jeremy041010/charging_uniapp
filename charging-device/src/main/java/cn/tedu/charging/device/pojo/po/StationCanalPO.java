package cn.tedu.charging.device.pojo.po;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import java.math.BigDecimal;

/**
 * 关心变化的数据
 *
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationCanalPO {

    Integer id;

    @Column(name = "station_name")
    String stationName;

    /**
     * 位置信息 经度
     */
    @Column(name = "station_lng")
    BigDecimal stationLng;

    /**
     * 位置信息 维度
     */
    @Column(name = "station_lat")
    BigDecimal stationLat;
}
