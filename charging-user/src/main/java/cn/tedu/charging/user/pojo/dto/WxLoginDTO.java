package cn.tedu.charging.user.pojo.dto;

import lombok.Data;

/**
 * 微信登录接口返回数据封装
 */
@Data
public class WxLoginDTO {
    /**
     * 用户唯一标识
     */
    private String openid;

    /**
     * 会话密钥
     */
    private String session_key;

    /**
     * 用户在开放平台的唯一标识符
     * 注意：只有在满足UnionID下发条件时才会返回
     */
    private String unionid;

    /**
     * 错误码
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;

    /**
     * 判断请求是否成功
     * @return true-成功 false-失败
     */
    public boolean isSuccess() {
        return errcode == null || errcode == 0;
    }

    /**
     * 获取错误信息，如果成功返回空字符串
     * @return 错误信息
     */
    public String getErrorMsg() {
        if (isSuccess()) {
            return "";
        }
        return String.format("微信登录失败[%d]: %s", errcode, errmsg);
    }
}
