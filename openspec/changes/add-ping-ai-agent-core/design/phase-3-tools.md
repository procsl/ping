# 阶段 3 设计：工具注册与执行策略

对应 `tasks.md` 阶段 3（任务 3.1–3.15）。本文档承载实现级细节。架构决策见 `../design.md` D8。

> 注：本文档由 design.md 迁移而来，尚未按细化节奏展开；后续细化时补充工具注册表契约与策略决策类型。

## 3.1–3.6 工具注册与三段式选择

- 全局工具清单在代码中定义（读文件、检索类各至少一个）。
- 三段式选择：会话开始时显式指定 → 未指定回落角色默认（`role.json.tools`）→ 清单外工具名告警忽略。
- `tools` 字段接受字符串数组；将来传参时升级为对象 `{"grep": {"max_results": 5}}`，字符串与对象同时接受。

## 3.7–3.9 策略接口

- 决策类型：允许 / 询问 / 拒绝。
- 非交互式：白名单内放行，白名单外拒绝且不执行。
- 被拒工具的结果需以结构化形式反馈给模型，模型仍能继续。

## 3.10–3.13 Spring AI 工具循环

关闭自动循环，手动驱动每次迭代。**以下代码以 2.0.1 的实际签名为准**（已用 `javap` 核对）：

```java
ToolCallingManager mgr = ToolCallingManager.builder().build();
ChatClient cc = builder
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
    .build();

ChatClient.ChatClientRequestSpec req = cc.prompt()
    .system(systemPrompt)                                            // 来自 system.md
    .tools(resolvedTools)                                            // 三段式解析结果
    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))   // 2.0 起必填
    .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false));  // 关掉自动循环

// 注意：stream().chatClientResponse() 返回 Flux<ChatClientResponse>，不是单个响应
Flux<ChatClientResponse> flux = req.stream().chatClientResponse();
ChatResponse response = /* 由 flux 收集出的完整响应 */;

while (response.hasToolCalls()) {          // hasToolCalls() 在 ChatResponse 上，不在 ChatClientResponse 上
    // <- 此处即"每次迭代 observable and interruptible"
    //    白名单外 -> 发 tool.confirm 事件挂起, 等适配器回话
    ToolExecutionResult res = mgr.executeToolCalls(prompt, response);  // 签名是 (Prompt, ChatResponse)
    ...
}
```

- 依据：官方文档明确 *"Each iteration is observable and interruptible"*，另有 `ToolCallObservingAdvisor` 可观测每个 tool-call chunk 并转发。**"挂起等确认"不需要自造对话循环**。
- **本变更的确认策略用测试替身验证**：发 `tool.confirm` 事件后由替身回话，验证「确认前不执行」；真实的确认 UI 属于 `add-ping-ai-nvim-tui`。
- 挂起等待的调度器与超时策略见 `../design.md` Open Questions，本变更只需固定语义（确认前不执行、拒绝后模型收到结构化拒绝结果）。

## 3.14 可观测事件

- `tool.requested`：含工具名与参数。
- `tool.finished`：含结果状态。
