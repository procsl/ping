# Proposal

## Why

`add-ping-ai-agent-core` 已冻结事件语义，`add-ping-ai-nvim-tui` 承担交互式渲染的高风险。web 是第三种传输，实现成本低但需要独立验收：HTTP 端点、WebSocket 通道、以及与交互形态的语义一致性。

单独成变更的理由：它不依赖 Neovim 子系统，只依赖核心的事件类型；把它与 nvim 绑在一起，会让一个低成本交付面被高风险工作拖住。

## What Changes

- **新增 `--web` 显式启动**：以 `SERVLET` 形态启动并监听 HTTP + WebSocket。
- **新增自动配置**：通过 `AutoConfiguration.imports` 登记，宿主应用为 web 应用时提供本模块能力。
- **新增 HTTP 端点**：提供会话相关的请求入口。
- **新增 WebSocket 通道**：与终端交互形态共用同一套消息语义，差异仅在传输编码。
- **新增语义一致性验收**：同一事件类型在两种形态下含义一致。
- **不包含**：核心对话逻辑改动、Neovim 渲染、前端页面（前端归 `ping-ui`）。

## Capabilities

### New Capabilities

- `ai-agent-web`: web 交付面 —— 显式启动、作为依赖时的能力暴露、HTTP 与 WebSocket 传输、与交互形态的语义一致性。

### Modified Capabilities

无。本变更只消费 `add-ping-ai-agent-core` 已冻结的事件类型，不修改任何既有需求。

## Impact

- **代码**：`ping-ai` 新增 `adapter/web/` 与 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`；`src/main` 不新增 main。
- **依赖**：`ping-web` 与 `spring-boot-starter-websocket` 已在本模块依赖树中，无需新增。
- **打包**：不改变终端形态的产物；被其他模块依赖时以自动配置提供能力。
- **不受影响**：非交互 CLI、Neovim 交互形态、`ping-distribute`。
- **前置**：`add-ping-ai-agent-core` 的事件类型冻结。
