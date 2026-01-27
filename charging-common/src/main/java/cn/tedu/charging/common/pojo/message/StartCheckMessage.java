package cn.tedu.charging.common.pojo.message;

import lombok.Data;

@Data
public class StartCheckMessage {
    private String orderNo;
    private Integer userId;
    private Integer gunId;
}
