# Proposal

## Why

ping-ai 目前是空壳 —— `src/main` 下只有一个类体为空的 `ChatStreamService`，没有入口、没有行为。要让它成为一个能在终端真正跑起来的 agent（带工具调用、可自定义系统提示词、配置与会话按角色组织在本地目录），必须先确定交互层的形态。本变更最核心的取舍是**用 Neovim 当交互式渲染引擎而非自研 TUI**，它决定了交互层的全部结构，也是后续扩展到 web 的同一套消息语义的起点。

## What Changes

- **新增 agent 对话核心**：基于 Spring AI 组装 ChatClient，支持自定义系统提示词、流式产出、多轮会话记忆。
- **新增 `~/.lwl/` 本地配置目录**：全局模型配置 `config.json`（provider 的 key/url、模型清单、默认模型）与角色容器 `roles/<role-name>/`，配置文件全部为 JSON。
- **新增角色（role）容器**：`system.md` 系统提示词、`first.md` 默认用户提示词、`role.json`（模型引用 + 工具子集）、`sessions/` 存放全部会话记录。角色名限定 `[a-zA-Z0-9_-]+`，仅是本地组织用的名字，**不传给模型、不定义人格**。
- **新增工具体系**：全局工具清单在代码中定义；三段式选择（创建角色时选定 → 会话开始时可从全局清单再选 → 未选则沿用角色创建时的默认）；按形态区分的执行策略 —— 非交互式只跑白名单，交互式对白名单之外的调用请求人工确认。
- **新增终端非交互式形态**：Spring Boot 长参数风格（`--xxxx`，不提供短参数），直接消费核心事件流，零网络监听。
- **新增终端交互式形态（Neovim TUI）**：agent 作为宿主自行下载 `nvim` 到 `~/.lwl/bin/` 并以 `--clean --listen` 启动，经 msgpack-rpc 单向控制；组件化 lua 渲染层建立在社区库之上，Java 侧只见"组件"不见 buffer。
- **新增语法高亮**：syntax 脚本随包提供，经 `runtimepath` 注入自有配置目录加载 —— 零编译、零 treesitter parser 分发。
- **新增图片输入能力**：粘贴或引用的图片落盘后以 base64 交给模型；输出侧不强制展示图片，无法展示时显示文件名。
- **新增 web 形态**：显式 `--web` 或作为依赖被打包进其他模块时启动 HTTP + WebSocket，与终端共用同一套消息语义。
- ping-ai 当前没有任何可运行行为，本变更不产生破坏性变更。

## Capabilities

### New Capabilities

- `ai-agent-conversation`: 对话运行时 —— 提示词组装、模型调用、流式产出，以及会话记录在角色目录下的持久化与恢复。
- `ai-agent-role`: 角色容器 —— 配置目录结构、角色名校验、角色级的提示词与模型和工具选择、会话归属。
- `ai-agent-tool-execution`: 工具调用 —— 全局工具清单、角色与会话两级的工具选择、白名单与确认两种执行策略。
- `ai-agent-terminal`: 终端形态 —— 非交互式调用、Neovim 交互式 TUI、两种形态的打包与 web 监听开关。

### Modified Capabilities

无。既有 spec 仅 `ui-component-sdk`（前端组件 SDK），与本变更无需求层面的交集。

## Impact

- **代码**：`ping-ai` 模块 `src/main` 从空壳到完整实现；其 `pom.xml` 可能需要调整 —— 当前依赖 `ping-web`（拖入 `spring-boot-starter-web` 的 servlet 容器）与 `ping-jpa`，对纯终端形态是负担。
- **构建**：`ping-ai` 需要一个可执行 jar 入口。仓库约定"业务模块 `src/main` 不得自带 main、`mainClass` 由 `ping-apt` 生成 `<Module>Launcher`"，但 `ping-apt` 目前并无生成 Launcher 的代码，根 pom 也无 repackage 配置 —— 这个缺口如何补见 `design.md`。
- **依赖**：新增 Neovim 生态库（`nui.nvim`、`nui-components.nvim`、`render-markdown.nvim`）。三者均为 MIT，与本项目 MIT 许可证兼容；GPL 许可的同类插件只借鉴架构，不引入代码。
- **运行时目录**：新增 `~/.lwl/` 约定 —— 全局配置、下载的 nvim、lua 与高亮脚本、日志、角色与会话。
- **不受影响**：`ping-distribute`（当前不含 ping-ai）、任何前端代码、既有 spec `ui-component-sdk`。
