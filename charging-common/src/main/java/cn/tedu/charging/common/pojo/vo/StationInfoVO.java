package cn.tedu.charging.common.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StationInfoVO {
    private Integer stationId;
    private Double stationLat;
    private Double stationLng;
    private String stationName;
    private BigDecimal distance;
    private Integer stationStatus;
    private Double avgPrice;
}
