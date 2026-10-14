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

## Decisions

### D1 分层：core + ports & adapters，三种传输

```
core (ModelConfig / RoleCatalog / PromptSource / ToolRegistry /
      ToolPolicy / SessionStore / AgentRuntime)
  │  对外交付：事件流
  ├── CliAdapter      非交互：直接消费事件流，零监听
  ├── NvimAdapter     交互：msgpack-rpc over unix socket
  └── WebAdapter      --web / 被依赖打包：HTTP + WebSocket over TCP
```

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

**备选 A**：`nvim --embed` + `nvim_ui_attach`，由 Java 自己消费 grid 事件画到终端 —— 等于实现一个 GUI 客户端，比自研 TUI 更重，否决。
**备选 B**：为 nvim 分配 pty、业务走 WS —— 引入 pty 库（JNA/pty4j）与 resize、信号处理的复杂度，且对本地父子进程用 WS 属过度设计，否决。
**备选 C**：nvim 只当渲染壳、把 CLI 开在其 terminal buffer 里 —— 零成本但 Java 仍在"输出文本"，组件协议根本不会被设计出来，否决（见 Non-Goals）。

### D3 交互层 = 组件库原语（A 方案）

```
java --组件语义(create/patch/append/emit)--> lua 组件层 --nui--> buffer/window
      java 不知道什么是 buffer
```

**社区积木**（许可证已核）：`nui.nvim`（blocks: text/line/table/tree；components: layout/popup/input/menu/split）、`nui-components.nvim`（在 nui 之上提供状态管理、表单校验、layout-engine、树）、`render-markdown.nvim`（extmark 就地渲染 markdown）。三者均 MIT，与本项目兼容。
**参考先例**：`sllm.nvim`（Apache-2.0，后端是外部 CLI、插件只做 UI 编排、UI 全用社区库，约 500 行 Lua）证明了这条路线的可行性；CopilotChat 官方 wiki 推荐"聊天 buffer + render-markdown"，说明该组合已被验证。
**GPL 生态**（CopilotChat 等）只借鉴架构思路，不引入代码。

**备选**：改造现成聊天插件的 UI 当渲染层 —— 撞 GPL 或与上游结构强耦合，否决。

### D4 渲染必须走两条路径

```
对话消息流  → append-only 行缓冲（按行写入递增的行号，只重画当前流式消息的尾部）
工具卡片    → 同一 buffer 内的折叠段（或仅让树组件管理侧栏等小结构）
布局 / 弹层 → 组件状态（nui-components 的 state）
```

**依据**：`nui` 的树组件是整树重绘（`tree:render()`）。把对话流塞进一棵树、每来一个 token 就整体重绘，长对话必然退化。**行缓冲与组件状态是两条不同的渲染路径，不得混用**。

### D5 流式写入纪律（来自真实事故，非推测）

1. **不得产生超长行**。`neovim#34019`：单行 80 万字符 + wrap 导致 100% CPU 挂死。因此 token 必须攒批、按换行切块后一次性写入，且单行长度有上限，超出即折行。
2. `undolevels = -1`（官方 `vi_diff`："Set it to -1 for no undo at all"），流式写入不进 undo 树。
3. 避免 legacy regex syntax 在超大 buffer 上的导航开销（`neovim#17862`，`--clean` 下同样复现）；长历史靠折叠收缩而非无界增长。
4. 高频变更触发的自动命令代价高（官方对 `TextChangedT` 的告警），不挂重活。

按既定策略：**默认使用开源组件，出现性能问题再针对性开发**，不设前置探针。

### D6 代码高亮：syntax 脚本 + `runtimepath` 注入，不做 parser 分发

- 官方 `vim_diff.txt` Defaults："Filetype detection is enabled by default. Syntax highlighting is enabled by default."
- `:syntax on` 实为 `:source $VIMRUNTIME/syntax/syntax.vim`，而 `$VIMRUNTIME` 是 nvim 内置，`--clean` 不屏蔽它。
- syntax 加载走 `runtime! syntax/<name>.vim`，**搜索整个 `runtimepath`** → 把我们自己的高亮脚本放进自有配置目录并注入 `runtimepath` 即可覆盖或补充内置脚本，`after/syntax/` 用于追加。
- 内置 markdown syntax 支持 `g:vimsyn_embed` 开启嵌入语言高亮（默认仅 Lua），可用于代码块内的语言染色。

**否决的路线**：预编译 treesitter parser 随包分发。虽然质量最好，但它需要"平台 × 架构 × 语言"的编译矩阵与包体积代价，而 `:TSInstall` 运行时又要求 tree-sitter CLI + C 编译器 + git —— 与"下载一个 nvim 就能用"的分发模型冲突。

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
    role.json      { "model": "<全局模型名>", "tools": ["<全局工具名>", ...] }
    sessions/<session-id>.json
```

- `config.json` 在启动早期读入并**转为 Spring Environment 中高优先级的 PropertySource（仅次于命令行）**。这样 `spring.ai.*` 等自动配置原生可读，凭据留在 JSON 里，命令行仍可覆盖 —— 两条配置线不打架。
- JSON 字段一律 `snake_case`（仓库约定）。`role.json` 的 `model` 存**名字**而非配置，`tools` 为字符串数组（将来需要传参时再升级为对象，字符串与对象可同时接受，向后兼容）。
- 合并 `role.json`（而非拆成两份）是用户明确的选择：工具子集与模型引用同属"角色的运行配置"。
- `first.md` 语义：**非交互式**未提供用户提示词时以其内容发起对话；**交互式**启动时回显到输入框（可编辑可清空）。核心只提供内容，如何使用由适配器决定。
- 全链路降级 + 告警、启动永不失败，沿用 `ping-ui` 抽象组件规范的既有不变量。

### D8 工具执行：Spring AI 的可观测、可中断工具循环

```java
.advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false))   // 关掉自动循环
while (response.hasToolCalls()) {
    // 此处即"每次迭代 observable and interruptible"
    // 白名单外 -> 发确认事件挂起，等适配器回话
    toolCallingManager.executeToolCalls(prompt, response);
}
```

**依据**：官方文档明确 *"Each iteration is observable and interruptible"*，另有 `ToolCallObservingAdvisor` 可观测每个 tool-call chunk 并转发。**"挂起等确认"不需要自造对话循环**，这使确认策略的实现成本降一个档位。
**备选**：纯 observer 方案（只看不拦）—— 无法满足交互式的确认要求，否决。

### D9 会话持久化：自定义 `ChatMemoryRepository`

实现该接口写入 `roles/<name>/sessions/*.json`，配 `MessageWindowChatMemory` 提供窗口上限。这直接满足"会话记录全部存储在角色下"，无需自造记忆层。注意 Spring AI 2.0 起 `CONVERSATION_ID` 为必填，缺失即抛错。

### D10 入口与双形态

- **入口缺口的处理**：为 `ping-ai` 提供 `<Module>Launcher` 形态的入口（即约定中所说的 `PingAiLauncher`），使"业务模块不自带 main"的约定在未来由 `ping-apt` 自动生成时可直接替换。这是本变更最小代价的落点，不在本变更内实现 APT 生成。
- 终端形态以 `WebApplicationType.NONE` 启动，web 形态以 `SERVLET` 启动；由 `AutoConfiguration.imports` 登记的自动配置在"被作为依赖打包"时提供 web 服务能力，与 `--web` 共用同一套监听配置。
- 依赖调整：**移除 `ping-jpa`**（会话是 JSON 文件，不需要数据库）；保留 `ping-web` 但终端形态不启动 servlet 容器。

### D11 Neovim 分发子系统

首次需要时下载到 `~/.lwl/bin/`，需处理：平台矩阵（linux/mac × x64/arm64，Windows 兜底）、版本锁定、校验和、以及 lua 插件与布局和高亮脚本的落位。`--clean` 下通过显式 `runtimepath` 指向 `~/.lwl/nvim/`。
ImageMagick 等可选外部工具若将来需要，复用同一套分发机制，不另建管线。

### D12 图片

输入侧（粘贴/引用 → 落盘 → base64 交给模型）不依赖终端能力，是一等功能；输出侧**只显示文件名** —— 不装图片插件时 buffer 中本就是 markdown 引用文本，天然满足，将来要做展示只是往上叠加一层，不影响现有结构。

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

- 事件流的具体字段与分片格式（用户明确要求暂缓，兜底形态为细事件流）。
- 组件类型清单（`chat_panel` / `message` / `tool_call` / `input` / `confirm` 等）的确切命名与属性 —— 实现 lua 层时确定。
- 图片输出侧的展示增强（终端图形协议探测、色块降级）。
- 入口是否改由 `ping-apt` 生成 `<Module>Launcher`（本变更按 D10 手写同名形态，替换不影响外部行为）。
- 是否为用户预留 `~/.lwl/nvim-plugins/` 之类的插件扩展口（当前决定是不留，`--clean` 保证完全一致）。
