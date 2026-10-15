# 阶段 2 设计：对话核心

对应 `tasks.md` 阶段 2（任务 2.1–2.16）。本文档承载实现级细节。架构决策见 `../design.md` D1（EventStream）、D8（工具循环）、D9（会话持久化）。

> 注：本文档由 design.md 迁移而来，尚未按细化节奏展开；后续细化时补充字段表与边界用例。

## 2.1–2.2 事件类型与通道

事件类型清单（字段与分片格式见 `../design.md` Open Questions，此处只定类型）：

```
session.started   会话建立（含解析出的 model / tools）
message.delta     流式文本增量
tool.requested    模型请求调用工具（工具名 + 参数）
tool.finished     工具结束（结果状态 + 摘要）
tool.confirm      交互式确认请求（仅白名单外）
message.completed 一条完整消息结束
error             任何阶段的失败
session.ended     会话结束
```

通道要求：保证顺序、背压不丢事件。

## 2.3–2.6 提示词加载

- `system.md` → 系统提示词；缺失时降级继续，不中断会话。
- `first.md` → 默认用户提示词；缺失时不影响启动。

### first.md 在两个适配器的分叉

core 只提供内容，如何使用由适配器决定：

```
core: 读 roles/<name>/first.md, 交出内容
              |
   +----------+----------+
   |                     |
非交互 CliAdapter     交互 NvimAdapter
   |                     |
   v                     v
 直接作为用户消息发出    预填输入框 (可编辑可清空)
 (无人可编辑)           空输入本身不做任何事
```

## 2.7–2.8 模型组装

- 角色声明模型时用该模型；未声明时回退全局默认。
- 由于配置不耦合 Spring（见 `../design.md` D7），**不能依赖 `spring.ai.*` 自动配置**，需由 `AgentRuntime` 显式持有 `GlobalConfig` 并显式构造模型客户端。

## 2.9–2.13 会话持久化

### 会话 JSON 结构（`roles/<name>/sessions/<session-id>.json`）

```json
{
  "session_id": "2026-10-15-a1b2",
  "role": "dev",
  "model": "claude_x",
  "messages": [
    { "type": "system",    "text": "..." },
    { "type": "user",      "text": "..." },
    { "type": "assistant", "text": "...", "tool_calls": [] },
    { "type": "tool",      "tool_call_id": "...", "text": "..." }
  ]
}
```

四类消息：system / user / assistant / tool。

### Spring AI 接入

```java
ChatMemory memory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(new RoleFileRepository(roleDir))  // -> sessions/*.json
    .maxMessages(configuredWindow)                          // 窗口上限
    .build();
```

- 实现 `ChatMemoryRepository` 接口写入角色目录，配 `MessageWindowChatMemory` 提供窗口上限，无需自造记忆层。
- **Spring AI 2.0 起 `CONVERSATION_ID` 为必填**，缺失即抛错。
- 会话按角色隔离：角色甲的会话对角色乙不可见。

## 2.14–2.16 流式执行与错误

- 流式对话执行：组装提示词 → 流式调用 → 发布增量事件。
- 模型调用失败时发布 `error` 事件，而非静默结束。
- 工具循环的手动驱动见 `phase-3-tools.md`。
