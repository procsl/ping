# 阶段 2 设计：对话核心

对应 `tasks.md` 阶段 2。本文档承载实现级细节。架构决策见 `../design.md` D1（BoxSink / Session / Completion）、D8（工具与 hook）、D9（持久化与重建）、D12（图片输入）。

## 2.1 Box 与对象

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

## 2.2 上游消息

### 消息模型

```
{ type, ...该类型自己的字段 }
```

- 只有 `type` 公共；Java 侧用 sealed interface + `@JsonTypeInfo` 实现判别联合。
- 与下游 box 的不对称是有意的：下游是统一分片流，上游是异构命令集。

### 类型与字段

| type | 字段 | 服务端行为 |
|---|---|---|
| `user` | `text`?、`images`?、`request_id`? | 校验 → 分配 id → 回送 user 对象 → 起一轮 |
| `confirm` | `request_id`、`decision` | 匹配挂起的确认 |
| `cancel` | 无 | 给未结束对象补结束 box，回到 idle |
| `ack` | `id` | 记录投递进度 |
| `pull` | `from`?（默认 0） | 等价于 `Session.history(from)` |

### 用户消息

```json
{ "type": "user", "text": "帮我看看这个文件", "images": ["a.png"] }
```

- `text` 与 `images` 的组合不在协议层限制，由后端按模型能力与上下文校验；不通过回 `error` 对象且不分配 id。
- `request_id` 可选；携带时按它去重，重试返回首次分配的对象。
- 上游字段是原始输入，下游 `user` 对象的 `content` 是显示投影，两者不要求相同。

## 2.3 Session 对象

Session 是**唯一编排者**：状态与行为都在对象内，不持有线程，也不假设调用方在哪个线程。

```
Session
  状态  sessionId / role / model / messages / objects / roundState
  端口  BoxSink（适配器注入）/ ChatMemory / ToolService / hook 链
  行为
    submit(msg) -> Completion   校验 -> 去重 -> 分配 id -> 回送 user 对象 -> 起一轮
    cancel()                    给未结束对象补结束 box -> roundState = idle
    history(fromId) -> boxes    按 id 升序重放
    ack(id)                     记录投递进度
    onToolCompleted(...)        工具线程回调入口
    awaitIdle(timeout)          等待整轮结束
    close()                     收尾并落盘
```

- **并发**：内部一把锁（`ReentrantLock`）保护 `roundState`、对象表与消息表；所有入口方法都加锁，工具线程回调 `onToolCompleted` 走同一入口。
- **生命周期**：`SessionManager` 按 `sessionId` 缓存实例；连接断开不销毁 Session，进程退出才销毁。
- **重启**：Session 是新建对象，`roundState = idle`，对象表由 `ObjectProjector` 从消息表重建。

### 创建

由**独立工厂**创建，不挂在配置对象上：

```java
SessionFactory.create(roleConfig, globalConfig, toolService, hooks, boxSink, history?)
```

- 不传 `history` → 新会话。
- 传 `history`（既有会话文件）→ 恢复消息表并重建对象表。

### 状态机

```
idle --submit(通过校验)--> running --全部工具处理完 + 对象全部结束--> idle
running --cancel--> idle
running --error--> idle（是否可继续由后端按错误类型判定）
```

不变量：**`idle` 时对象表中不存在未结束对象**。

## 2.4 Completion

`submit` **立即返回**，不阻塞调用线程：

```java
interface Completion {
    void subscribe(Consumer<Box> consumer);   // 持续投递直到本轮结束
    boolean await(Duration timeout);          // 阻塞到结束
    void cancel();                            // 等价 Session.cancel()
}
```

- CLI：`completion.await(...)` + 同步渲染（或阻塞遍历）。
- nvim/web：`subscribe(...)` 把 box 推给外壳。
- 一轮结束、取消、失败都会让 `subscribe` 终止并让 `await` 返回。

### 调用流程

```
CliAdapter / NvimAdapter / WebAdapter
   │ submit(userMessage)
   ▼
Session（加锁：校验 → 去重 → 分配 id → 回送 user 对象 → roundState=running）
   │ 交给执行器（虚拟线程）
   ▼
RoundRunner
   │ hook(发送前) → 调模型（流式）
   │ 每个增量 → hook(文本前后) → 对象表 → BoxSink
   │ 有 tool call → hook(工具前) → ToolService 取执行对象 → 执行
   │             → onToolCompleted → hook(工具后) → 下一轮请求
   │ 全部工具处理完 + 对象结束 → roundState=idle → 唤醒 awaitIdle()
   ▼
BoxSink → 适配器 → 渲染
```

- `RoundRunner` 只调用 `Session` 的公开方法，不直接改对象表。
- 工具可能在别的线程完成，`onToolCompleted` 负责把控制权交回。
- `cancel` 由适配器直接调 `Session.cancel()`，不经过 `RoundRunner`。

## 2.5 轮次完成判定

一轮结束的条件是**该轮发起的全部工具调用都处理完毕**（成功、失败、被拒、超时都算处理完），且所有对象已结束。

```
round.pending = 1 + 工具调用数        // 1 = 本轮主流程
每个动作结束 → pending-- 
pending == 0 → roundState = idle
```

- 模型停止输出**不算**结束 —— 还可能带着 tool call 继续。
- 计数器用并发安全结构，工具线程完成时也能递减。

## 2.6 错误

```json
{ "code": "provider_timeout", "status": "retry" }
```

- `code` 覆盖具体场景，由后端保证「同类错误稳定、处理方式不同的错误不同码」。
- `status` 表示处理方式（取值后续跟进），前端据此决定重试 / 停止 / 忽略。
- `error` 作为**一条消息记录落盘**，不进入 LLM 上下文。
- 处理后标记为已处理（隐藏，不删除）；重放时照常发送，客户端跳过渲染。
- 忙时拒绝：

```
id=5  text  "…还在输出…"        ← 未结束

客户端 ──submit("新问题")──►
服务端 ── box(id=6, type=error, seq=0, "{\"code\":\"busy\",\"status\":\"retry\"}")
       ── box(id=6, type=error, seq=-1, "")
```

上一轮未结束时**不取消、不排队**，直接回 `error`，不为该消息分配 user 对象。

## 2.7 持久化

### 会话文件（`roles/<name>/sessions/<session-id>.json`）

```json
{
  "session_id": "20261101153045123ABCDEFGHIJ",
  "role": "dev",
  "model": "claude_x",
  "messages": [
    { "type": "system",    "text": "..." },
    { "type": "user",      "text": "..." },
    { "type": "assistant", "text": "...", "tool_calls": [], "interrupted": false },
    { "type": "tool",      "tool_call_id": "...", "text": "..." },
    { "type": "error",     "code": "provider_timeout", "status": "retry", "handled": false }
  ]
}
```

- 只有 `messages` 落盘，**对象表不落盘**。
- `error` 是消息记录的一种，但不参与模型请求。
- 会话 id：`yyyyMMddHHmmssSSS` + 10 位大写随机字母（27 字符）；`--session` 传入值须匹配 `^[A-Za-z0-9]{1,64}$`。

### 写入时机

| 场景 | 时机 |
|---|---|
| user / tool / error | 产生即写 |
| 流式 assistant | 累计 500 字符写一次；距上次写盘超 5s 也写一次 |
| 一轮结束 | 再补一次 |

- 「临时文件 + 原子替换」，不产生半个 JSON。
- 未完成的 assistant 消息带 `interrupted: true`，正常结束时清除；重建时对象末尾追加 `[输出中断]`。

### Spring AI 接入

```java
ChatMemory memory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(new RoleFileRepository(roleDir))  // -> sessions/*.json
    .maxMessages(configuredWindow)
    .build();
```

- Spring AI 2.0 起 `CONVERSATION_ID` 必填。
- 会话按角色隔离；历史窗口只影响送入模型的消息，不影响落盘。

## 2.8 对象表重建

`ObjectProjector` 是唯一映射实现，实时与全量共用：

| 消息 | 对象数 | 说明 |
|---|---|---|
| `user` | 1 | 一条用户消息一个对象 |
| `assistant`（纯文本） | 1 | 文本一个对象 |
| `assistant`（带 tool_calls） | 1 + N | 文本一个对象，每个 tool call 各自一个对象 |
| `tool` | 0 | 并入对应 tool call 对象的后续内容 |
| `error` | 1 | 已处理的对象在重放时带 `handled` 标记 |
| `system` | 0 | 不进渲染 |

```java
project(messages) -> List<Object>   // 全量重建（加载时）
project(message)  -> List<Object>   // 增量（实时追加）
```

- 幂等：同一份消息表两次加载得到逐字段相同的对象表。
- 等效：流式对象的最终内容 = 全量重建的 `content`。

## 2.9 图片输入

图片是**输入侧一等能力**，与终端能力无关（见 `../design.md` D12）：

```
用户引用 /path/to/a.png
   → 校验可读（不存在或不可读 -> 明确错误，不发送残缺消息）
   → 复制到 ~/.lwl/images/<hash>.<ext>（同内容不重复复制）
   → 读取为 base64
   → 与文本一起组装为多模态用户消息（文本顺序不变）
```

- 只做「存在且可读」校验；格式与大小交给 provider 判断，provider 拒绝映射为 `error` 对象。
- 落盘目录复用配置根，图片可跨会话复用；文件名以内容哈希命名。
- 输出侧展示不在本变更范围。

## 2.10 重放、确认与断线

### history

```
history()        从头重放
history(fromId)  从指定对象 id 开始重放
```

- 与实时推送同一套 box，每个对象只发两条：`seq=0` 带完整内容，`seq<0` 结束。
- 重放使用对象原本的 id，不重新编号。
- 上游 `pull` 命令只是它的入口，只有一份实现。

```
history(from=1)
  ◄── box(id=1, type=text, seq=0, "我先读一下这个文件。")
  ◄── box(id=1, type=text, seq=-1, "")
  ◄── box(id=2, type=tool, seq=0, "{...完整工具调用...}")
  ◄── box(id=2, type=tool, seq=-1, "")
```

### ack 与丢包

- 每收到一个完整 box，回 `ack(id)`；重复 ack 无副作用。
- 对象 id 出现缺口 → `history(from=缺口)`。
- 对象内 `seq` 出现缺口 → 丢弃该对象，`history(from=该对象 id)`。
- ack 只用于观测投递进度，不做断点续传。

### 断线

```
断线 → 服务端清掉该连接；客户端清掉未结束对象与本地状态
重连 → 客户端发 pull → 按 id 重建
```

- 前端**不持久化任何状态**：进程重启后必然全量 `pull()`；同一进程内重连可用 `pull(from_id)`。

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
- `submit` 前检查 `roundState`：`running` 即回 `error`，不分配 user 对象。
- `history(from)` 按 id 升序重放，每个对象两条 box。

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
  服务端清连接；客户端清未结束对象与本地状态
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
