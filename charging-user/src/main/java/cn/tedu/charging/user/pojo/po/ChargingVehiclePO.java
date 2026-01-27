package cn.tedu.charging.user.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;


@Data
@TableName("charging_vehicle")
public class ChargingVehiclePO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("brand")
    private String brand;

    @TableField("license")
    private String license;

    @TableField("model")
    private String model;

    @TableField("vin")
    private String vin;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("deleted")
    private Integer deleted;
}
