# Design

## Context

动机与范围见 `proposal.md`。此处只列影响方案的现状与约束：

- `ping-ai` 是空壳：`src/main` 下仅有一个空的 `ChatStreamService`，无 `@SpringBootApplication`、无 main、无 `AutoConfiguration.imports`。仓库里 `src/main` 唯一的 Boot 入口是 `ping-distribute` 的 `DistributeApplication`。
- **入口约定存在缺口**：`AGENTS.md` 要求"业务模块 `src/main` 不得自带 main，`mainClass` 由 `ping-apt` 生成 `<Module>Launcher`"，但 `ping-apt` 源码中没有任何生成 Launcher 的处理器，根 pom 与 `ping-parent` 也没有 `ping.standalone` / `repackage` 配置。即该机制尚未存在。
- `ping-ai` 当前依赖 `ping-web`（因此拖入 `spring-boot-starter-web` 的 servlet 容器）与 `ping-jpa`。
- Spring Boot 4.1.1、Spring AI 2.0.1；Java 25；构建必须带数据库 profile。
- 目标机器已确认**未安装 Neovim**，因此分发是本变更的一等子系统，而非环境假设。
- 平台为 Linux x86_64（WSL），终端图形能力未知。
- 项目许可证为 MIT。

## Goals / Non-Goals

**Goals:**

- 让 core 不知道传输与渲染，三种外壳（非交互 CLI、Neovim 交互、web）共用同一套消息语义。
- 让 Java 侧的视野停留在"组件"层面，渲染细节沉到 lua 与布局配置。
- 尽可能复用社区积木，把自研限制在协议、编排与粘合。
- 全链路降级：配置损坏、缺少 Neovim、终端不支持图片，都不得让启动失败。

**Non-Goals:**

- 不自研 TUI 渲染器，也不以"把 CLI 开在 nvim 终端 buffer 里"作为目标形态（可作为过程中的临时验证，但不写入本变更的任务）。
- 不做 treesitter parser 的编译与跨平台分发。
- 不做图片的像素级展示增强（协议探测、色块降级留待后续）。
- 不引入任何 GPL 许可代码。
- 不实现 `ping-apt` 的 Launcher 生成能力（见 Open Questions）。

## 阶段设计文档索引

本文件只承载架构决策（D1–D13）：取舍理由、被否决的备选、跨阶段约束与不变量。各阶段的实现级细节（字段表、示例文件、边界用例、接口契约、代码接入点）拆分到 `design/` 下的阶段文档：

| 阶段 | 文档 | 覆盖 tasks.md | 状态 |
|---|---|---|---|
| 1 | `design/phase-1-config.md` | 阶段 1 基础设施与配置 | 已细化 |
| 2 | `design/phase-2-conversation.md` | 阶段 2 对话核心 | 已迁移，待细化 |
| 3 | `design/phase-3-tools.md` | 阶段 3 工具注册与执行策略 | 已迁移，待细化 |
| 4 | `design/phase-4-cli.md` | 阶段 4 非交互式命令行 | 待细化 |
| 5 | `design/phase-5-nvim-dist.md` | 阶段 5 Neovim 分发与进程编排 | 已迁移，待细化 |
| 6 | `design/phase-6-nvim-ui.md` | 阶段 6 Neovim 组件层 | 已迁移，待细化 |
| 7 | `design/phase-7-entry.md` | 阶段 7 入口与双形态 | 已迁移，待细化 |
| 8 | `design/phase-8-integration.md` | 阶段 8 集成验收 | 待细化 |

约定：阶段文档与 `tasks.md` 的阶段编号一一对应，只承载"是什么"与"怎么验"；决策理由回写本文档对应的 D 条目，阶段文档改为引用。

## Decisions

### D1 分层：core + ports & adapters，三种传输

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
                     |  EventStream    对外交付的唯一出口                 |
                     +-----------------------+--------------------------+
                                             |
      +--------------------------------------+--------------------------+
      |                                      |                          |
+-----v--------------+          +------------v-----------+    +-----------v---------+
| CliAdapter         |          | NvimAdapter           |    | WebAdapter          |
| 非交互式            |          | 交互式                 |    | --web / 被依赖打包   |
| 直接订阅 EventStream|          | spawn nvim --clean    |    | HTTP + WebSocket    |
| 零网络监听          |                --listen        |    | over TCP            |
+--------------------+          | java 不碰 stdio        |    +----------+----------+
                                | msgpack-rpc 双向       |
                                +------------+-----------+
                                             |
                                +------------v-----------+
                                | lua 组件层 (~/.lwl/nvim)|
                                | nui + nui-components   |
                                | render-markdown        |
                                +------------+-----------+
                                             |
                                +------------v-----------+
                                | nvim 渲染引擎           |
                                | --clean (不读用户配置)  |
                                | syntax 脚本 (runtimepath)|
                                +------------------------+
```

**同一事件在三种传输下的形态**（语义相同、编码不同）：

| 事件 | CliAdapter | NvimAdapter | WebAdapter |
|---|---|---|---|
| 增量片段 | 直接写 stdout | rpc 通知 → lua 写入行缓冲 | WS frame `{"type":"delta",...}` |
| 工具确认请求 | 不适用（走白名单） | rpc 请求 → 弹确认框 → 回响应 | WS 双向请求/响应 |
| 提交输入 | 不适用（参数传入） | lua `rpcnotify` → Java | WS 客户端上行 |

**备选**：把 core 写成一个必须经过 HTTP 的服务、CLI 降级为 HTTP 客户端。
**否决理由**：非交互式不需要网络，强制绕一圈既慢又违背"纯命令行程序"的定位；而三种外壳共用同一套**消息语义**已经足以满足"将来一套接口"的诉求，不必强求同一套**传输**。

### D2 进程模型：agent 是宿主，`nvim --clean --listen`

```
java(宿主)
  ├─ spawn: nvim --clean --listen <addr>
  │            ├─ 继承 java 的 stdin/stdout/stderr → nvim 直接在真终端全屏渲染
  │            └─ --listen 开一条 msgpack-rpc（unix socket；Windows 用 named pipe，兜底 TCP loopback）
  ├─ java 全程不碰 stdio，日志落 ~/.lwl/logs/
  └─ 双向 rpc：java→nvim 调 lua 组件层；nvim→java 发事件（submit/cancel/approve/leave）
```

- `--clean` 正是"不读用户配置、不加载用户插件、但保留内置 runtime 与 Vim 默认按键"的语义，一次参数同时满足两项要求。
- 选 unix socket 上的 msgpack-rpc 而非 HTTP/WS，**恰好消解了"纯命令行不启动 web 服务"与"交互式需要通信"的矛盾**：unix socket 不属于 web 服务，`--web` 才起 TCP 监听。

启动时序与双向调用协议见 `design/phase-5-nvim-dist.md`。

**备选 A**：`nvim --embed` + `nvim_ui_attach`，由 Java 自己消费 grid 事件画到终端 —— 等于实现一个 GUI 客户端，比自研 TUI 更重，否决。
**备选 B**：为 nvim 分配 pty、业务走 WS —— 引入 pty 库（JNA/pty4j）与 resize、信号处理的复杂度，且对本地父子进程用 WS 属过度设计，否决。
**备选 C**：nvim 只当渲染壳、把 CLI 开在其 terminal buffer 里 —— 零成本但 Java 仍在"输出文本"，组件协议根本不会被设计出来，否决（见 Non-Goals）。

### D3 交互层 = 组件库原语

**视野对比** —— 这是本决策的实质：

```
若直接调 nvim API                     本设计调组件语义
-------------------------            -------------------------
nvim_buf_set_lines(...)              component.append(msg_id, chunk)
nvim_open_win(...)                   component.create("chat", {...})
nvim_buf_set_extmark(...)            component.emit("submit", {text})
nvim_set_keymap(...)                 (Java 不知道 buffer/window 存在)
   ^                                     ^
   Java 必须懂编辑器模型                  改渲染只改 lua 与布局配置
```

社区库选型（全部 MIT）与 `~/.lwl/nvim/` 目录布局见 `design/phase-6-nvim-ui.md`。

**备选**：改造现成聊天插件的 UI 当渲染层 —— 撞 GPL 或与上游结构强耦合，否决。

### D4 渲染必须走两条路径

```
对话消息流  → append-only 行缓冲（只重画当前流式消息的尾部）
布局 / 弹层 → 组件状态（nui-components 的 state）
```

**依据**：`nui` 的树组件是整树重绘（`tree:render()`）。把对话流塞进一棵树、每来一个 token 就整体重绘，长对话必然退化。**行缓冲与组件状态是两条不同的渲染路径，不得混用**。

### D5 流式写入纪律

流式写入必须遵守四条纪律（不得产生超长行、禁 undo、避免 legacy syntax 开销、自动命令不挂重活），全部来自真实事故而非推测，逐条依据与对应 issue 见 `design/phase-6-nvim-ui.md`。

按既定策略：**默认使用开源组件，出现性能问题再针对性开发**，不设前置探针。

### D6 代码高亮：syntax 脚本 + `runtimepath` 注入，不做 parser 分发

- 官方 `vim_diff.txt` Defaults："Filetype detection is enabled by default. Syntax highlighting is enabled by default."
- `:syntax on` 实为 `:source $VIMRUNTIME/syntax/syntax.vim`，而 `$VIMRUNTIME` 是 nvim 内置，`--clean` 不屏蔽它。
- syntax 加载走 `runtime! syntax/<name>.vim`，**搜索整个 `runtimepath`** → 把自有高亮脚本放进自有配置目录并注入 `runtimepath` 即可覆盖或补充内置脚本。

**否决的路线**：预编译 treesitter parser 随包分发 —— nvim 内置 parser 只覆盖 render-markdown 必需的 markdown，其余语言一个都没有；`:TSInstall` 强制要求 tree-sitter CLI + C 编译器 + git，与"下载一个 nvim 就能用"的分发模型冲突。完整证据链见 `design/phase-6-nvim-ui.md`。

### D7 配置分层与目录结构

```
~/.lwl/
  config.json      全局：providers(key/url) + models 清单 + default_model
  bin/nvim         分发的 Neovim
  logs/            java 日志（java 不碰 stdio）
  nvim/            lua 组件层 + 布局 + syntax 高亮脚本
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

**`first.md` 在两个适配器的分叉**（core 只提供内容，如何使用由适配器决定）

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

- 合并 `role.json`（而非拆成两份）是用户明确的选择：工具子集与模型引用同属"角色的运行配置"。
- 全链路降级 + 告警、启动永不失败，沿用 `ping-ui` 抽象组件规范的既有不变量。

`config.json` 字段表、角色名校验边界、`role.json` 示例、`GlobalConfigLoader` 与 `ModelCatalog` 职责契约见 `design/phase-1-config.md`；会话 JSON 结构见 `design/phase-2-conversation.md`。

### D8 工具执行：Spring AI 的可观测、可中断工具循环

关闭自动工具循环，手动驱动每次迭代 —— 官方文档明确 *"Each iteration is observable and interruptible"*，另有 `ToolCallObservingAdvisor` 可观测每个 tool-call chunk 并转发。**"挂起等确认"不需要自造对话循环**，这使确认策略的实现成本降一个档位。代码接入点见 `design/phase-3-tools.md`。

**备选**：纯 observer 方案（只看不拦）—— 无法满足交互式的确认要求，否决。

### D9 会话持久化：自定义 `ChatMemoryRepository`

实现 Spring AI 的 `ChatMemoryRepository` 接口写入 `roles/<name>/sessions/*.json`，配 `MessageWindowChatMemory` 提供窗口上限。这直接满足"会话记录全部存储在角色下"，无需自造记忆层。

注意 Spring AI 2.0 起 `CONVERSATION_ID` 为必填，缺失即抛错。

### D10 入口与双形态

- **入口缺口的处理**：为 `ping-ai` 提供 `<Module>Launcher` 形态的入口（即约定中所说的 `PingAiLauncher`），使"业务模块不自带 main"的约定在未来由 `ping-apt` 自动生成时可直接替换。这是本变更最小代价的落点，不在本变更内实现 APT 生成。
- 终端形态以 `WebApplicationType.NONE` 启动，web 形态以 `SERVLET` 启动；由 `AutoConfiguration.imports` 登记的自动配置在"被作为依赖打包"时提供 web 服务能力，与 `--web` 共用同一套监听配置。
- 依赖调整：**移除 `ping-jpa`**（会话是 JSON 文件，不需要数据库）；保留 `ping-web` 但终端形态不启动 servlet 容器。

细节见 `design/phase-7-entry.md`。

### D11 Neovim 分发子系统

agent 自行下载 `nvim` 到 `~/.lwl/bin/` 并以 `--clean --listen` 启动，版本锁定，不跟随系统或最新版。ImageMagick 等可选外部工具若将来需要，复用同一套分发机制，不另建管线。

分发步骤与平台矩阵见 `design/phase-5-nvim-dist.md`。

### D12 图片

```
输入侧 (一等能力)  粘贴/引用 -> 落盘 ~/.lwl/ -> base64 进模型   零终端依赖
输出侧 (降级)      不显示像素, 只显示文件名 = markdown 引用文本的原生行为
```

**被否决的展示路线**（供后续参考，不在本变更）：kitty protocol（需 kitty>=28 / WezTerm 性能差）、Sixel（需 XTerm/foot 等 + ImageMagick 编译带 sixel）、OSC 1337（仅 iTerm2/Konsole/WezTerm，靠环境变量启发式探测）、色块降级（需 ImageMagick 读像素）。共同依赖 ImageMagick —— 若将来要做，复用 D11 的分发机制。

不装图片插件时 buffer 中本就是 markdown 引用文本，天然满足；将来要做展示只是往上叠加一层，不影响现有结构。

### D13 模块内落点（包结构）

```
ping-ai/src/main/java/cn/procsl/ping/boot/ai/
  config/      配置根、config.json 加载、模型目录、角色目录与名校验
  prompt/      system.md / first.md
  tool/        工具注册表、执行策略（白名单 / 确认）、三段式选择
  session/     角色目录下的会话 JSON 仓库
  core/        运行时编排、事件流与事件类型
  adapter/
    cli/       非交互式适配器与长参数解析
    nvim/      Neovim 启动器、msgpack-rpc 客户端、交互式适配器
    web/       web 适配器、WS 通道、自动配置
  PingAiLauncher          <- D10 的入口形态

~/.lwl/nvim/             <- lua 组件层 (见 D3)
```

包边界即分层边界：`adapter/*` 只依赖 `core` 与各自的事件编解码，互不依赖；`core` 不 import 任何 `adapter`。具体类名见各阶段设计文档。

### 覆盖对照：spec 需求 → 决策

| spec 需求 | 由哪个决策落实 |
|---|---|
| 系统提示词取自角色目录 / `first.md` 语义 | D7 + D3（两个适配器分叉） |
| 流式增量产出 / 调用失败事件 | D1（EventStream）+ D8 |
| 会话持久化于角色 / 窗口上限 | D9 |
| 模型配置来源全局 / 异常降级 | D7（GlobalConfigLoader + ModelCatalog） |
| 角色目录结构 / 校验 / 不暴露给模型 | D7 + D13（RoleCatalog） |
| 全局工具清单 / 三段式选择 | D8 + D13（ToolRegistry / Resolver） |
| 非交互白名单 / 交互确认 / 过程可观测 | D8 |
| 非交互单次问答 / 长参数 / 无监听 | D1 + D13（CliAdapter） |
| 交互渲染引擎（`--clean` 保留 Vim 键位） | D2 |
| Neovim 自动获取 | D11 |
| 双形态打包 / web 消息语义一致 | D1 + D10 |
| 图片输入与文件名降级 | D12 |

## Risks / Trade-offs

- **流式性能未知** → 已有 D5 纪律兜底；按既定策略先用开源组件，出现卡顿再针对性优化。代价是可能在首次端到端联调时才暴露。
- **引入 Lua 语言栈**（Java 项目新增一层 Lua）→ 把 Lua 限制为薄粘合：布局与渲染用社区库，业务编排留在 Java；不把状态机写进 Lua。
- **legacy syntax 长 buffer 开销**（`#17862`）→ 对话 buffer 一般达不到触发量级；若触发，针对性处理（例如仅对代码块范围启用 syntax、历史区关闭）。
- **Windows 的 unix socket 不可用** → `--listen` 退回 named pipe，再退 TCP loopback；代价是 Windows 上多一层例外路径。
- **`--clean` 屏蔽用户已装的高亮与主题插件** → 这是"不受用户配置干扰"的必然结果，取一致性而放弃复用；不提供逃逸开关。
- **`nvim --listen` 与 nvim 版本行为差异** → 分发时锁定 nvim 版本，不跟随系统或最新版。
- **图片无展示能力** → 显示文件名，已由 spec 固化为可接受行为。
- **GPL 生态不可复用** → 只借架构；代价是部分成熟聊天 UI 无法直接拿来，由 D3 的组件库路线补足。

## Migration Plan

纯新增能力，`ping-ai` 之前没有任何可运行行为，无数据迁移与回滚需求。唯一需注意的是移除 `ping-jpa` 依赖可能影响既有构建配置，通过 `./mvnw -Ph2 -pl ping-ai test` 验证；若外部有未记录的引用，回退该依赖即可。

## Open Questions

以下均为可安全延后、且不改变 specs、方案与任务拆分的项：

- 事件流的具体字段与分片格式（用户明确要求暂缓，兜底形态为细事件流）。D1 只固定了**事件类型**（见 `phase-2-conversation.md`），未涉及字段。
- 组件类型清单（`chat_panel` / `message` / `tool_call` / `input` / `confirm` 等）的确切命名与属性 —— 实现 lua 层时确定。D3 只固定了**布局区域**与目录结构，未写死 type 名。
- 图片输出侧的展示增强（终端图形协议探测、色块降级）。
- 入口是否改由 `ping-apt` 生成 `<Module>Launcher`（本变更按 D10 手写同名形态，替换不影响外部行为）。
- 是否为用户预留 `~/.lwl/nvim-plugins/` 之类的插件扩展口（当前决定是不留，`--clean` 保证完全一致）。
