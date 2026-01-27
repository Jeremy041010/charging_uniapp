package cn.tedu.charging.common.pojo.param;

import lombok.Data;

@Data
public class ProgressCostParam {
    private String orderNo;
    private Integer userId;
    private Integer pileId;
    private Integer gunId;
    private Integer stationId;
    private Double totalCapacity;
}
