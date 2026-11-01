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
| 1 | `design/phase-1-config.md` | 基础设施与配置 | 已细化 |
| 2 | `design/phase-2-conversation.md` | 对话核心 | 已迁移，待细化 |
| 3 | `design/phase-3-tools.md` | 工具注册与执行策略 | 已迁移，待细化 |
| 4 | `design/phase-4-cli.md` | 非交互式命令行 | 待细化 |
| 5 | `design/phase-5-entry.md` | 入口与可执行打包 | 待细化 |
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
                     |  ToolRegistry   全局工具清单 (代码内定义)          |
                     |  ToolPolicy     白名单 | 确认 (按形态注入)         |
                     |  SessionStore   ChatMemoryRepository -> JSON      |
                     |  AgentRuntime   ChatClient 组装 + 工具循环         |
                     |  ObjectTable    会话内对象表 (id -> type/content)  |
                     |  BoxStream      对外交付的唯一出口                 |
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

配置加载不耦合 Spring：`config.json` 由 `GlobalConfigLoader` 直接读取为 `GlobalConfig` 对象，不注册为 Spring `PropertySource`、不走 Spring `Environment`。命令行参数解析仍走 Spring Boot 原生机制，但 `GlobalConfig` 是独立的配置事实源，由 `AgentRuntime` 显式持有并显式构造模型客户端（不依赖 `spring.ai.*` 自动配置读取）。这使配置加载可在 `SpringApplication.run()` 之前执行，不受 Spring 生命周期约束。

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

### D8 工具执行：Spring AI 的可观测、可中断工具循环

关闭自动工具循环，手动驱动每次迭代 —— 官方文档明确 *"Each iteration is observable and interruptible"*，另有 `ToolCallObservingAdvisor` 可观测每个 tool-call chunk 并转发。**「挂起等确认」不需要自造对话循环**，这使确认策略的实现成本降一个档位。

已核实（`spring-ai-client-chat-2.0.1` / `spring-ai-model-2.0.1`）：

- `AdvisorParams.toolCallingAdvisorAutoRegister(false)` 存在，落到 `ChatClientAttributes.TOOL_CALLING_ADVISOR_AUTO_REGISTER`。
- `ToolCallingManager.executeToolCalls(Prompt, ChatResponse)` 存在。
- `ChatMemoryRepository`、`MessageWindowChatMemory`、`MessageChatMemoryAdvisor` 均存在。

**注意**：`ChatClientResponse` 没有 `hasToolCalls()`（那是 `ChatResponse` 的方法），且 `stream().chatClientResponse()` 返回 `Flux<ChatClientResponse>`。实现时以编译器为准，不要照抄阶段文档里的示意代码。

**备选**：纯 observer 方案（只看不拦）—— 无法满足交互式的确认要求，否决。

### D9 会话持久化：自定义 `ChatMemoryRepository`

实现 Spring AI 的 `ChatMemoryRepository` 接口写入 `roles/<name>/sessions/*.json`，配 `MessageWindowChatMemory` 提供窗口上限。这直接满足「会话记录全部存储在角色下」，无需自造记忆层。

注意 Spring AI 2.0 起 `CONVERSATION_ID` 为必填，缺失即抛错。

**并发与原子性**：同一角色的会话文件按会话标识隔离，写入采用「临时文件 + 原子替换」，避免流式过程中进程中断留下半个 JSON。多角色并发不共享文件，不需要跨角色锁。

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
  tool/        工具注册表、执行策略（白名单 / 确认）、三段式选择
  session/     角色目录下的会话 JSON 仓库
  core/        运行时编排、对象表与 box 流
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
| 流式产出 / 多对象并发 / 调用失败 | D1（BoxStream）+ D8 |
| 用户消息回送 / 历史重放 / 丢包恢复 | D1（对象 id + pull/ack） |
| 图片作为模型输入 | D12 |
| 会话持久化于角色 / 窗口上限 | D9 |
| 模型配置来源全局 / 异常降级 | D7（GlobalConfigLoader + ModelCatalog） |
| 角色目录结构 / 校验 / 不暴露给模型 | D7 + D13（RoleCatalog） |
| 全局工具清单 / 三段式选择 | D8 + D13（ToolRegistry / Resolver） |
| 非交互白名单 / 交互确认 / 过程可观测 | D8 |
| 非交互单次问答 / 长参数 / 无监听 / 退出状态 | D1 + D10 + D13（CliAdapter） |
| 可执行入口与独立运行 | D10 |

## Risks / Trade-offs

- **配置明文密钥**：`api_key` 为纯字符串、不支持环境变量引用 → 本地个人工具的取舍；若将来要共享机器，需要补环境变量或系统密钥环支持。
- **`~/.lwl` 是全局单例**：同一用户在多个项目里运行会共用角色与会话 → 当前接受，若成为问题再加项目级作用域。
- **Spring AI API 仍在演进**：2.0.1 的 advisor 命名存在 `toolCallingAdvisor` / `toolCallAdvisor` 两个入口 → 以编译与单测为准，不依赖文档措辞。
- **确认策略的线程模型**：在 Reactor 流里挂起等待外部回话需要明确的调度器与超时 → 本变更先用测试替身固定语义，真实阻塞模型由 nvim 变更验证。
- **打包债务**：手写 Launcher 与 AGENTS.md 冲突 → 已在 D10 表格中登记，等基础设施变更偿还。

## Migration Plan

纯新增能力，`ping-ai` 之前没有任何可运行行为，无数据迁移与回滚需求。移除 `ping-jpa` 已完成；把 starter 收窄为 `spring-ai-openai` 后通过 `./mvnw -Ph2 -pl ping-ai test` 验证。

## Open Questions

以下均为可安全延后、且不改变 specs、方案与任务拆分的项：

- 消费端 `type` 词表（实现约定，不属于协议）：v1 预期为 `user` / `text` / `tool` / `confirm` / `error`。
- 确认策略在 Reactor 流中的具体阻塞/超时实现。
- `api_key` 是否支持环境变量引用。
- `~/.lwl` 是否需要按项目隔离。
- 入口是否改由 `ping-apt` 生成 `<Module>Launcher`（替换不影响外部行为）。
