package cn.tedu.charging.cost.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("charging_cost_rule")
public class ChargingCostRulePO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("station_id")
    private Integer stationId;

    @TableField("gun_type")
    private Integer gunType;

    @TableField("name")
    private String name;

    @TableField("start_time")
    private Integer startTime;

    @TableField("end_time")
    private Integer endTime;

    @TableField("power_fee")
    private BigDecimal powerFee;

    @TableField("service_fee")
    private BigDecimal serviceFee;
}
