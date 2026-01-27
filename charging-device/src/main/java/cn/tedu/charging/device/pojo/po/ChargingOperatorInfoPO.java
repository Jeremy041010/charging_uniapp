package cn.tedu.charging.device.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Date;

@Data
@TableName("charging_operator_info")
public class ChargingOperatorInfoPO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("operator_number")
    private String operatorNumber;

    @TableField("operator_name")
    private String operatorName;

    @TableField("business")
    private String business;

    @TableField("phone")
    private String phone;

    @TableField("address")
    private String address;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("deleted")
    private Integer deleted;
}
