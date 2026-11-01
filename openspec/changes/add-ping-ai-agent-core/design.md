# Design

## Context

动机与范围见 `proposal.md`。此处只列影响方案的现状与约束：

- `ping-ai` 当前**只有 `pom.xml`**，没有 `src/`：既无 `@SpringBootApplication`、无 main、无 `AutoConfiguration.imports`，也没有任何 `ChatStreamService`（历史文件已在 `bf71424d` 删除）。
- 全仓库只有 `ping-distribute` 配置了 `spring-boot-maven-plugin`；根 pom 与 `ping-parent` 均无 repackage 配置，`ping-ai/pom.xml` 里的 `ping.standalone` 属性**没有任何插件消费**。
- `ping-apt` 中没有任何 `<Module>Launcher` 生成处理器 —— AGENTS.md 描述的该机制尚未存在。
- `ping-ai` 依赖 `ping-web`（拖入 `spring-boot-starter-web` 的 servlet 容器）与 `spring-ai-starter-model-openai`；`ping-jpa` 已移除。
- Spring Boot 4.1.1、Spring AI 2.0.1；Java 25；构建必须带数据库 profile。
- 平台为 Linux x86_64（WSL），终端图形能力未知（本变更不涉及）。
- 项目许可证为 MIT。

**决策编号沿用拆分前的 `design.md`（D1–D13）**，空缺编号属于兄弟变更：

| 编号 | 归属 |
|---|---|
| D1、D7、D8、D9、D10、D12、D13 | 本变更（部分收窄） |
| D2、D3、D4、D5、D6、D11 | `add-ping-ai-nvim-tui` |
| D10 的 web 部分 | `add-ping-ai-web-shell` |

## Goals / Non-Goals

**Goals:**

- 让 core 不知道传输与渲染，为后续两种外壳留出稳定的语义边界。
- 配置加载不耦合 Spring 生命周期，启动前即可完成。
- 全链路降级：配置损坏、角色缺失、模型引用未命中，都不得让启动崩溃。
- 核心行为全部可用单测与进程内断言验证，不需要外部进程或网络。

**Non-Goals:**

- 不做交互式渲染（Neovim）与 web 形态。
- 不实现 `ping-apt` 的 Launcher 生成能力（见 D10 的债务说明）。
- 不做图片的输出侧展示（本变更只有输入侧）。
- 不做多角色并发会话、会话搜索、导入导出。

## 阶段设计文档索引

本文件只承载架构决策（D1、D7–D10、D12、D13）：取舍理由、被否决的备选、跨阶段约束与不变量。实现级细节（字段表、示例文件、边界用例、接口契约）拆分到 `design/` 下的阶段文档：

| 阶段 | 文档 | 覆盖 tasks.md | 状态 |
|---|---|---|---|
| 0 | 骨架切片（无独立文档） | 最短可运行路径 | —— |
| 1 | `design/phase-1-config.md` | 基础设施与配置 | 已细化 |
| 2 | `design/phase-2-conversation.md` | 对话核心（含 Session / Completion） | 已细化 |
| 3 | `design/phase-3-tools.md` | 工具服务与 hook | 已细化 |
| 4 | `design/phase-4-cli.md` | 非交互式命令行 | 已细化 |
| 5 | `design/phase-5-entry.md` | 入口与可执行打包 | 已细化 |
| 6 | —— | 集成验收 | 无独立设计文档 |

约定：阶段文档与 `tasks.md` 的阶段编号一一对应，只承载「是什么」与「怎么验」；决策理由回写本文档对应的 D 条目，阶段文档改为引用。

## Decisions

### D1 分层：core + ports & adapters（本变更只交付第一个 adapter）

```
                     +--------------------------------------------------+
                     |  core  (不知传输, 不知渲染)                       |
                     |                                                  |
                     |  ConfigLoader   ~/.lwl/config.json -> GlobalConfig
                     |  RoleCatalog    roles/<name> 读取与校验           |
                     |  PromptSource   system.md / first.md              |
                     |  ToolService    工具查询 (按名称取执行对象)        |
                     |  HookChain      阶段化校验 (无状态, 可注入)        |
                     |  SessionStore   ChatMemoryRepository -> JSON      |
                     |  Session        状态 + 行为 (不持有线程)           |
                     |  Completion     一轮对话的 box 流                  |
                     |  ObjectProjector 消息 -> 对象表 (幂等)             |
                     |  BoxSink        对外交付的唯一出口                 |
                     +-----------------------+--------------------------+
                                             |
                                  +----------v-----------+
                                  | CliAdapter           |
                                  | 按 id 渲染 box        |
                                  | 零网络监听            |
                                  +----------------------+

  兄弟变更追加（不得改动 box 契约与对象 id 规则）：
    add-ping-ai-nvim-tui  -> NvimAdapter  (msgpack-rpc + lua 渲染器)
    add-ping-ai-web-shell -> WebAdapter   (HTTP + WebSocket)
```

**Box 契约**（渲染数据的唯一形态）

```
Box = { type, id, seq, content }
```

| 字段 | 规则 |
|---|---|
| `type` | 开放字符串。协议不定义取值，消费端自行分派；同一对象内不变 |
| `id` | 对象 id，会话内从 0 递增、不复用；渲染顺序 = id 升序，id 0 恒在顶端 |
| `seq` | 对象内片段序号：`0` 开始、`>0` 追加；**其余（负数、缺失、非法）一律表示该对象结束** |
| `content` | 字符串，不透明。结构化数据由后端序列化，消费端按 `type` 解释 |

- **允许多个对象同时推送**：不同 `id` 的 box 可以交错到达，对象之间只靠 `id` 排序。
- 对象有两种形态：流式对象（`seq` 0..n + 结束）与完整对象（`seq=0` 带完整内容 + 结束）。
- `pull(from_id)` 按 id 升序重放，每个对象两条 box，与实时推送同一套格式。
- 每个 box 收到后回 `ack(id)`；对象 id 或 `seq` 出现缺口即 `pull(from=缺口)`。
- 断线：服务端清连接，客户端清未结束对象，重连后 `pull`。

**上游消息**（客户端 → 服务端）

```
{ type, ...该类型自己的字段 }
```

| type | 字段 |
|---|---|
| `user` | `text`?、`images`?、`request_id`? |
| `confirm` | `request_id`、`decision` |
| `cancel` | 无 |
| `ack` | `id` |
| `pull` | `from`?（默认 0） |

- 只有 `type` 是公共字段，其余按类型增减 —— 与 box 的固定四字段形成有意的不对称：下游是统一分片流，上游是异构命令集。
- `request_id` 可选：携带时服务端按它去重，重复提交返回同一对象。
- 组合是否合法（例如只有图片没有文本）由后端按模型能力与上下文校验，协议不限制。

**Session 与 Completion**（会话对象是唯一的编排者）

```
Session                       状态 + 行为，不持有线程
  submit(msg) -> Completion   校验 -> 分配 id -> 回送 user 对象 -> 起一轮
  cancel()                    给未结束对象补结束 box -> 回到 idle
  history(fromId) -> boxes    重放（与上游 pull 同一实现）
  ack(id)                     记录投递进度
  onToolCompleted(...)        工具线程回调入口
  awaitIdle(timeout)          等待整轮结束
```

```java
interface Completion {
    void subscribe(Consumer<Box> consumer);   // 持续投递直到本轮结束
    boolean await(Duration timeout);          // 阻塞到结束
    void cancel();                            // 等价 Session.cancel()
}
```

- `submit` **立即返回** `Completion`，不阻塞调用线程；模型流、工具循环、落盘都在执行器里跑。
- `Session` 自己维护状态与并发安全（内部锁），**不绑定任何线程**；工具可能由别的线程执行，回调统一走 `onToolCompleted`。
- 一轮的完成条件是**该轮发起的全部工具调用都处理完毕**（含被拒绝的），而不是「模型停止输出」。
- 对象表不落盘：由 `ObjectProjector` 从消息表重建，重建幂等；实时推送与全量重建共用同一映射。
- `history(fromId)` 与上游 `pull` 是同一能力，只有一份实现。

**为什么不定义事件类型**：`message.delta`、`tool.finished` 这类事件会把 provider 的流式细节升格为协议。box 只回答「这是第几个对象的第几段、内容是什么」，渲染语义完全交给消费端。这也是 D3「Java 只见组件」在三种外壳上的统一。

**对象语义是三个变更之间的契约**。本变更冻结 box 结构与 id 规则；后续变更只允许在消费端实现各自的 `type` 分派，不得修改 box 结构。

**备选**：把 core 写成一个必须经过 HTTP 的服务、CLI 降级为 HTTP 客户端。
**否决理由**：非交互式不需要网络，强制绕一圈既慢又违背「纯命令行程序」的定位。

### D7 配置分层与目录结构

```
~/.lwl/
  config.json      全局：providers(key/url) + models 清单 + default_model + chat.max_history
  logs/            java 日志（交互形态下 java 不碰 stdio；本变更同样统一落盘）
  images/          图片输入落盘
  roles/<name>/
    system.md      系统提示词
    first.md       默认用户提示词
    role.json      模型引用 + 工具子集
    sessions/<session-id>.json
```

**配置优先级**

```
命令行 --xxx          最高   (Spring Boot 原生)
      ^
~/.lwl/config.json    次之   (GlobalConfigLoader 独立加载为 GlobalConfig 对象)
      ^
内置默认              最低
```

配置加载不耦合 Spring：`config.json` 由 `GlobalConfigLoader` 直接读取为 `GlobalConfig` 对象，不注册为 Spring `PropertySource`、不走 Spring `Environment`。命令行参数解析仍走 Spring Boot 原生机制，但 `GlobalConfig` 是独立的配置事实源，由 `SessionFactory` 显式持有并显式构造模型客户端（不依赖 `spring.ai.*` 自动配置读取）。这使配置加载可在 `SpringApplication.run()` 之前执行，不受 Spring 生命周期约束。

**推论**：既然不依赖 `spring.ai.*` 自动配置，依赖就不应停留在 starter 层。`spring-ai-starter-model-openai` 收窄为 `spring-ai-openai`，避免把 autoconfigure、observation、retry 一并拖入。

**`first.md` 的语义（本变更只实现第一个分支）**

```
core: 读 roles/<name>/first.md, 交出内容
              |
              v
非交互 CliAdapter：直接作为用户消息发出（无人可编辑）

交互 NvimAdapter（add-ping-ai-nvim-tui）：预填输入框，可编辑可清空
```

- 合并 `role.json`（而非拆成两份）是用户明确的选择：工具子集与模型引用同属「角色的运行配置」。
- 全链路降级 + 告警、启动永不失败，沿用 `ping-ui` 抽象组件规范的既有不变量。

`config.json` 字段表、角色名校验边界、`role.json` 示例、`GlobalConfigLoader` 与 `ModelCatalog` 职责契约见 `design/phase-1-config.md`；会话 JSON 结构见 `design/phase-2-conversation.md`。

### D8 工具来源、执行与 hook 机制

**工具来源**：`ToolService` 管理系统中已经开发好的工具，按名称查询到工具的执行对象。

```java
interface ToolService {
    List<ToolDescriptor> query(ToolQuery query);   // 初始化时按角色/会话条件查询
    ToolExecutor find(String name);                // 按名称取执行对象
}
```

- Session 初始化时查询一次，把结果**绑定进 LLM 的 tools 上下文**。
- 没有注册进上下文的工具，模型看不见，也就调不到 —— 这就是「白名单」，不另设第二份配置。
- 本变更提供代码内注册表的实现；将来接 MCP 或外部注册中心只换实现。

**执行**：关闭自动工具循环，手动驱动每次迭代 —— 官方文档明确 *"Each iteration is observable and interruptible"*。已核实（`spring-ai-client-chat-2.0.1` / `spring-ai-model-2.0.1`）：

- `AdvisorParams.toolCallingAdvisorAutoRegister(false)` 存在，落到 `ChatClientAttributes.TOOL_CALLING_ADVISOR_AUTO_REGISTER`。
- `ToolCallingManager.executeToolCalls(Prompt, ChatResponse)` 存在。
- `ChatMemoryRepository`、`MessageWindowChatMemory`、`ChatMessageMemoryAdvisor` 均存在。

**注意**：`ChatClientResponse` 没有 `hasToolCalls()`（那是 `ChatResponse` 的方法），且 `stream().chatClientResponse()` 返回 `Flux<ChatClientResponse>`。实现时以编译器为准。

**线程**：虚拟线程只负责发请求；工具调用**不绑定同一线程**，可以由其他线程执行。因此 `Session` 的轮次状态必须自己维护并发安全，工具结果统一回调 `Session.onToolCompleted`。

**Hook 机制**（替代「白名单硬编码」）

```java
enum Decision { PASS, REJECT, WAIT, REPLACE }

record HookResult(Decision decision, Object data) {}

interface Hook {
    HookResult apply(HookStage stage, HookContext ctx, Object data);
}
```

- 入参：**阶段名 + context + 完整数据**。流式阶段传入的是拼装完成的完整内容，不是单个片段。
- 返回：执行策略（通过 / 拒绝 / 等待 / 替换）。**部分阶段允许替换内容**，其余阶段返回 `REPLACE` 视为配置错误。
- **任意阶段都可以注入 hook**；具体阶段集合暂缓定义，只保留机制（见 Open Questions）。
- hook 无状态、不依赖 Spring，直接 new。
- 工具白名单是其中一个实现：**请求前**校验工具名与参数、**执行完成后**再校验结果归属，两次都通过才把结果送回模型，否则回「调用失败」。

**一轮的完成判定**：该轮发起的全部工具调用都处理完毕（含被拒绝、失败、超时）才算结束，模型停止输出不算。

**备选**：纯 observer 方案（只看不拦）—— 无法满足确认与白名单要求，否决。

### D9 会话持久化：消息落盘，对象表重建

实现 Spring AI 的 `ChatMemoryRepository` 接口写入 `roles/<name>/sessions/*.json`，配 `MessageWindowChatMemory` 提供窗口上限。这直接满足「会话记录全部存储在角色下」，无需自造记忆层。

**落盘的是消息表，不是对象表**。对象表是渲染投影，由 `ObjectProjector` 从消息表重建：

```
messages --(ObjectProjector, 幂等)--> objects --(BoxSink)--> 适配器
```

- 实时推送与全量重建共用同一映射规则：user 1 个、assistant 文本 1 个、每个 tool call 1 个、tool result 并入对应 tool 对象、system 0 个、error 1 个。
- 重建必须幂等：同一份消息表两次加载得到完全相同的对象表。

**写入时机**

| 场景 | 时机 |
|---|---|
| user / tool / error 消息 | 产生即写 |
| 流式 assistant | 累计 500 字符写一次；距上次写盘超 5s 也写一次 |
| 一轮结束 | 再补一次 |

写入采用「临时文件 + 原子替换」。未完成的 assistant 消息带 `interrupted: true`，正常结束时清除；重建时该对象末尾追加 `[输出中断]`。

**错误也落盘**：`error` 作为一条消息记录持久化（带 `code` 与处理方式），**不进入 LLM 上下文**；处理后被标记为已处理，重放时仍发送但客户端跳过渲染。只隐藏、不删除 —— 删除会让后续对象 `id` 位移，破坏重建幂等与 `history(fromId)`。

**会话标识**：`yyyyMMddHHmmssSSS` + 10 位大写随机字母（共 27 字符，无分隔符）。`--session` 传入值必须匹配 `^[A-Za-z0-9]{1,64}$`，不符合即参数错误，防止路径穿越。

注意 Spring AI 2.0 起 `CONVERSATION_ID` 为必填，缺失即抛错。会话按角色隔离：角色甲的会话对角色乙不可见。多角色并发不共享文件，不需要跨角色锁。

### D10 入口与可执行打包（含已知债务）

- **入口**：手写 `cn.procsl.ping.boot.ai.PingAiLauncher` 作为启动类。
- **打包**：在 `ping-ai/pom.xml` 配置 `spring-boot-maven-plugin` 的 `repackage`，显式指定 `mainClass`，使 `./mvnw -Ph2 -pl ping-ai package` 产出可执行 jar。
- **终端形态**：以 `WebApplicationType.NONE` 启动；`ping-web` 保留，但本变更不启动 servlet 容器。

**已知债务（必须在 design 中显式记录，不得隐去）**

| 约定（AGENTS.md） | 本变更的做法 | 后续处置 |
|---|---|---|
| 业务模块 `src/main` 不得自带 main | `PingAiLauncher` 手写在 `src/main` | `ping-apt` 实现 `<Module>Launcher` 生成后替换 |
| `mainClass` 由 `ping-apt` 生成 | 在 pom 中显式写死 | 同上 |
| 开关 `ping.standalone` 控制 repackage | 未引入该机制，仅本模块直接配置插件 | 与 Launcher 生成一起做成基础设施变更 |

**为什么不顺手实现 APT 生成**：该机制影响所有业务模块的打包方式，属于仓库级基础设施变更，塞进本变更会让核心能力被构建改造拖住。

### D12 图片：只做输入侧

```
输入侧 (一等能力)  粘贴/引用 -> 落盘 ~/.lwl/images/ -> base64 进模型   零终端依赖
输出侧           不在本变更范围（见 add-ping-ai-nvim-tui 的降级展示）
```

输入侧与终端能力无关：CLI 形态下图片只能通过文件路径引用传入，同样需要落盘与编码。因此它属于对话核心，而不是某个适配器。

### D13 模块内落点（包结构）

```
ping-ai/src/main/java/cn/procsl/ping/boot/ai/
  config/      配置根、config.json 加载、模型目录、角色目录与名校验
  prompt/      system.md / first.md
  tool/        工具服务（查询 + 按名取执行对象）、三段式选择
  hook/        hook 接口、阶段枚举、注册链、白名单实现
  session/     Session（状态 + 行为）、Completion、会话 JSON 仓库
  core/        轮次编排、ObjectProjector、BoxSink
  adapter/
    cli/       非交互式适配器与长参数解析
  PingAiLauncher          <- D10 的入口形态

  兄弟变更追加：
    adapter/nvim/  (add-ping-ai-nvim-tui)
    adapter/web/   (add-ping-ai-web-shell)
```

包边界即分层边界：`adapter/*` 只依赖 `core` 与各自的 box 编解码，互不依赖；`core` 不 import 任何 `adapter`。

## 覆盖对照：spec 需求 → 决策

| spec 需求 | 由哪个决策落实 |
|---|---|
| 系统提示词取自角色目录 / `first.md` 语义 | D7（本变更只做非交互分支） |
| 流式产出 / 多对象并发 / 调用失败 | D1（BoxSink）+ D8 |
| 会话对象 / Completion / 轮次完成判定 | D1（Session 与 Completion） |
| 用户消息回送 / 历史重放 / 丢包恢复 | D1（对象 id + history/ack） |
| 上游消息按类型分派 / request_id 幂等 | D1（上游消息） |
| 对象表重建幂等 / 流式与全量等效 | D9（ObjectProjector） |
| 图片作为模型输入 | D12 |
| 会话持久化于角色 / 窗口上限 / 写盘时机 | D9 |
| 模型配置来源全局 / 异常降级 | D7（GlobalConfigLoader + ModelCatalog） |
| 角色目录结构 / 校验 / 不暴露给模型 | D7 + D13（RoleCatalog） |
| 工具查询服务 / 三段式选择 | D8 + D13（ToolService） |
| 非交互白名单 / 交互确认 / hook 双校验 | D8（Hook 机制） |
| 非交互单次问答 / 长参数 / 无监听 / 退出状态 | D1 + D10 + D13（CliAdapter） |
| 可执行入口与独立运行 | D10 |

## Risks / Trade-offs

- **配置明文密钥**：`api_key` 为纯字符串、不支持环境变量引用 → 本地个人工具的取舍；若将来要共享机器，需要补环境变量或系统密钥环支持。
- **`~/.lwl` 是全局单例**：同一用户在多个项目里运行会共用角色与会话 → 当前接受，若成为问题再加项目级作用域。
- **Spring AI API 仍在演进**：2.0.1 的 advisor 命名存在 `toolCallingAdvisor` / `toolCallAdvisor` 两个入口 → 以编译与单测为准，不依赖文档措辞。
- **工具执行线程不受控**：工具可能在其他线程完成，轮次完成判定依赖计数器 → 状态机必须并发安全；异步执行器的死锁与超时由适配器侧兜底。
- **对象表重建与实时推送可能漂移**：两条路径共用 `ObjectProjector` 是唯一保障 → 用「全量 vs 增量结果逐字段相等」的单测锁死。
- **隐藏的 error 会累积**：只隐藏不删除是为了保住 `id` 连续 → 长期会话里会留下不再显示的记录，接受。
- **hook 阶段集合未定**：机制先落地，阶段枚举后续补齐 → 新增阶段不影响既有实现，但需要同步 spec。
- **确认策略的线程模型**：挂起等待外部回话需要明确的超时与调度 → 本变更先用测试替身固定语义，真实阻塞模型由 nvim 变更验证。
- **打包债务**：手写 Launcher 与 AGENTS.md 冲突 → 已在 D10 表格中登记，等基础设施变更偿还。

## Migration Plan

纯新增能力，`ping-ai` 之前没有任何可运行行为，无数据迁移与回滚需求。移除 `ping-jpa` 已完成；把 starter 收窄为 `spring-ai-openai` 后通过 `./mvnw -Ph2 -pl ping-ai test` 验证。

## Open Questions

以下均为可安全延后、且不改变 specs、方案与任务拆分的项：

- 消费端 `type` 词表（实现约定，不属于协议）：v1 预期为 `user` / `text` / `tool` / `confirm` / `error`。
- **hook 的阶段集合**：机制已定（阶段名 + context + 完整数据 → 策略），具体阶段枚举后续定义。
- **`error.status` 的取值集合**：字段先保留，取值后续跟进（当前只要求能表达处理方式）。
- 确认策略的挂起与超时实现。
- 工具执行器的默认并发度（同线程顺序 vs 并行）。
- `api_key` 是否支持环境变量引用。
- `~/.lwl` 是否需要按项目隔离。
- 入口是否改由 `ping-apt` 生成 `<Module>Launcher`（替换不影响外部行为）。
