# 阶段 2 设计：对话核心

对应 `tasks.md` 阶段 2（任务 2.1–2.25）。本文档承载实现级细节。架构决策见 `../design.md` D1（BoxStream）、D8（工具循环）、D9（会话持久化）、D12（图片输入）。

## 2.1–2.2 Box 与对象

### Box

```json
{ "type": "text", "id": 1, "seq": 0, "content": "我先读一下" }
```

| 字段 | 规则 |
|---|---|
| `type` | 开放字符串。协议不定义取值，消费端自行分派；同一对象内不变 |
| `id` | 对象 id，会话内从 0 递增、不复用；渲染顺序 = id 升序，id 0 恒在顶端 |
| `seq` | 对象内片段序号：`0` 开始、`>0` 追加；**其余（负数、缺失、非法）一律表示该对象结束** |
| `content` | 字符串，不透明。结构化数据由后端序列化，消费端按 `type` 解释 |

- `content` 永远是字符串，协议不假定其内部形态。
- `seq` 的判定只看「能否解析为非负整数」；不能解析的一律当结束，不抛错。
- 后端不解释 `type` 的语义，只负责在封装时打上标记。
- 下文示例中的 `user` / `text` / `tool` / `confirm` / `error` 是**实现约定的取值**，不属于 box 协议。

### 对象

对象是一段可渲染内容，由同一 `id` 的 box 组成。

```
流式对象（助手文本、工具参数）
  seq=0 → seq=1 → seq=2 → seq<0

完整对象（用户消息、历史重放、错误）
  seq=0 带完整内容 → seq<0
```

- **同一会话内允许多个对象同时推送**，不同 `id` 的 box 可以交错到达。
- 对象之间只靠 `id` 排序，不靠到达顺序拼接。
- 新对象分配下一个 id；id 只增不复用。

### 示例：一轮完整会话

```
id=0  user   "帮我看看这个文件"                       完整对象
id=1  text   "我先读一下" → "这个文件。" → 结束       流式对象
id=2  tool   {"name":"read_file",...} → 结束         流式或完整
id=3  text   "里面定义了一个…" → 结束                 流式对象
```

对应的 box 序列：

```
box(id=0, type=user, seq=0,  "帮我看看这个文件")
box(id=0, type=user, seq=-1, "")
box(id=1, type=text, seq=0,  "我先读一下")
box(id=2, type=tool, seq=0,  "{\"name\":\"read_file\",...}")
box(id=1, type=text, seq=1,  "这个文件。")
box(id=2, type=tool, seq=-1, "")
box(id=1, type=text, seq=-1, "")
box(id=3, type=text, seq=0,  "里面定义了一个…")
box(id=3, type=text, seq=-1, "")
```

`id=1` 与 `id=2` 的片段交错到达，消费端按 id 归组，互不影响。

## 2.24–2.25 上游消息

### 消息模型

```
{ type, ...该类型自己的字段 }
```

- 只有 `type` 公共；Java 侧用 sealed interface + `@JsonTypeInfo` 实现判别联合。
- 与下游 box 的不对称是有意的：下游是统一分片流，上游是异构命令集。

### 类型与字段

| type | 字段 | 服务端行为 |
|---|---|---|
| `user` | `text`?、`images`?、`request_id`? | 校验 → 分配 id → 回送 user 对象 → 开始一轮 |
| `confirm` | `request_id`、`decision` | 匹配挂起的确认 |
| `cancel` | 无 | 中断当前轮次 |
| `ack` | `id` | 记录投递进度 |
| `pull` | `from`?（默认 0） | 从该 id 起重放 |

### 用户消息

```json
{ "type": "user", "text": "帮我看看这个文件", "images": ["a.png"] }
```

- `text` 与 `images` 的取舍不设协议级限制：纯文本、纯图片、图文混合都可以提交，由后端按模型能力与上下文校验。
- 校验不通过时回 `error` 对象，不分配 id。
- `request_id` 可选；携带时服务端按它去重，重试返回同一个对象 id。
- 上游字段是原始输入，下游 `user` 对象的 `content` 是显示投影，两者不要求相同。

### 提交与回送时序

```
客户端                       服务端
  │ user(text,images,rid?)     │
  │───────────────────────────►│
  │                            │ 校验（含模型能力）
  │                            │ request_id 去重
  │                            │ 分配 id=N
  │◄── box(user, N, 0, …) ─────┤
  │◄── box(user, N, -1, "") ───┤
  │                            │ 开始一轮
```

## 2.3–2.6 提示词加载

- `system.md` → 系统提示词；缺失时降级继续，不中断会话。
- `first.md` → 默认用户提示词；缺失时不影响启动。

### first.md 在两个适配器的分叉

core 只提供内容，如何使用由适配器决定：

```
core: 读 roles/<name>/first.md, 交出内容
              |
              v
非交互 CliAdapter：直接作为用户消息发出（无人可编辑）

交互 NvimAdapter：预填输入框（可编辑可清空）
                  —— 该分支属于 add-ping-ai-nvim-tui
```

### first.md 缺失

- 非交互式未提供提示词时，给出明确错误并以非零状态退出，**不静默发送空消息**（见 `ai-agent-conversation` 与 `ai-agent-cli` 的对应场景）。

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
- **会话持久化与 box 流是两回事**：落盘的是原始消息，box 只是渲染投影，不写文件。

## 2.14–2.16 流式产出与错误

- 流式对话执行：组装提示词 → 流式调用 → 每个增量封装成同一对象的下一段 box。
- 模型调用失败：产生一个 `error` 对象（完整对象），而不是静默结束。
- 工具循环的手动驱动见 `phase-3-tools.md`；工具发起与结束各自封装为对象。
- box 只用于渲染，不改变模型侧的消息结构。

### 忙时拒绝

```
id=5  text  "…还在输出…"        ← 未结束

客户端 ──submit("新问题")──►

服务端 ── box(id=6, type=error, seq=0, "{\"code\":\"busy\"}")
       ── box(id=6, type=error, seq=-1, "")
```

- 上一轮未结束时，服务端**不取消、不排队**，直接回 `error` 对象。
- 用户消息不被接受，不分配 user 对象；客户端保留输入等待重试。
- `error` 对象占用 id 并进入对象表，保证 id 连续。
- `error` 的 `content` 由后端决定（例如 JSON 字符串带 `code`），可表达忙、429、网络等各类错误。

## 2.17–2.18 图片输入

图片是**输入侧一等能力**，与终端能力无关（见 `../design.md` D12）：

```
用户引用 /path/to/a.png
   → 校验可读（不存在或不可读 -> 明确错误，不发送残缺消息）
   → 复制到 ~/.lwl/images/<hash>.<ext>（同内容不重复复制）
   → 读取为 base64
   → 与文本一起组装为多模态用户消息（文本顺序不变）
```

- 落盘目录复用配置根（`~/.lwl/images/`），不放在角色目录 —— 图片可跨会话复用。
- 文件名以内容哈希命名，避免同名覆盖与重复复制。
- 用户消息对象的 `content` 由后端序列化成字符串（含文本与图片引用），消费端按 `type` 解释。
- 输出侧展示（无法显示时显示文件名）不在本变更范围。

## 2.19–2.23 重放、确认与断线

### pull

```
pull()          从头重放
pull(from_id)   从指定对象 id 开始重放
```

- 与实时推送同一套 box，唯一区别：每个对象只发两条 —— `seq=0` 带完整内容，`seq<0` 结束。
- 客户端替换 `id >= from_id` 的对象，之前的保持不动。
- 重放使用对象原本的 id，不重新编号。

```
pull(from=1)
  ◄── box(id=1, type=text, seq=0, "我先读一下这个文件。")
  ◄── box(id=1, type=text, seq=-1, "")
  ◄── box(id=2, type=tool, seq=0, "{...完整工具调用...}")
  ◄── box(id=2, type=tool, seq=-1, "")
```

### ack 与丢包

- 每收到一个完整 box，回 `ack(id)`；同一 id 重复 ack 无副作用。
- 对象 id 出现缺口 → `pull(from=缺口)`。
- 对象内 `seq` 出现缺口 → 丢弃该对象，`pull(from=该对象 id)`。
- ack 只用于服务端观测投递进度，**不做断点续传**。

### 断线

```
断线 → 服务端清掉该连接；客户端清掉未结束对象
重连 → 客户端发 pull → 按 id 重建
```

### 消费端状态机

```java
// objects: id -> {type, content, open, lastSeq}
// expected: 下一个新对象 id
void onBox(Box b) {
    if (b.id() > expected) { pull(expected); return; }

    Obj existing = objects.get(b.id());
    if (b.id() < expected && existing != null && !existing.open()) return;

    ack(b.id());

    if (isNonNegativeInt(b.seq())) {
        if (b.seq() == 0) {
            objects.put(b.id(), new Obj(b.type(), b.content(), true, 0));
            expected = Math.max(expected, b.id() + 1);
        } else {
            if (existing == null || !existing.open() || b.seq() != existing.lastSeq() + 1) {
                pull(b.id()); return;
            }
            existing.append(b.content());
            existing.lastSeq(b.seq());
        }
    } else {
        if (existing != null) existing.close();
    }
}
```

### 服务端职责

- 维护对象表 `id -> {type, content}`，id 只增不复用。
- 分配 id、递增 seq、结束时发非负以外的值。
- `submit` 前检查是否有未结束对象：有则回 `error`，不分配 user 对象。
- `pull(from)` 按 id 升序重放，每个对象两条 box。
- 不知道 `type` 会被渲染成什么，也不保存 box 历史 —— 对象表本身就是渲染投影。

## 完整流程

```
连接建立
  客户端 → pull()
  服务端 → 历史对象（每个：完整内容 + 结束）
  客户端 → ack(id) × N

提交
  客户端 → submit("...")
  服务端 → 用户消息对象（完整）
  服务端 → 助手对象（流式 seq 0..n + 结束）
  客户端 → ack(id) × N

丢包
  客户端发现对象 id 或 seq 缺口 → pull(from=缺口)
  服务端 → 从缺口起重放

断线
  服务端清连接；客户端清未结束对象
  重连 → 回到「连接建立」
```

### 一轮对话的时序

```
客户端                服务端
  │  submit            │
  │───────────────────►│
  │                    │ 分配 id=0
  │◄── user  完整 ─────┤
  │                    │ 分配 id=1..3
  │◄── text  seq 0..n ─┤
  │◄── tool  seq 0..n ─┤
  │◄── text  seq 0..n ─┤
  │  ack(0..3)         │
  │───────────────────►│
```
