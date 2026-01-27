package cn.tedu.charging.user.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
@TableName("charging_user_info")
public class ChargingUserInfoPO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("wx_open_id")
    private String wxOpenId;

    @TableField("nick_name")
    private String nickName;

    @TableField("balance")
    private BigDecimal balance;

    @TableField("username")
    private String username;

    @TableField("gender")
    private String gender;

    @TableField("age")
    private Integer age;

    @TableField("occupation")
    private String occupation;

    @TableField("password")
    private String password;

    @TableField("phone")
    private String phone;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("deleted")
    private Integer deleted;
}
