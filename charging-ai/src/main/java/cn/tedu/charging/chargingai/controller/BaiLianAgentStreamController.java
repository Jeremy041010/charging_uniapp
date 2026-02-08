package cn.tedu.charging.chargingai.controller;

import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgent;
import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgentOptions;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAgentApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ai")
public class BaiLianAgentStreamController {

    private DashScopeAgent agent;
    @Value("${spring.ai.dashscope.agent.options.app-id}")
    private String appId;
    @Value("${spring.ai.dashscope.agent.incremental-output:true}")
    private boolean incrementalOutput;

    private final DashScopeAgentApi dashScopeAgentApi;

    public BaiLianAgentStreamController(DashScopeAgentApi dashScopeAgentApi) {
        this.dashScopeAgentApi = dashScopeAgentApi;
    }

    @PostConstruct
    public void initAgent() {
        DashScopeAgentOptions options = DashScopeAgentOptions.builder()
                .withAppId(appId)
                .withIncrementalOutput(incrementalOutput)
                .build();
        this.agent = new DashScopeAgent(dashScopeAgentApi, options);
        log.info("百炼智能体初始化完成，AppId：{}，增量输出：{}", appId, incrementalOutput);
    }

    /**
     * 最终定型版：固定格式输出（单条老李:前缀+完整分点回复）+ 系统指令固化风格
     */
    @GetMapping(value = "/bailian/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> stream(String message) {
        // 入参校验
        if (message == null || message.trim().isEmpty()) {
            return Flux.just("老李: 请输入有效的问题！\n\n");
        }
        log.info("接收前端提问：{}", message);

        // 核心：系统提示词 - 强制AI按指定格式/风格回复，完全贴合示例
        String systemPrompt = """
        你是充电桩专属智能助手「老李」，仅解答充电桩使用、充电操作、收费、新能源汽车基础问题，非相关问题直接说明不专业并建议咨询专业人士。
        回复严格遵循：
        1. 开头简短自报身份，表明解答方向；
        2. 核心内容按「XX方面：」分模块，每模块仅用1-2句短句，模块间用分号分隔，不用列表；
        3. 结尾极简提醒非专业领域需咨询专人；
        4. 总字数控制在200字内，语言正式，无冗余表述。
        """;

        // 构建Prompt：系统指令+用户问题
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(message.trim())
        ));

        // 流式合并为完整字符串，仅添加1次老李:前缀（贴合你要的格式）
        return agent.stream(prompt)
                .map(response -> {
                    AssistantMessage output = response.getResult().getOutput();
                    return output.getText() == null ? "" : output.getText().trim();
                })
                .filter(content -> !content.isEmpty())
                .reduce("", String::concat) // 合并所有分片为完整内容
                .map(completeContent -> "老李: " + completeContent + "\n\n") // 固定前缀
                .onErrorResume(e -> {
                    log.error("百炼智能体流式响应异常", e);
                    return Mono.just("老李: 很抱歉，处理你的问题时发生异常：" + e.getMessage() + "，请稍后重试！\n\n");
                })
                .flux();
    }

    // 网关测试接口
    @GetMapping("/test")
    public String testGateway() {
        return "charging-ai服务已连接，网关转发正常！";
    }

    // 健康检查接口
    @GetMapping("/health")
    public String healthCheck() {
        return "{\"status\":\"UP\",\"service\":\"charging-ai\",\"message\":\"百炼智能体服务正常\"}";
    }
}