package cn.tedu.charging.common.pojo.message;

import lombok.Data;

import java.io.Serializable;
@Data
public class DelayCheckMessage implements Serializable {
    private String orderNo;
    private Integer userId;
    private Integer pileId;
    private Integer gunId;
    private Integer vehicleId;
}
