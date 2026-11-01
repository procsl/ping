# 阶段 2 设计：HTTP 与 WebSocket

对应 `tasks.md` 阶段 2（任务 2.1–2.4）。架构决策见 `../design.md` D10（web 部分）。

> 注：本文档待细化，当前只固定传输形态与背压策略；具体路径与字段见 Open Questions。

## HTTP 端点

- 路径遵循仓库约定：每个 mapping 写全 `/v1/...`；分页 `offset`/`limit` one-indexed；JSON `SNAKE_CASE`。
- 鉴权沿用宿主，本变更不做特殊处理。

## WebSocket 通道

```
客户端 ──WS──► WebAdapter ──► EventStream（与终端共用）
                  ▲
                  └── tool.confirm 请求/响应走同一通道
```

- 下行：`message.delta` / `tool.requested` / `tool.finished` / `message.completed` / `error`。
- 上行：用户输入、取消、确认响应。
- 编码是传输层的事，事件类型与含义不变（D15 的一致性要求）。

## 背压

- 慢客户端堆积时，**丢弃中间增量而不是阻塞模型产出** —— 与核心 EventStream 的背压策略一致。
- 丢弃需在客户端可感知（例如最终 `message.completed` 携带完整文本），避免出现「内容缺失但无提示」。

## 验证方式

- 请求端点断言响应结构。
- 建立 WS 连接、收发消息断言事件类型对应。
- 模拟慢消费断言模型产出不被阻塞。
