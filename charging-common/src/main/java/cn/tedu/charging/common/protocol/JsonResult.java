package cn.tedu.charging.common.protocol;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * 统一返回结果
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JsonResult<T> {
    /**
     * 状态码 0表示成功 其他表示异常失败
     */
    Integer code;
    /**
     * 消息
     */
    String message;
    /**
     * 接口的出参,具体的数据
     */
    T data;
    public static JsonResult ok(Object data,String msg) {
        JsonResult result = new JsonResult<>();
        result.setCode(0);
        result.setData(data);
        result.setMessage(msg);
        return result;
    }
    public static JsonResult ok(Object data) {
        return ok(data,"OK");
    }
    public static JsonResult ok(){
        return ok(null);
    }
    public static JsonResult error(Integer code,String msg) {
        JsonResult result = new JsonResult<>();
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }
}
