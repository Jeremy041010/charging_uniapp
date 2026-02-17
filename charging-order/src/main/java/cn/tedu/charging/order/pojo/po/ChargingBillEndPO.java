package cn.tedu.charging.order.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("charging_bill_success")  // 映射到现有成功订单表
public class ChargingBillEndPO {

    @TableField("bill_id")
    private String billId;

    @TableField("charging_duration")  // 充电时长
    private Integer chargingDuration;

    @TableField("pay_amount")  // 消费金额
    private BigDecimal consumeAmount;

    @TableField("bill_status")  // 订单状态：1充电中 2正常结束 3异常结束 4欠费
    private Integer endReason;

    @TableField("update_time")
    private Date updateTime;

    @TableField("deleted")
    private Integer deleted;
}
