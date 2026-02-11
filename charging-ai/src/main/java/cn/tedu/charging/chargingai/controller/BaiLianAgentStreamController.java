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
import java.time.Duration;
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

    @GetMapping(value = "/bailian/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> stream(String message) {
        // 入参校验
        if (message == null || message.trim().isEmpty()) {
            return Flux.just("老李: 请输入有效的问题！");
        }
        log.info("接收前端提问：{}", message);

        // 系统提示词
        String systemPrompt = """
               你是专业充电桩智能助手「老李」，同时具备通用大模型的全领域知识能力。你可以专业解答充电桩使用、充电操作、充电收费、新能源汽车相关问题；除此之外，生活、学习、工作、技术、代码、娱乐等所有问题你都能正常回答。回答自然、简洁、正常交流即可，不使用固定格式、不强制模块。
        """;

        // 构建Prompt：系统指令+用户问题
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(message.trim())
        ));

        // 合并所有分片为完整文本（保证格式统一），解决超时/连接中止问题
        return agent.stream(prompt)
                .map(response -> {
                    AssistantMessage output = response.getResult().getOutput();
                    String text = output.getText() == null ? "" : output.getText().trim();
                    return text;
                })
                .filter(content -> !content.isEmpty())
                // 合并所有分片为完整字符串（避免格式错乱）
                .reduce("", String::concat)
                // 确保开头有"老李: "前缀（防止AI遗漏）
                .map(completeContent -> completeContent.startsWith("老李: ") ? completeContent : "老李: " + completeContent)
                // 超时保护（5分钟）
                .timeout(Duration.ofMinutes(5), Mono.just("老李: 后端处理超时，请稍后重试！"))
                // 异常兜底
                .onErrorResume(e -> {
                    log.error("智能体响应异常", e);
                    return Mono.just("老李: 很抱歉，处理你的问题时发生异常：" + e.getMessage() + "，请稍后重试！");
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
        return "{\"status\":\"UP\",\"service\":\"charging-ai\",\"message\":\"智能体服务正常\"}";
    }
}