# Proposal

## Why

`ping-ai` 目前只有 `pom.xml` —— 没有源码、没有入口、没有可运行行为。本变更确立 agent 的地基：本地配置与角色目录约定、对话核心、工具体系，以及一个不依赖任何外部进程的非交互式交付面。

把它与交互式渲染拆开的原因：这一层可以完全用单测与进程内断言验证；而 Neovim 渲染层（见 `add-ping-ai-nvim-tui`）的失败模式只在真实终端里暴露。先让地基独立归档，后续两个交付面才有稳定的语义可依赖。

## What Changes

- **新增 `~/.lwl/` 配置根**：全局模型配置 `config.json`（provider 的 key/url、模型清单、默认模型、历史窗口），配置文件为 JSON。
- **新增角色（role）容器**：`system.md` 系统提示词、`first.md` 默认用户提示词、`role.json`（模型引用 + 工具子集）、`sessions/` 会话记录。角色名限定 `[a-zA-Z0-9_-]{1,36}`，仅是本地组织用的名字，**不传给模型、不定义人格**。
- **新增对话核心**：提示词组装、模型客户端显式构造、流式增量事件、会话 JSON 持久化与恢复、历史窗口上限。
- **新增工具体系**：全局工具清单在代码中定义；三段式选择（会话指定 → 角色默认 → 全局清单）；白名单与确认两种策略（确认策略在本变更用测试替身验证挂起与恢复）。
- **新增图片输入**：图片落盘后以 base64 组装为多模态消息。
- **新增终端非交互式形态**：Spring Boot 长参数风格（`--xxxx`，不提供短参数），直接消费核心事件流，零网络监听。
- **新增终端入口与可执行打包**：手写 `PingAiLauncher` 并在 `ping-ai/pom.xml` 配置 `spring-boot-maven-plugin` repackage。**这与 AGENTS.md「业务模块 `src/main` 不得自带 main、`mainClass` 由 `ping-apt` 生成」的约定冲突**，本变更以最小代价先跑通，并在 `design.md` 记为已知债务。
- **不包含**：Neovim 交互形态（`add-ping-ai-nvim-tui`）、web 形态（`add-ping-ai-web-shell`）。

## Capabilities

### New Capabilities

- `ai-agent-conversation`: 对话运行时 —— 提示词组装、模型调用、流式产出、图片输入，以及会话记录在角色目录下的持久化与恢复。
- `ai-agent-role`: 角色容器 —— 配置目录结构、角色名校验、角色级的提示词与模型选择、会话归属。
- `ai-agent-tool-execution`: 工具调用 —— 全局工具清单、角色与会话两级的工具选择、白名单与确认两种执行策略。
- `ai-agent-cli`: 非交互式交付面 —— 长参数调用、标准输出增量、退出状态、零网络监听、独立运行默认不启动 web。

### Modified Capabilities

无。主 spec 目录当前为空（`redesign-ping-ui-framework` 与 `add-compose-client` 尚未归档），本变更不与任何既有 spec 产生需求层面交集。

## Impact

- **代码**：`ping-ai` 模块从「只有 pom」到完整核心与非交互适配器；新增 `core/` `config/` `prompt/` `tool/` `session/` `adapter/cli/` 与 `PingAiLauncher`。
- **依赖**：`spring-ai-starter-model-openai` 收窄为 `spring-ai-openai` —— 本变更显式构造模型客户端，不依赖 `spring.ai.*` 自动配置（见 `design.md` D7）。
- **构建**：`ping-ai/pom.xml` 增加 `spring-boot-maven-plugin`，使 `package` 产出可执行 jar；`ping.standalone` 属性目前无任何插件消费，本变更不引入该机制。
- **运行时目录**：新增 `~/.lwl/` 约定 —— 全局配置、角色、会话、图片、日志。
- **不受影响**：`ping-distribute`（当前不含 ping-ai）、任何前端代码、Neovim 与 lua 相关部分。
