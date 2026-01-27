package cn.tedu.charging.user.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("charging_user_vehicle_bind")
public class ChargingUserVehicleBindPO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("vehicle_id")
    private Integer vehicleId;

    @TableField("state")
    private Integer state;

    @TableField("create_time")
    private Date createTime;

    @TableField("unbind_time")
    private Date unbindTime;

    @TableField("deleted")
    private Integer deleted;
}
