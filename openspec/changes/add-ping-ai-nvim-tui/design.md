# Design

## Context

动机与范围见 `proposal.md`。前置：`add-ping-ai-agent-core` 已冻结 box 契约与 `ToolPolicy` 接口。此处只列影响方案的现状与约束：

- 目标机器已确认**未安装 Neovim**，因此分发是本变更的一等子系统，而非环境假设。
- 平台为 Linux x86_64（WSL），终端图形能力未知。
- `ping-ai` 已由核心变更建立 `core/` 与 `adapter/cli/`；本变更只新增 `adapter/nvim/`。
- 项目许可证为 MIT；引入的 lua 库必须同为宽松许可。
- 构建必须带数据库 profile；本变更不改变依赖树中的 Java 库（除非选定 msgpack 库）。

**决策编号沿用拆分前的 `design.md`（D1–D13）**，空缺编号属于兄弟变更：

| 编号 | 归属 |
|---|---|
| D2、D3、D4、D5、D6、D11 | 本变更 |
| D12 的输出侧 | 本变更 |
| D14（新增） | 本变更 —— 先探针后铺开 |
| D1、D7、D8、D9、D10、D13 | `add-ping-ai-agent-core` |
| D10 的 web 部分 | `add-ping-ai-web-shell` |

## Goals / Non-Goals

**Goals:**

- 让 Java 侧的视野停留在「组件」层面，渲染细节沉到 lua 与布局配置。
- 尽可能复用社区积木，把自研限制在协议、编排与粘合。
- 交互形态在真实终端里可用：流式不卡、键位是 Vim 的、用户配置不干扰。
- 失败可诊断：获取、启动、连接、渲染四个环节各有明确错误。

**Non-Goals:**

- 不自研 TUI 渲染器，也不以「把 CLI 开在 nvim 终端 buffer 里」作为目标形态。
- 不做 treesitter parser 的编译与跨平台分发。
- 不做图片的像素级展示增强（协议探测、色块降级留待后续）。
- 不引入任何 GPL 许可代码。
- 不实现 Windows 分支（named pipe）—— 见 Open Questions。

## 阶段设计文档索引

本文件只承载架构决策（D2–D6、D11、D12 输出侧、D14）。实现级细节拆分到 `design/` 下的阶段文档：

| 阶段 | 文档 | 覆盖 tasks.md | 状态 |
|---|---|---|---|
| 1 | `design/phase-1-nvim-dist.md` | Neovim 分发 | 已迁移，待细化 |
| 2 | `design/phase-2-probe.md` | 管线探针（可终止变更） | 新增 |
| 3 | `design/phase-3-rpc.md` | 进程编排与 RPC | 已迁移，待细化 |
| 4 | `design/phase-4-nvim-ui.md` | 组件层 | 已迁移，待细化 |
| 5 | —— | 集成验收 | 无独立设计文档 |

约定：阶段文档与 `tasks.md` 的阶段编号一一对应，只承载「是什么」与「怎么验」；决策理由回写本文档对应的 D 条目。

## Decisions

### D2 进程模型：agent 是宿主，`nvim --clean --listen`

```
java(宿主)
  ├─ spawn: nvim --clean --listen <addr>
  │            ├─ 继承 java 的 stdin/stdout/stderr → nvim 直接在真终端全屏渲染
  │            └─ --listen 开一条 msgpack-rpc（unix socket；Windows 用 named pipe，兜底 TCP loopback）
  ├─ java 全程不碰 stdio，日志落 ~/.lwl/logs/
  └─ 双向 rpc：java→nvim 调 lua 组件层；nvim→java 发命令（submit/cancel/confirm/ack/pull）
```

- `--clean` 正是「不读用户配置、不加载用户插件、但保留内置 runtime 与 Vim 默认按键」的语义，一次参数同时满足两项要求。
- 选 unix socket 上的 msgpack-rpc 而非 HTTP/WS，**恰好消解了「纯命令行不启动 web 服务」与「交互式需要通信」的矛盾**：unix socket 不属于 web 服务，`--web` 才起 TCP 监听。

**备选 A**：`nvim --embed` + `nvim_ui_attach`，由 Java 自己消费 grid 事件画到终端 —— 等于实现一个 GUI 客户端，比自研 TUI 更重，否决。
**备选 B**：为 nvim 分配 pty、业务走 WS —— 引入 pty 库（JNA/pty4j）与 resize、信号处理的复杂度，且对本地父子进程用 WS 属过度设计，否决。
**备选 C**：nvim 只当渲染壳、把 CLI 开在其 terminal buffer 里 —— 零成本但 Java 仍在「输出文本」，组件协议根本不会被设计出来，否决（见 Non-Goals）。

### D3 交互层 = 组件库原语

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

社区库选型（全部 MIT）与 `~/.lwl/nvim/` 目录布局见 `design/phase-4-nvim-ui.md`。

**备选**：改造现成聊天插件的 UI 当渲染层 —— 撞 GPL 或与上游结构强耦合，否决。

### D4 渲染必须走两条路径

```
对话消息流  → append-only 行缓冲（只重画当前流式消息的尾部）
布局 / 弹层 → 组件状态（nui-components 的 state）
```

**依据**：`nui` 的树组件是整树重绘（`tree:render()`）。把对话流塞进一棵树、每来一个 token 就整体重绘，长对话必然退化。**行缓冲与组件状态是两条不同的渲染路径，不得混用**。

### D5 流式写入纪律

流式写入必须遵守五条纪律，全部来自真实事故或可推导的机制：

1. **不得产生超长行**。`neovim#34019`：单行 80 万字符 + wrap 导致 100% CPU 挂死。token 必须攒批、按换行切块后一次性写入，且单行长度有上限，超出即折行。
2. `undolevels = -1`（官方 `vi_diff`："Set it to -1 for no undo at all"），流式写入不进 undo 树。
3. 避免 legacy regex syntax 在超大 buffer 上的导航开销（`neovim#17862`，`--clean` 下同样复现）；长历史靠折叠收缩而非无界增长。
4. 高频变更触发的自动命令代价高（官方对 `TextChangedT` 的告警），不挂重活。
5. **markdown 渲染必须去抖**。`render-markdown.nvim` 的工作方式是监听 buffer 变更后重算可见区 extmark —— 在流式 buffer 上等价于「每个 chunk 触发一次 markdown 重解析」，正好撞上第 3、4 条。因此：流式期间冻结渲染，对象结束（`seq` 为非负以外）后一次性渲染；或只渲染已冻结的消息区间，当前流式消息不参与渲染。

**第 5 条是拆分时新识别出的约束**：原 design 只写了「对话 buffer 用 render-markdown 呈现」，没有说明它与第 3、4 条的关系。不做去抖，长对话的表现是打字久了 CPU 打满，而不是启动失败 —— 属于最难排查的一类问题。

### D6 代码高亮：syntax 脚本 + `runtimepath` 注入，不做 parser 分发

- 官方 `vim_diff.txt` Defaults："Filetype detection is enabled by default. Syntax highlighting is enabled by default."
- `:syntax on` 实为 `:source $VIMRUNTIME/syntax/syntax.vim`，而 `$VIMRUNTIME` 是 nvim 内置，`--clean` 不屏蔽它。
- syntax 加载走 `runtime! syntax/<name>.vim`，**搜索整个 `runtimepath`** → 把自有高亮脚本放进自有配置目录并注入 `runtimepath` 即可覆盖或补充内置脚本。

**否决的路线**：预编译 treesitter parser 随包分发 —— nvim 内置 parser 只覆盖 render-markdown 必需的 markdown，其余语言一个都没有；`:TSInstall` 强制要求 tree-sitter CLI + C 编译器 + git，与「下载一个 nvim 就能用」的分发模型冲突。完整证据链见 `design/phase-4-nvim-ui.md`。

### D11 Neovim 分发子系统

agent 自行下载 `nvim` 到 `~/.lwl/bin/` 并以 `--clean --listen` 启动，版本锁定，不跟随系统或最新版。

```
识别平台 -> 解析版本与 URL -> 校验和 -> 落位 ~/.lwl/bin/
        -> 置可执行位 -> 后续启动直接复用（已存在则跳过）
```

- **获取失败必须有明确错误**：提示网络/代理/校验和失败原因，并告知可改用非交互形态。这是对「启动永不失败」不变量的诚实修正 —— 交互形态没有替代渲染器，无法真正降级。
- ImageMagick 等可选外部工具若将来需要，复用同一套分发机制，不另建管线。

### D12 图片：只做输出侧降级

```
输入侧   已在 add-ping-ai-agent-core 实现（落盘 + base64）
输出侧   不显示像素，只显示文件名 = markdown 引用文本的原生行为
```

**被否决的展示路线**（供后续参考，不在本变更）：kitty protocol（需 kitty>=28 / WezTerm 性能差）、Sixel（需 XTerm/foot 等 + ImageMagick 编译带 sixel）、OSC 1337（仅 iTerm2/Konsole/WezTerm，靠环境变量启发式探测）、色块降级（需 ImageMagick 读像素）。共同依赖 ImageMagick —— 若将来要做，复用 D11 的分发机制。

不装图片插件时 buffer 中本就是 markdown 引用文本，天然满足；将来要做展示只是往上叠加一层，不影响现有结构。

### D14 先探针，后铺开

在实现完整 RPC 与组件层之前，先用一个**可失败的探针阶段**验证这条管线：

```
分发 nvim → --clean --listen 起来 → 一次 exec_lua 往返 → 一次流式 append → 采样耗时/行数/CPU
   成功 → 继续 30 个任务
   失败 → 终止变更，核心变更不受影响
```

**理由**：原方案把「管线是否可用」的验证放在 29 个任务之后（阶段 6 的 headless 断言）。一旦管线本身不成立（例如 `--listen` 与继承 stdio 的组合行为不符预期、rpc 往返延迟过高、流式 append 触发性能问题），前面所有工作都要返工。探针把最大的不确定性提前到第 2 阶段。

**探针的通过标准**（具体阈值见 `design/phase-2-probe.md`）：

- `exec_lua` 往返成功且延迟可接受；
- 1000 个 chunk 的流式 append 不产生超长行、不阻塞界面；
- 进程退出后资源正确回收。

## 覆盖对照：spec 需求 → 决策

| spec 需求 | 由哪个决策落实 |
|---|---|
| 交互式渲染引擎（`--clean` 保留 Vim 键位） | D2 |
| Neovim 自动获取 / 获取失败明确错误 | D11 |
| 流式呈现不退化 | D4 + D5 |
| 工具确认呈现 | D2（双向 rpc）+ 核心变更的 `ToolPolicy` |
| 图片无法显示时显示文件名 | D12 |
| 退出收尾 | D2 |

## Risks / Trade-offs

- **流式性能未知** → 探针阶段前置（D14）+ D5 五条纪律兜底。代价是探针本身需要真实终端，无法完全自动化。
- **markdown 渲染与流式写入冲突** → D5 第 5 条要求去抖；若开源库无法在冻结窗口内关闭渲染，需要自己接管渲染时机。
- **引入 Lua 语言栈**（Java 项目新增一层 Lua）→ 把 Lua 限制为薄粘合：布局与渲染用社区库，业务编排留在 Java；不把状态机写进 Lua。
- **运行时下载的供应链风险** → 版本锁定 + 校验和；代价是首次使用需要网络。
- **无网 / 代理 / 校验和不符时交互形态不可用** → 明确错误并提示改用非交互形态，不假装降级成功。
- **`--clean` 屏蔽用户已装的高亮与主题插件** → 这是「不受用户配置干扰」的必然结果，取一致性而放弃复用；不提供逃逸开关。
- **`nvim --listen` 与 nvim 版本行为差异** → 分发时锁定 nvim 版本，不跟随系统或最新版。

## Migration Plan

纯新增能力，不改变核心与非交互形态的行为。回滚方式是删除 `adapter/nvim/` 与 `~/.lwl/nvim/`，核心功能不受影响。

## Open Questions

- **msgpack 编解码**：用现成库（如 msgpack-jackson）还是手写最小实现？取决于依赖体积与 native 兼容性。
- **探针阈值**：往返延迟、chunk 吞吐、CPU 占用的具体数值需要一次真实采样后确定。
- **去抖策略**：冻结整段流式消息，还是只渲染已完成消息、流式消息走纯文本？两者对 `render-markdown` 的接入方式不同。
- **Windows 分支**：本变更只做 unix socket；named pipe 与 TCP 兜底是否另开变更。
- **图片展示增强**（终端图形协议探测、色块降级）。
- **是否为用户预留 `~/.lwl/nvim-plugins/` 之类的插件扩展口**（当前决定是不留，`--clean` 保证完全一致）。
