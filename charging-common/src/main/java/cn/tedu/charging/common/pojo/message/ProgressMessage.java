package cn.tedu.charging.common.pojo.message;

import lombok.Data;

/**
 * 设备 给 订单服务 同步的 充电进度数据的 对象
 */
@Data
public class ProgressMessage {
    private String orderNo;
    private Integer userId;
    private Integer pileId;
    private Integer gunId;
    private Integer stationId;
    private Long totalTime;
    private Double totalCapacity;
    //是否充满
    private Boolean isFull;
    /**
     * {
     *     "orderNo":"123",
     *     "userId": 24,
     *     "chargingCapacity": 5.8,
     *     "temperature": 95.8,
     *     "stationId":1,
     *     "pileId":1,
     *     "gunId":1,
     *     "isFull": false
     * }
     */
}
