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
                     +--------------------------------------------------+
                     |  core  (不知传输, 不知渲染)                          |
                     |                                                  |
                     |  ConfigLoader   ~/.lwl/config.json -> PropertySrc |
                     |  RoleCatalog    roles/<name> 读取与校验            |
                     |  PromptSource   system.md / first.md             |
                     |  ToolRegistry   全局工具清单 (代码内定义)           |
                     |  ToolPolicy     白名单 | 确认 (按形态注入)           |
                     |  SessionStore   ChatMemoryRepository -> JSON      |
                     |  AgentRuntime   ChatClient 组装 + 工具循环          |
                     |  EventStream    对外交付的唯一出口                  |
                     +-----------------------+--------------------------+
                                             |
      +--------------------------------------+--------------------------+
      |                                      |                          |
+-----v--------------+          +------------v-----------+    +-----------v---------+
| CliAdapter         |          | NvimAdapter           |    | WebAdapter          |
| 非交互式            |          | 交互式                 |    | --web / 被依赖打包    |
| 直接订阅 EventStream|          | spawn nvim --clean    |    |                     |
| 零网络监听          |          |        --listen       |    | HTTP + WebSocket    |
+--------------------+          | java 不碰 stdio        |    | over TCP            |
                                | msgpack-rpc 双向       |    +----------+----------+
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
                                | --clean (不读用户配置)   |
                                | syntax 脚本 (runtimepath)|
                                +------------------------+
```

**同一事件在三种传输下的形态**（语义相同、编码不同）：

| 事件 | CliAdapter | NvimAdapter | WebAdapter |
|---|---|---|---|
| 增量片段 | 直接写 stdout | rpc 通知 → lua 写入行缓冲 | WS frame `{"type":"delta",...}` |
| 工具确认请求 | 不适用（走白名单） | rpc 请求 → 弹确认框 → 回响应 | WS 双向请求/响应 |
| 提交输入 | 不适用（参数传入） | lua `rpcnotify` → Java | WS 客户端上行 |

**事件类型清单**（字段与分片格式见 Open Questions，此处只定类型）：

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

**交互式启动时序**：

```
$ ping-ai --role dev
  │
  ├─ 1. 解析长参数 (--role/--tools/--session/--model/--web)
  ├─ 2. 读 ~/.lwl/config.json -> PropertySource
  ├─ 3. 读 roles/dev/{system.md,first.md,role.json}   (校验 role-name)
  ├─ 4. 确认 Neovim 存在, 缺失则按 D11 下载到 ~/.lwl/bin/
  ├─ 5. 生成唯一 addr (~/.lwl/nvim/<pid>.sock)
  ├─ 6. spawn  nvim --clean --listen <addr>
  ├─ 7. 注入 runtimepath -> ~/.lwl/nvim/  (lua 组件层 + 布局 + syntax 脚本)
  ├─ 8. 连接 <addr>, 建立双向 msgpack-rpc
  ├─ 9. lua 组件层初始化布局, 上报 ready
  ├─10. first.md 内容 -> 预填输入框
  ├─11. 进入事件循环
  └─12. nvim 退出 -> 停事件流, 收尾, 进程结束
        (java 全程不写 stdout, 日志在 ~/.lwl/logs/)
```

**双向调用形态**：

```
java -> lua    请求/响应:  经 nvim 的 API 转到 lua 组件层
                          nvim_exec_lua("return require('agent').call(...)", {json})
                          用于 create/patch/append 等下行组件操作

lua  -> java   通知/请求:  vim.rpcnotify(chan_id, "agent.event", payload)
                          用于 submit / cancel / tool.approve 等上行事件
                          chan_id 即 --listen 建立的那个 channel
```

- `--clean` 正是"不读用户配置、不加载用户插件、但保留内置 runtime 与 Vim 默认按键"的语义，一次参数同时满足两项要求。
- 选 unix socket 上的 msgpack-rpc 而非 HTTP/WS，**恰好消解了"纯命令行不启动 web 服务"与"交互式需要通信"的矛盾**：unix socket 不属于 web 服务，`--web` 才起 TCP 监听。

**备选 A**：`nvim --embed` + `nvim_ui_attach`，由 Java 自己消费 grid 事件画到终端 —— 等于实现一个 GUI 客户端，比自研 TUI 更重，否决。
**备选 B**：为 nvim 分配 pty、业务走 WS —— 引入 pty 库（JNA/pty4j）与 resize、信号处理的复杂度，且对本地父子进程用 WS 属过度设计，否决。
**备选 C**：nvim 只当渲染壳、把 CLI 开在其 terminal buffer 里 —— 零成本但 Java 仍在"输出文本"，组件协议根本不会被设计出来，否决（见 Non-Goals）。

### D3 交互层 = 组件库原语（A 方案）

**视野对比** —— 这是本决策的实质：

```
若直接调 nvim API                     本设计调组件语义
-------------------------            -------------------------
nvim_buf_set_lines(...)              component.append(msg_id, chunk)
nvim_open_win(...)                   component.create("chat", {...})
nvim_buf_set_extmark(...)            component.emit("submit", {text})
nvim_set_keymap(...)                 (Java 不知道 buffer/window 存在)
   ^                                     ^
   Java 必须懂编辑器模型                   改渲染只改 lua 与布局配置
```

**社区库清单**（全部 MIT，与本项目 MIT 兼容）：

| 库 | 许可 | 提供 | 在本设计中的角色 |
|---|---|---|---|
| `nui.nvim` | MIT | text / line / table / **tree** / layout / popup / input / menu / split | 原语层：布局、弹层、菜单 |
| `nui-components.nvim` | MIT | 响应式状态、表单校验、layout-engine、树（基于 nui） | 状态层：布局区联动 |
| `render-markdown.nvim` | MIT | extmark 就地渲染 markdown，只画可见内容 | 对话 buffer 的 markdown 呈现 |
| *(参考) `sllm.nvim`* | Apache-2.0 | 后端外部 CLI + 插件只做 UI 编排，约 500 行 Lua | 架构先例，不引入 |
| *(禁用) CopilotChat.nvim* | **GPL-3** | 成熟聊天 UI | 只借思路，**不引入代码** |

**参考先例**：`sllm.nvim` 证明"外部进程做 agent + 插件只做 UI 粘合 + UI 全用社区库"这条路线可行且成本低；CopilotChat 官方 wiki 推荐"聊天 buffer + render-markdown"，说明该组合已被验证。GPL 生态只借鉴架构思路，不引入代码。

**`~/.lwl/nvim/` 布局**（`--clean` 下经 `runtimepath` 注入）：

```
~/.lwl/nvim/
  lua/agent/
    init.lua          组件层入口, 对外暴露 call(event, payload)
    layout.lua        布局加载与区域装配
    handlers/         按组件类型查处理器
    buffer_stream.lua 行缓冲流式写入 (攒批/切块/行上限/禁 undo)
    input.lua         多行输入与提交
  layout.json         UI 布局描述（配置, 非代码）
  syntax/             自带高亮脚本
  after/syntax/       追加用
  plugins/            nui 等社区库落位处
```

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

**加载与开关的具体做法**：

```vim
" 由 --cmd 或 -c 在启动早期注入
set runtimepath+=~/.lwl/nvim

" 代码块内嵌语言高亮 (内置 markdown syntax 支持)
let g:vimsyn_embed = 'lpch'           " 默认仅 'l' (Lua)
" after/syntax/*.vim 用于在内置脚本之后追加
```

**否决的路线**：预编译 treesitter parser 随包分发。完整证据链：

```
nvim 内置 treesitter parser (官方 :h treesitter 原文):
  C  Diff  Lua  Markdown  Vimscript  Vimdoc   (+ markdown_inline, query)

  => render-markdown 的【必需】parser (markdown/markdown_inline) 免费得到
  => python/js/java/go/rust/json/yaml 一个都没有

:TSInstall 的三个强制前置 (多个 parser 管理器一致列出):
  tree-sitter CLI   +   C 编译器 (gcc/clang/cl.exe)   +   git
        |
        └─ nvim-treesitter#8206 就是这个失败:
           "Failed to execute the C compiler command: cl.exe ..."
```

质量虽最好，但需要"平台 × 架构 × 语言"的编译矩阵与包体积代价，且运行时前置与"下载一个 nvim 就能用"的分发模型冲突。

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

**`config.json`（全局）**

```json
{
  "providers": {
    "openai":     { "api_key": "sk-...", "base_url": "https://api.openai.com/v1" },
    "anthropic":  { "api_key": "sk-ant-...", "base_url": "https://api.anthropic.com" }
  },
  "models": {
    "gpt5":     { "provider": "openai",    "model": "gpt-5" },
    "claude_x": { "provider": "anthropic", "model": "claude-x" }
  },
  "default_model": "gpt5"
}
```

**`roles/dev/role.json`（角色）**

```json
{
  "model": "claude_x",
  "tools": ["read_file", "grep", "shell"]
}
```

> `model` 存**名字**（引用 `config.json.models` 的键），不是配置本身 —— 换 key/url 只改全局一处。`tools` 是字符串数组，引用代码里写死的全局工具名；将来需要传参时升级为对象 `{"grep": {"max_results": 5}}`，字符串与对象同时接受，向后兼容。

**`roles/dev/sessions/<session-id>.json`**（结构示意，字段细节见 Open Questions）

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

**配置优先级**

```
命令行 --xxx          最高   (Spring Boot 原生)
      ^
~/.lwl/config.json    次之   (启动早期转成高优先级 PropertySource)
      ^
内置默认              最低
```

`config.json` 转为高优先级 PropertySource 的收益：`spring.ai.*` 等自动配置原生可读，凭据留在 JSON 里，命令行仍可覆盖 —— 两条配置线不打架。

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

- JSON 字段一律 `snake_case`（仓库约定）。合并 `role.json`（而非拆成两份）是用户明确的选择：工具子集与模型引用同属"角色的运行配置"。
- 全链路降级 + 告警、启动永不失败，沿用 `ping-ui` 抽象组件规范的既有不变量。

### D8 工具执行：Spring AI 的可观测、可中断工具循环

```java
// 策略注入点
ToolCallingManager mgr = ToolCallingManager.builder().build();
ChatClient cc = builder
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
    .build();

ChatClientResponse r = cc.prompt()
    .system(systemPrompt)                                  // 来自 system.md
    .tools(resolvedTools)                                  // 三段式解析结果
    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))  // 2.0 起必填
    .advisors(AdvisorParams.toolCallingAdvisorAutoRegister(false)) // 关掉自动循环
    .stream().chatClientResponse();

while (r.hasToolCalls()) {
    // <- 此处即"每次迭代 observable and interruptible"
    //    白名单外 -> 发 tool.confirm 事件挂起, 等适配器回话
    ToolExecutionResult res = mgr.executeToolCalls(prompt, r);
    ...
}
```

**依据**：官方文档明确 *"Each iteration is observable and interruptible"*，另有 `ToolCallObservingAdvisor` 可观测每个 tool-call chunk 并转发。**"挂起等确认"不需要自造对话循环**，这使确认策略的实现成本降一个档位。
**备选**：纯 observer 方案（只看不拦）—— 无法满足交互式的确认要求，否决。

### D9 会话持久化：自定义 `ChatMemoryRepository`

```java
ChatMemory memory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(new RoleFileRepository(roleDir))  // -> sessions/*.json
    .maxMessages(configuredWindow)                          // 窗口上限
    .build();
```

实现该接口写入 `roles/<name>/sessions/*.json`，配 `MessageWindowChatMemory` 提供窗口上限。这直接满足"会话记录全部存储在角色下"，无需自造记忆层。注意 Spring AI 2.0 起 `CONVERSATION_ID` 为必填，缺失即抛错。

### D10 入口与双形态

- **入口缺口的处理**：为 `ping-ai` 提供 `<Module>Launcher` 形态的入口（即约定中所说的 `PingAiLauncher`），使"业务模块不自带 main"的约定在未来由 `ping-apt` 自动生成时可直接替换。这是本变更最小代价的落点，不在本变更内实现 APT 生成。
- 终端形态以 `WebApplicationType.NONE` 启动，web 形态以 `SERVLET` 启动；由 `AutoConfiguration.imports` 登记的自动配置在"被作为依赖打包"时提供 web 服务能力，与 `--web` 共用同一套监听配置。
- 依赖调整：**移除 `ping-jpa`**（会话是 JSON 文件，不需要数据库）；保留 `ping-web` 但终端形态不启动 servlet 容器。

### D11 Neovim 分发子系统

**步骤**：

```
识别平台 -> 解析版本与 URL -> 校验和 -> 落位 ~/.lwl/bin/
        -> 置可执行位 -> 后续启动直接复用 (已存在则跳过)
```

**平台矩阵（第一版）**：

```
linux-x64   linux-arm64   darwin-x64   darwin-arm64
windows-* (兜底: 优先 named pipe, 不可用则 TCP loopback)
```

`--clean` 下通过显式 `runtimepath` 指向 `~/.lwl/nvim/`。ImageMagick 等可选外部工具若将来需要，复用同一套分发机制，不另建管线。

### D12 图片

```
输入侧 (一等能力)  粘贴/引用 -> 落盘 ~/.lwl/ -> base64 进模型   零终端依赖
输出侧 (降级)      不显示像素, 只显示文件名 = markdown 引用文本的原生行为
```

**被否决的展示路线**（供后续参考，不在本变更）：

```
kitty protocol  需 kitty>=28 / WezTerm(性能差)
Sixel           需 XTerm/foot 等 + ImageMagick 编译带 sixel
OSC 1337        仅 iTerm2/Konsole/WezTerm, 靠环境变量启发式探测
色块降级 █       需 ImageMagick 读像素
共同依赖 ImageMagick -> 若将来要做, 复用 D11 的分发机制
```

不装图片插件时 buffer 中本就是 markdown 引用文本，天然满足；将来要做展示只是往上叠加一层，不影响现有结构。

### D13 模块内落点（包结构）

```
ping-ai/src/main/java/cn/procsl/ping/boot/ai/
  config/      LwlConfigRoot, ConfigJsonLoader, PropertySourceRegistrar,
               ModelCatalog, RoleCatalog, RoleNameValidator
  prompt/      PromptSource (system.md / first.md)
  tool/        ToolRegistry, ToolPolicy, WhitelistPolicy,
               ConfirmPolicy, ToolSelectionResolver
  session/     RoleFileRepository (ChatMemoryRepository), SessionIds
  core/        AgentRuntime, EventStream, AgentEvent(类型)
  adapter/
    cli/       CliAdapter, LongArgsParser
    nvim/      NvimLauncher, MsgpackClient, NvimAdapter
    web/       WebAdapter, WsChannel, AgentAutoConfiguration
  PingAiLauncher          <- D10 的入口形态

~/.lwl/nvim/             <- lua 组件层 (见 D3)
```

包边界即分层边界：`adapter/*` 只依赖 `core` 与各自的事件编解码，互不依赖；`core` 不 import 任何 `adapter`。

### 覆盖对照：spec 需求 → 决策

| spec 需求 | 由哪个决策落实 |
|---|---|
| 系统提示词取自角色目录 / `first.md` 语义 | D7 + D3（两个适配器分叉） |
| 流式增量产出 / 调用失败事件 | D1（EventStream）+ D8 |
| 会话持久化于角色 / 窗口上限 | D9 |
| 模型配置来源全局 / 异常降级 | D7（优先级表） |
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

- 事件流的具体字段与分片格式（用户明确要求暂缓，兜底形态为细事件流）。上文 D1 只固定了**事件类型**，未涉及字段。
- 组件类型清单（`chat_panel` / `message` / `tool_call` / `input` / `confirm` 等）的确切命名与属性 —— 实现 lua 层时确定。上文 D3 只固定了**布局区域**与目录结构，未写死 type 名。
- 图片输出侧的展示增强（终端图形协议探测、色块降级）。
- 入口是否改由 `ping-apt` 生成 `<Module>Launcher`（本变更按 D10 手写同名形态，替换不影响外部行为）。
- 是否为用户预留 `~/.lwl/nvim-plugins/` 之类的插件扩展口（当前决定是不留，`--clean` 保证完全一致）。
