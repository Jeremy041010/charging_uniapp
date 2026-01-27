package cn.tedu.charging.order.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("charging_bill_exception")
public class ChargingBillExceptionPO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("bill_id")
    private String billId;

    @TableField("electricity")
    private BigDecimal electricity;

    @TableField("voltage")
    private BigDecimal voltage;

    @TableField("temperature")
    private Float temperature;

    @TableField("bill_starttime")
    private Date billStarttime;

    @TableField("create_time")
    private Date createTime;

    @TableField("deleted")
    private Integer deleted;
}
