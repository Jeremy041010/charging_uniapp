package cn.tedu.charging.order.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("charging_bill_success")
public class ChargingBillSuccessPO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("bill_id")
    private String billId;

    @TableField("charging_capacity")
    private Integer chargingCapacity;

    @TableField("electric_money")
    private BigDecimal electricMoney;

    @TableField("service_money")
    private BigDecimal serviceMoney;

    @TableField("charging_duration")
    private Integer chargingDuration;

    @TableField("user_id")
    private Integer userId;

    @TableField("operator_id")
    private Integer operatorId;

    @TableField("station_id")
    private Integer stationId;

    @TableField("gun_id")
    private Integer gunId;

    @TableField("vehicle_id")
    private Integer vehicleId;

    @TableField("charging_start_time")
    private Date chargingStartTime;

    @TableField("charging_end_time")
    private Date chargingEndTime;

    @TableField("bill_status")
    private Integer billStatus;

    @TableField("pay_amount")
    private BigDecimal payAmount;

    @TableField("pay_time")
    private Date payTime;

    @TableField("pay_channel")
    private Integer payChannel;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("deleted")
    private Integer deleted;
}
