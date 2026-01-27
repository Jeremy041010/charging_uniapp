package cn.tedu.charging.common.pojo.message;

import lombok.Data;

@Data
public class ProgressData {
    private Double chargingCapacity;
    private Double totalCapacity;
    private Double oneElectricityCost;
    private Integer hours;
    private Double totalCost;
    private Integer minutes;
    private Integer seconds;
    /**
     * {
     *         "chargingCapacity":6.0,
     *         "totalCapacity": 15.0,
     *         "oneElectricityCost": 1.5,
     *         "hours":0,
     *         "minutes":5,
     *         "seconds":0,
     *         "totalCost":15.8
     *     }
     */
}
