package cn.tedu.charging.common.exception;

import cn.tedu.charging.common.protocol.JsonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.sql.SQLException;

/**
 * 全局异常处理器（完全适配已有JsonResult了。）
 */
// 新注解（修复后）
@RestControllerAdvice
// 或指定明确的扫描包（确保包含控制器和异常处理器）
// @RestControllerAdvice(basePackages = "cn.tedu.charging")
@Slf4j
public class GlobalExceptionHandler {
    // ========== 1. 处理订单模块专属异常（优先级最高） ==========
    @ExceptionHandler(OrderBusinessException.class)
    public JsonResult<?> handleOrderBusinessException(OrderBusinessException ex) {
        // 日志：包含错误码、提示、订单号（便于排查）
        log.error("【订单业务异常】错误码：{}，提示：{}，订单号：{}",
                ex.getCode(), ex.getMessage(), ex.getBillId());
        // 调用已有JsonResult.error()返回异常
        return JsonResult.error(ex.getCode(), ex.getMessage());
    }

    // ========== 2. 处理通用业务异常 ==========
    @ExceptionHandler(ServiceException.class)
    public JsonResult<?> handleServiceException(ServiceException ex) {
        log.error("【通用业务异常】错误码：{}，提示：{}", ex.getCode(), ex.getMessage());
        return JsonResult.error(ex.getCode(), ex.getMessage());
    }

    // ========== 3. 处理封装类参数校验异常（@Valid + POJO） ==========
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JsonResult<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getFieldError().getDefaultMessage();
        log.error("【参数校验异常-封装类】提示：{}", errorMsg);
        // 参数校验失败固定用400码
        return JsonResult.error(400, "参数错误：" + errorMsg);
    }

    // ========== 4. 处理非封装类参数校验异常（@RequestParam + 注解） ==========
    @ExceptionHandler(ConstraintViolationException.class)
    public JsonResult<?> handleConstraintViolationException(ConstraintViolationException ex) {
        String[] msgArray = ex.getMessage().split(":");
        String errorMsg = msgArray.length > 1 ? msgArray[1].trim() : msgArray[0].trim();
        log.error("【参数校验异常-非封装类】提示：{}", errorMsg);
        return JsonResult.error(400, "参数错误：" + errorMsg);
    }

    // ========== 5. 处理空指针异常（订单模块高频场景） ==========
    @ExceptionHandler(NullPointerException.class)
    public JsonResult<?> handleNullPointerException(NullPointerException ex) {
        log.error("【空指针异常】详情：", ex); // 打印堆栈
        return JsonResult.error(500, "系统服务调用异常，请稍后重试");
    }

    // ========== 6. 处理数据库异常 ==========
    @ExceptionHandler(SQLException.class)
    public JsonResult<?> handleSQLException(SQLException ex) {
        log.error("【数据库异常】详情：", ex);
        return JsonResult.error(500, "数据处理异常，请联系管理员");
    }

    // ========== 7. 兜底：处理所有未捕获的运行时异常 ==========
    @ExceptionHandler(RuntimeException.class)
    public JsonResult<?> handleRuntimeException(RuntimeException ex) {
        log.error("【系统运行时异常】详情：", ex);
        return JsonResult.error(500, "系统繁忙，请稍后重试");
    }

    // ========== 8. 最终兜底：处理所有异常 ==========
    @ExceptionHandler(Exception.class)
    public JsonResult<?> handleException(Exception ex) {
        log.error("【系统未知异常】详情：", ex);
        return JsonResult.error(500, "系统异常，请联系管理员");
    }
}