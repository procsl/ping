# 阶段 2 设计：HTTP 与 WebSocket

对应 `tasks.md` 阶段 2（任务 2.1–2.4）。架构决策见 `../design.md` D10（web 部分）。

> 注：本文档待细化，当前只固定传输形态与背压策略；具体路径与字段见 Open Questions。

## HTTP 端点

- 路径遵循仓库约定：每个 mapping 写全 `/v1/...`；分页 `offset`/`limit` one-indexed；JSON `SNAKE_CASE`。
- 鉴权沿用宿主，本变更不做特殊处理。

## WebSocket 通道

```
客户端 ──WS──► WebAdapter ──► BoxStream（与终端共用）
                  ▲
                  └── 上行命令走同一通道
```

- 下行：box，原样承载 `type` / `id` / `seq` / `content`，不做任何语义转换。
- 上行命令：`submit` / `confirm` / `cancel` / `ack` / `pull`。
- 编码是传输层的事，box 结构与 id 规则不变（D15 的一致性要求）。

## 背压

- 慢客户端**不断缓冲、不丢片段**：超过阈值即断开连接。
- 客户端重连后发 `pull(from_id)`，从缺失的 `id` 起重放，按对象重建。
- 这样不需要「丢弃中间增量」这类会破坏内容完整性的策略。

## 验证方式

- 请求端点断言响应结构。
- 建立 WS 连接、收发 box，断言四个字段与对象 id 连续。
- 模拟慢消费：断言连接被断开，重连后 `pull` 能恢复一致状态。
