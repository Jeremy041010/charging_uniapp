package cn.tedu.charging.common.protocol;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * websocket消息
 * 统一的消息格式对象
 * @param <T>
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebSocketResult<T> {

    /**
     * 消息类型
     * 2 余额不足
     * 3 充电进度
     * 1 订单的消息
     */
    Integer state;

    /**
     * 消息内容
     */
    String message;

    /**
     *  数据
     */
    T data;
}
