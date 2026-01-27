package cn.tedu.charging.common.pojo.vo;

import lombok.Data;

import java.util.List;
@Data
public class StationDetailVO {
    private Integer stationId;
    private String stationName;
    private String address;
    private Integer stationStatus;
    private List<GunInfoVO> gunInfoVos;
}
