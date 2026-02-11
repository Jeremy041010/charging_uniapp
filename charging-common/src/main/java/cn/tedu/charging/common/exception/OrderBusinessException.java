package cn.tedu.charging.common.exception;

import lombok.Getter;

/**
 * 订单模块专属业务异常（适配已有JsonResult）
 */
@Getter
public class OrderBusinessException extends ServiceException {
    // 订单模块专属错误码（建议4001-4099段）
    public static final Integer ORDER_GUN_UNAVAILABLE_CODE = 4001;
    public static final String ORDER_GUN_UNAVAILABLE_MSG = "充电枪状态不可用，无法下单";

    public static final Integer ORDER_USER_CHECK_FAIL_CODE = 4002;
    public static final String ORDER_USER_CHECK_FAIL_MSG = "用户与车辆绑定关系异常，无法下单";

    public static final Integer ORDER_CREATE_FAIL_CODE = 4003;
    public static final String ORDER_CREATE_FAIL_MSG = "订单创建失败，请稍后重试";

    // 扩展字段：订单号（便于日志排查）
    private String billId;

    // 构造1：仅传错误码+提示（无订单号）
    public OrderBusinessException(Integer code, String message) {
        super(code, message);
    }

    // 构造2：错误码+提示+订单号（有订单号）
    public OrderBusinessException(Integer code, String message, String billId) {
        super(code, message);
        this.billId = billId;
    }
}