# Proposal

## Why

`add-ping-ai-agent-core` 冻结了 box 契约与非交互交付面，但整个提案最核心的取舍仍未验证：**用 Neovim 当交互式渲染引擎，而不是自研 TUI**。这项取舍的失败模式 —— 流式卡顿、超长行、RPC 阻塞、二进制获取失败 —— 只在真实终端里暴露，因此单独成一个变更。

拆开的直接好处：本变更开头有一个**可失败的探针阶段**。探针失败就终止，核心变更已经独立归档、独立可用，沉没成本可控。

## What Changes

- **新增 Neovim 分发子系统**：平台识别、版本锁定、下载、校验和、落位 `~/.lwl/bin/`，不要求系统预装。
- **新增进程编排**：以宿主身份 `spawn nvim --clean --listen <addr>`，继承终端；unix socket 上的 msgpack-rpc 双向通道；Java 全程不碰 stdio，日志落 `~/.lwl/logs/`。
- **新增 lua 组件层**：基于 `nui.nvim`、`nui-components.nvim`、`render-markdown.nvim`（均 MIT），经 `runtimepath` 注入自有配置目录。
- **新增流式渲染纪律**：攒批、按换行切块、单行长度上限、禁 undo、markdown 渲染去抖 —— 全部针对已知的编辑器性能事故。
- **新增语法高亮**：syntax 脚本随包提供，零编译、零 treesitter parser 分发。
- **新增交互式确认呈现**：白名单外的工具调用在界面请求确认，确认前不执行。
- **新增图片输出侧降级**：无法显示图片时显示文件名。
- **不包含**：核心对话逻辑改动、web 形态、Windows 分支（见 Open Questions）。

## Capabilities

### New Capabilities

- `ai-agent-nvim`: 交互式交付面 —— 渲染引擎、二进制获取、流式呈现纪律、确认交互、图片降级。

### Modified Capabilities

无。本变更只消费 `add-ping-ai-agent-core` 已冻结的 box 契约与 `ai-agent-tool-execution` 的确认策略接口，不修改任何既有需求。

## Impact

- **代码**：`ping-ai` 新增 `adapter/nvim/`（启动器、rpc 客户端、交互适配器）；`src/main` 不新增 main，不改变打包形态。
- **运行时目录**：`~/.lwl/bin/nvim`、`~/.lwl/nvim/`（lua 组件层 + 布局 + syntax）、`~/.lwl/logs/`。
- **依赖**：新增 3 个 MIT lua 库；Java 侧可能需要一个 msgpack 编解码依赖（待定，见 Open Questions）。
- **不受影响**：`ping-distribute`（当前不含 ping-ai）、非交互 CLI 的行为、既有 spec。
- **前置**：`add-ping-ai-agent-core` 的 box 契约与策略接口。两者可以并行开发，但本变更归档前核心变更必须已冻结语义。
