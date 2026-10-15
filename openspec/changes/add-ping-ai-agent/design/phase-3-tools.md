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

关闭自动循环，手动驱动每次迭代：

```java
ToolCallingManager mgr = ToolCallingManager.builder().build();
ChatClient cc = builder
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
    .build();

ChatClientResponse r = cc.prompt()
    .system(systemPrompt)                                  // 来自 system.md
    .tools(resolvedTools)                                  // 三段式解析结果
    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))  // 2.0 起必填
    .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false)) // 关掉自动循环
    .stream().chatClientResponse();

while (r.hasToolCalls()) {
    // <- 此处即"每次迭代 observable and interruptible"
    //    白名单外 -> 发 tool.confirm 事件挂起, 等适配器回话
    ToolExecutionResult res = mgr.executeToolCalls(prompt, r);
    ...
}
```

- 依据：官方文档明确 *"Each iteration is observable and interruptible"*，另有 `ToolCallObservingAdvisor` 可观测每个 tool-call chunk 并转发。**"挂起等确认"不需要自造对话循环**。
- 交互式确认：发 `tool.confirm` 事件挂起 → 等适配器回话 → 允许则执行、拒绝则不执行且模型收到拒绝结果。

## 3.14 可观测事件

- `tool.requested`：含工具名与参数。
- `tool.finished`：含结果状态。
