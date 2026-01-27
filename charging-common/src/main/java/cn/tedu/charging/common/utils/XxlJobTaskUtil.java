package cn.tedu.charging.common.utils;

import cn.tedu.charging.common.constant.XxlJobAuthConst;
import cn.tedu.charging.common.pojo.dto.XxlJobGroup;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class XxlJobTaskUtil {
    private static final RestTemplate restTemplate = new RestTemplate();
    //根据传入的参数 创建一个任务
    private static final String XXL_JOB_ADMIN_URL = "http://localhost:8080/xxl-job-admin";
    private static final HttpHeaders headers = new HttpHeaders();



    /**
     * 专门为当前扫码下单 创建计划任务编写封装的一个http请求
     * @param cronExpression 计划时间表达式 调用者生成计算
     * @param executorName 执行器名称 固定值 order-executor
     * @param orderNo 任务参数 具体传递是数据 billId
     * @return
     */
    public static boolean createJobTask(String cronExpression,String executorName, String orderNo){
        //1.利用restTemplate 发送http请求根据executorName查询jobGroupId
        XxlJobGroup jobGroup = getJobGroupByName(executorName);
        System.out.println(jobGroup);
        //2.利用这个对象,创建一个任务
        return doCreateJobTask(jobGroup,cronExpression,orderNo);
    }

    private static boolean doCreateJobTask(XxlJobGroup jobGroup, String cronExpression, String orderNo) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("jobGroup", jobGroup.getId()); // 注意：XXL-JOB 可能需要字符串形式的数字
        formData.add("jobDesc", "订单状态检查任务");
        formData.add("author", "admin");
        formData.add("alarmEmail", "");
        formData.add("scheduleType", "CRON");
        formData.add("scheduleConf", cronExpression);
        formData.add("executorBlockStrategy", "SERIAL_EXECUTION");
        formData.add("misfireStrategy", "DO_NOTHING");
        formData.add("executorRouteStrategy", "FIRST");
        formData.add("executorHandler", "order-status-check");//@XxlJob
        formData.add("executorParam", orderNo);
        formData.add("executorTimeout", "0");
        formData.add("executorFailRetryCount", "0");
        formData.add("glueType", "BEAN");
        formData.add("glueSource", "");
        formData.add("glueRemark", "GLUE代码初始化");
        formData.add("childJobId", "");
        formData.add("triggerStatus", "1");
        formData.add("triggerLastTime", "0");
        formData.add("triggerNextTime", "0");
        String adminUrl = "http://localhost:8080/xxl-job-admin/jobinfo/add";
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                adminUrl,
                HttpMethod.POST,
                request,
                String.class
        );
        // 4. 验证响应
        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("延迟任务设置成功，将在2分钟后执行");
            return true;
        } else {
            System.out.println("设置失败: " + response.getBody());
            return false;
        }
    }

    public static XxlJobGroup getJobGroupByName(String executorName) {
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Cookie", XxlJobAuthConst.COOKIE_NAME_VALUE);
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("start", 0);
        formData.add("length", 10);
        formData.add("appname", executorName);
        formData.add("title", "");
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(formData, headers);
        String adminUrl = "http://localhost:8080/xxl-job-admin/jobgroup/pageList";
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                adminUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        );
        Map<String, Object> responseBody = response.getBody();
        // 提取data字段并转换为List<XxlJobGroup>
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseBody.get("data");
        if (dataList != null && !dataList.isEmpty()) {
            // 手动将Map转换为XxlJobGroup对象
            return convertMapToXxlJobGroup(dataList);
        }else{
            return null;
        }
    }

    private static XxlJobGroup convertMapToXxlJobGroup(List<Map<String, Object>> dataList) {
        XxlJobGroup jobGroup = new XxlJobGroup();
        dataList.stream().forEach(data->{
            if (data.containsKey("appname")&&data.get("appname").equals("order-executor")){
                jobGroup.setId((int) data.get("id"));
                jobGroup.setAppname((String) data.get("appname"));
                jobGroup.setTitle((String) data.get("title"));
                jobGroup.setAddressList((String) data.get("addressList"));
                jobGroup.setAddressType((int) data.get("addressType"));
            }
        });
        return jobGroup;
    }
}
