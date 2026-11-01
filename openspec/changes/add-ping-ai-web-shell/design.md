# Design

## Context

动机与范围见 `proposal.md`。前置：`add-ping-ai-agent-core` 已冻结事件类型与策略接口。此处只列影响方案的现状与约束：

- `ping-ai` 依赖 `ping-web`（拖入 `spring-boot-starter-web` 的 servlet 容器）与 `spring-boot-starter-websocket`，web 形态所需的运行时已具备。
- 核心变更已实现终端形态以 `WebApplicationType.NONE` 启动；本变更只新增 `--web` 分支。
- 主 spec 目录当前为空，本变更以新增 capability 的形式加入。
- Spring Boot 4.1.1、Java 25；构建必须带数据库 profile。
- 项目许可证为 MIT。

**决策编号沿用拆分前的 `design.md`（D1–D13）**，空缺编号属于兄弟变更：

| 编号 | 归属 |
|---|---|
| D10 的 web 部分 | 本变更 |
| D15（新增） | 本变更 —— 运行时判定用上下文类型 |
| D1、D7、D8、D9、D10（终端）、D12、D13 | `add-ping-ai-agent-core` |
| D2、D3、D4、D5、D6、D11、D14 | `add-ping-ai-nvim-tui` |

## Goals / Non-Goals

**Goals:**

- 三种外壳（CLI、Neovim、web）共用同一套消息语义，差异只在传输编码。
- 本模块被其他模块依赖时，能力以自动配置形式暴露，不要求宿主额外装配。
- 终端形态的行为完全不受本变更影响。

**Non-Goals:**

- 不做前端页面与组件渲染（归 `ping-ui` 的抽象组件体系）。
- 不做鉴权与权限模型（沿用宿主的鉴权，端点鉴权不作特殊处理）。
- 不做多用户并发会话与配额。
- 不进入 `ping-distribute`（当前不含 ping-ai）。

## 阶段设计文档索引

| 阶段 | 文档 | 覆盖 tasks.md | 状态 |
|---|---|---|---|
| 1 | `design/phase-1-startup.md` | 启动形态与自动配置 | 待细化 |
| 2 | `design/phase-2-transport.md` | HTTP 与 WebSocket | 待细化 |
| 3 | `design/phase-3-parity.md` | 语义一致与验收 | 待细化 |

约定：阶段文档与 `tasks.md` 的阶段编号一一对应，只承载「是什么」与「怎么验」；决策理由回写本文档对应的 D 条目。

## Decisions

### D10（web 部分）显式启动与双形态

| 形态 | 触发 | `WebApplicationType` | 网络监听 |
|---|---|---|---|
| 终端 | 默认 | `NONE` | 无 |
| Web | `--web` | `SERVLET` | HTTP + WebSocket |

- `--web` 是唯一触发条件；不存在「环境变量开启」之类的隐式开关。
- 终端形态由核心变更负责，本变更只新增 web 分支，不修改默认值。

### D15 运行时判定用上下文类型，不用「是否被依赖」

原需求写的是「被其他模块打包引入时提供 web 服务形态」。这在运行时不可判定：jar 不知道自己被谁依赖。可实现、可测试的表述是：

```
宿主是 web 应用（ApplicationContext 为 SERVLET） → 自动配置生效，提供 HTTP + WS
宿主不是 web 应用                              → 自动配置不生效，本模块不注册 web 组件
独立运行 + --web                               → 以 SERVLET 启动
独立运行 + 无 --web                            → NONE，不监听
```

因此自动配置使用 `@ConditionalOnWebApplication(type = SERVLET)`，而不是任何打包期标记。

**备选**：用 `ping.standalone` 之类的构建期属性在运行时判断 —— 属性不进入运行时，且与「散/聚打包」的构建语义混淆，否决。

## 覆盖对照：spec 需求 → 决策

| spec 需求 | 由哪个决策落实 |
|---|---|
| Web 形态显式启动并监听 | D10 |
| 作为依赖时提供 web 能力 | D15 |
| HTTP 端点 | D10 + D15 |
| WebSocket 双向消息 | D10 + D15 |
| 与交互形态语义一致 | D10（共用事件类型） |

## Risks / Trade-offs

- **端点鉴权**：沿用宿主鉴权，本变更不做特殊处理 → 若将来作为独立服务暴露，需要单独设计。
- **WS 背压**：慢客户端会堆积消息 → 与核心 EventStream 的背压策略保持一致，必要时丢弃中间增量而不是阻塞模型。
- **多用户并发**：当前按单用户本地工具设计 → 并发会话未定义，属于 Non-Goal。
- **自动配置的误触发**：宿主是 web 应用但不想启用本模块 → 需要提供关闭开关（`@ConditionalOnProperty`）。

## Migration Plan

纯新增能力。回滚方式是移除 `adapter/web/` 与自动配置登记，终端形态不受影响。

## Open Questions

- HTTP 端点的具体路径与请求/响应结构（遵循仓库的 `/v1/...` 与 SNAKE_CASE 约定）。
- WebSocket 消息字段（与核心变更的事件字段格式一起定）。
- 是否提供关闭自动配置的开关。
- 多用户并发与配额是否需要单独变更。
