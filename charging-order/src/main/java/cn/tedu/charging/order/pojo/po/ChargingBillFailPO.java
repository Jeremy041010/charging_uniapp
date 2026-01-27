package cn.tedu.charging.order.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("charging_bill_fail")
public class ChargingBillFailPO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("bill_id")
    private String billId;

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

    @TableField("fail_desc")
    private String failDesc;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("deleted")
    private Integer deleted;
}
