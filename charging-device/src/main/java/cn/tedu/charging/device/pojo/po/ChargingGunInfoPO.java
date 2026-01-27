package cn.tedu.charging.device.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@TableName("charging_gun_info")
public class ChargingGunInfoPO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("gun_number")
    private String gunNumber;

    @TableField("gun_name")
    private String gunName;

    @TableField("power")
    private BigDecimal power;

    @TableField("electricity")
    private BigDecimal electricity;

    @TableField("voltage_upper_limits")
    private BigDecimal voltageUpperLimits;

    @TableField("voltage_lower_limits")
    private BigDecimal voltageLowerLimits;

    @TableField("gun_type")
    private Integer gunType;

    @TableField("pile_id")
    private Integer pileId;

    @TableField("gun_status")
    private Integer gunStatus;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("deleted")
    private Integer deleted;
}
