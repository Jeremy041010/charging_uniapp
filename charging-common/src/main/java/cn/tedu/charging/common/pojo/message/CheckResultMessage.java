package cn.tedu.charging.common.pojo.message;

import lombok.Data;

/**
 * 充电订单自检结果消息
 */
@Data
public class CheckResultMessage {
    private String orderNo;
    private Integer userId;
    private Boolean result;//该用户的该订单的该充电自检结果
    //充电桩自检成功 发送的消息可以更准确 pileId gunId
    private Integer pileId;
    private Integer gunId;
    //缺少如果是失败,设备通知的失败原因
    private String failDesc;
}
