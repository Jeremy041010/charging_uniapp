package cn.tedu.charging.common.pojo.param;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ChargeParam {
    Integer userId;
    BigDecimal amount;
}
