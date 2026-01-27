package cn.tedu.charging.user.utils;

import cn.tedu.charging.common.constant.AppAuthConst;
import cn.tedu.charging.user.pojo.dto.WxLoginDTO;
import com.alibaba.fastjson2.JSON;
import org.springframework.web.client.RestTemplate;


public class WxOpenIdUtils {
    private static final RestTemplate restTemplate=new RestTemplate();

    public static WxLoginDTO getWxLoginDTO(String code) {
        RestTemplate restTemplate=new RestTemplate();
        String url="https://api.weixin.qq.com/sns/jscode2session?appid={1}&secret={2}&js_code={3}&grant_type=authorization_code";
        String result = restTemplate.getForObject(url, String.class, AppAuthConst.APP_ID, AppAuthConst.APP_SECRET, code);
        WxLoginDTO wxLoginDTO = JSON.parseObject(result, WxLoginDTO.class);
        return wxLoginDTO;
    }
}
