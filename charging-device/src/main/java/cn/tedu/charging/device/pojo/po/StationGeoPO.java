package cn.tedu.charging.device.pojo.po;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StationGeoPO {
    private Integer stationId;
    private Double stationLat;
    private Double stationLng;
    private String stationName;
    private BigDecimal distance;
    private Integer stationStatus;
}
