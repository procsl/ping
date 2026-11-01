# 阶段 3 设计：工具、hook 与执行策略

对应 `tasks.md` 阶段 3（任务 3.1–3.18）。本文档承载实现级细节。架构决策见 `../design.md` D8。

## 3.1 工具服务（ToolService）

工具由**工具服务**统一管理：系统里已经开发好的工具都在这里，按名称查询到执行对象。

```java
interface ToolService {
    List<ToolDescriptor> query(ToolQuery query);   // 按角色/会话条件查询可用工具
    ToolExecutor find(String name);                // 按名称取执行对象
}

interface ToolDescriptor {
    String name();
    String description();
    Map<String, Object> inputSchema();
}
```

- Session 初始化时**查询一次**，把结果绑定进 LLM 的 tools 上下文。
- 没有注册进上下文的工具，模型看不见也调不到 —— **这就是白名单**，不另设第二份配置。
- 本变更提供代码内注册表的实现（读文件、检索各至少一个）；将来接 MCP 或外部注册中心只换实现。
- 名称约束：`^[a-z][a-z0-9_]{1,63}$`；清单外的名字在解析阶段告警并忽略。

## 3.2 三段式选择

```
会话开始时显式指定
   └─ 未指定 → 角色创建时配置的工具列表（role.json.tools）
        └─ 清单外名字 → 告警忽略
```

- 会话级指定覆盖角色默认；两者都只从 `ToolService.query` 的结果里取。
- `tools` 字段接受字符串数组；将来传参时升级为对象 `{"grep": {"max_results": 5}}`，字符串与对象同时接受。

## 3.3 Hook 机制

```java
enum Decision { PASS, REJECT, WAIT, REPLACE }

record HookResult(Decision decision, Object data) {}

interface Hook {
    HookResult apply(HookStage stage, HookContext ctx, Object data);
}
```

- **入参**：阶段名 + context + 完整数据（流式阶段传入拼装完成的完整内容，不是单个片段）。
- **返回**：执行策略。`PASS` 继续；`REJECT` 短路；`WAIT` 挂起等外部输入；`REPLACE` 替换内容。
- **任意阶段都可以注入 hook**；具体阶段集合暂缓定义（见 `../design.md` Open Questions），机制先落地。
- `REPLACE` 只在允许替换的阶段有效，其余阶段返回即视为配置错误。
- hook **无状态**、不依赖 Spring，直接 new；多个 hook 按注册顺序执行，任一 `REJECT` 短路，`REPLACE` 的结果传给下一个。

## 3.4 工具白名单：hook 的双校验

白名单是 hook 的一个实现，**请求时与执行完成时各校验一次**：

| 时机 | 校验内容 | 不通过时 |
|---|---|---|
| 工具调用请求 | 工具名在本次会话已绑定的工具集内；参数符合该工具的 `inputSchema` | 不执行；回模型 `{"status":"denied","reason":...}`；发一个 tool 对象显示「调用失败」 |
| 执行完成 | 结果归属同一次已批准的调用（防并发串号）；结果大小在截断上限内 | 结果不发给模型；改为回 `{"status":"failed","reason":...}` |

- 两次都通过，才把结果送回模型。
- 被拒绝、失败、超时的工具调用**都算「已处理」**，轮次计数照常递减。

## 3.5 工具执行：Spring AI 内置循环

工具调用与结果回填由 Spring AI 内置循环完成，**不自造对话循环**；box 只是渲染推送，不参与循环控制。

```java
List<ToolCallback> callbacks = tools.stream()
    .map(tool -> new HookedToolCallback(
        registry.find(tool.name()), tool.name(), hooks, hookContext, session, truncator))
    .toList();

ChatClient.ChatClientRequestSpec request = chatClient.prompt()
    .system(systemPrompt)
    .messages(promptMessages)
    .toolCallbacks(callbacks);
```

- `HookedToolCallback` 包装真实的 `ToolCallback`：调用前 hook → 执行 → 调用后 hook → 截断 → 投递对象。
- 工具结果截断后回给模型；对象里只放摘要。
- **线程**：模型请求在虚拟线程上；工具由 Spring AI 在流式过程中调用，可能不在调用方线程，`Session` 的投递方法自带锁。
- **确认**：hook 返回 `WAIT` 时，非交互形态按拒绝处理；交互形态由适配器挂起等待（`add-ping-ai-nvim-tui`）。

## 3.6 轮次完成

内置循环意味着模型流结束前所有工具调用都已处理完，因此：

- 轮次结束 = 模型流结束 + 所有对象结束。
- 工具成功、失败、被拒、超时都算已处理。
- 保留 `pending` 计数与 `onToolCompleted` 入口，供将来换成异步工具执行器时使用。

## 3.7 对象交付

- 工具发起与结束共用同一个对象 `id`：`seq=0` 的内容带工具名与参数，结束段带状态与结果摘要。
- 具体 `type` 取值由实现约定，不属于 box 契约。
- 工具结果与界面展示分离：模型收到完整结构化结果，对象里放可渲染摘要。
- 截断：回模型的结果上限约 32KB（可配置），超限保留头尾并插入 `[已截断 N 行]`；box 摘要只留前 40 行 / 2KB。
