package cn.tedu.charging.common.exception;

import lombok.Getter;

/**
 * 通用业务异常（适配已有JsonResult）
 */
@Getter
public class ServiceException extends RuntimeException {
    // 错误码（对应JsonResult的code）
    private final Integer code;
    // 提示信息（对应JsonResult的message）
    private final String message;

    // 构造方法：传入错误码+提示信息
    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}