package cn.tedu.charging.common.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GunInfoVO {
    private Integer gunId;
    private String gunName;
    private Integer gunStatus;
    private Integer gunType;
    private BigDecimal electricity;
}
