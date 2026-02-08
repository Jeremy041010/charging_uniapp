package cn.tedu.charging.chargingai.controller;

import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgent;
import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgentOptions;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAgentApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;

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
     * 最终版：仅1次老李:前缀 + 合并完整详细回复 + 修复异常编译问题
     */
    @GetMapping(value = "/bailian/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(String message) {
        // 入参校验：仅1次老李前缀
        if (message == null || message.trim().isEmpty()) {
            return Flux.just("老李: 请输入有效的问题！\n\n");
        }
        log.info("接收前端提问：{}", message);

        Prompt prompt = new Prompt(message.trim());

        // 核心逻辑：合并所有流式内容 → 仅加1次老李: → 保留SSE规范
        return agent.stream(prompt)
                // 转换为纯文本，过滤空内容
                .map(response -> {
                    AssistantMessage output = response.getResult().getOutput();
                    String text = output.getText() == null ? "" : output.getText().trim();
                    // 保留AI返回的规范换行，让格式更美观
                    return text;
                })
                .filter(content -> !content.isEmpty())
                // 合并所有流式分片为【一个完整的字符串】（核心：解决重复前缀）
                .reduce("", (totalContent, currentSegment) -> totalContent + currentSegment)
                // 仅在完整内容前加1次「老李:」，保留SSE结束符\n\n
                .map(completeContent -> "老李: " + completeContent + "\n\n")
                // 修复异常：Mono流的异常处理，用Mono.just替代Flux.just
                .onErrorResume(e -> {
                    log.error("百炼智能体流式响应异常", e);
                    return Mono.just("老李: 很抱歉，处理你的问题时发生异常：" + e.getMessage() + "\n\n");
                })
                // 最后将Mono转回Flux，匹配方法返回值类型
                .flux();
    }

    // 网关测试接口（供前端testGatewayConnection调用）
    @GetMapping("/test")
    public String testGateway() {
        return "charging-ai服务已连接，网关转发正常！";
    }

    // 健康检查接口（供前端testAiHealth调用）
    @GetMapping("/health")
    public String healthCheck() {
        return "{\"status\":\"UP\",\"service\":\"charging-ai\",\"message\":\"百炼智能体服务正常\"}";
    }
}