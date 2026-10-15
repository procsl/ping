# 阶段 7 设计：入口与双形态

对应 `tasks.md` 阶段 7（任务 7.1–7.8）。本文档承载实现级细节。架构决策见 `../design.md` D10。

> 注：本文档由 design.md 迁移而来，尚未按细化节奏展开；后续细化时补充 web 端点与 WS 消息格式。

## 入口形态

- 为 `ping-ai` 提供 `<Module>Launcher` 形态的入口（即约定中所说的 `PingAiLauncher`），使"业务模块不自带 main"的约定在未来由 `ping-apt` 自动生成时可直接替换。
- 本变更手写同名形态，**不在本变更内实现 APT 生成**（见 `../design.md` Open Questions）。

## 双形态启动

| 形态 | 触发 | `WebApplicationType` | 网络监听 |
|---|---|---|---|
| 终端 | 默认 | `NONE` | 无 |
| Web | `--web` 或被依赖打包 | `SERVLET` | HTTP + WebSocket |

- 由 `AutoConfiguration.imports` 登记的自动配置在"被作为依赖打包"时提供 web 服务能力，与 `--web` 共用同一套监听配置。
- web 形态与终端交互式形态使用同一套消息语义，差异仅限于传输方式。

## 依赖调整

- **移除 `ping-jpa`**：会话是 JSON 文件，不需要数据库（已在 `phase-1-config.md` 任务 1.1 落实）。
- 保留 `ping-web`，但终端形态不启动 servlet 容器。
