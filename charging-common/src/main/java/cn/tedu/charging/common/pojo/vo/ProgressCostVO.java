package cn.tedu.charging.common.pojo.vo;

import lombok.Data;

@Data
public class ProgressCostVO {
    //本次充电后 累计总金额
    private Double totalCost;
    //本次充电 使用的单价
    private Double powerFee;
    //单次度数
    private Double chargingCapacity;
}
