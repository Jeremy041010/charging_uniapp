package cn.tedu.charging.common.constant;

public class MqttTopicConst {
    //订单发送消息 开启设备自检命令主题前缀
    public static final String START_GUN_CHECK_PREFIX="charging/device/check/";
    //设备检查完毕 反馈检查结果主题
    public static final String GUN_CHECK_RESULT_TOPIC="charging/device/check/result";
    //设备开始充电后 同步充电进度给订单服务主题
    public static final String CHARGING_PROGRESS_TOPIC="charging/device/progress";
}
