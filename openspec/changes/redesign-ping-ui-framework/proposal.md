# Proposal: redesign-ping-ui-framework

## Why

当前抽象 UI 框架（`add-ping-ui-web-sdk` 产物）过度依赖 JSON 驱动：18 种组件类型中大量与前端导航壳耦合（layout、nav_group、menu、breadcrumb、tabs 等），SDK 注册表 + 消息总线 + 生命周期机制过重，且后端聚合链路已断裂（`GET /v1/ui/components` 不存在）。需要重新设计一个更克制、更专注的声明式 UI 框架：**JSON 只声明功能组件及其与接口的输入/输出映射，渲染细节交给前端**。

同时，`cn.procsl.ping.boot.ui.component` 包下的组件设计混乱、职责不明确、不符合领域驱动设计（DDD）。编译流程涉及多个阶段（operation 解析、$ref 展开、UUID 物化、默认值合并、分页物化），但代码组织是扁平的，缺少编排层，命名混乱（Resolver/Expander/Materializer 三种后缀），没有类型安全的领域对象。

## What Changes

- **BREAKING**：移除 12 种前端耦合组件类型（application、layout、nav_group、menu、breadcrumb、tabs、user_info_panel、main_container、row_action、form_action、empty、column 作为独立类型）
- **BREAKING**：移除 SDK 注册表、生命周期、消息总线、错误处理器等重机制
- **新增**：`operation` 组件类型——接口契约定义（含 method/path/request/response），编译时解析 `$ref` 引用（`openapi://<operationId>`）替换为 OpenAPI schema
- **新增**：`input` / `output` 组件类型——通过 `$ref` 引用 operation 的 request/response 字段（`openapi://<operationId>/<request|response>/<path>`），编译时解析为 UUID
- **新增**：`function` 组件类型——功能入口（唯一标识 + children），不含导航 UI 概念
- **新增**：`view` / `form` 组件类型——数据视图（读）/ 表单（写），包含 input/output 子节点
- **新增**：编译校验分级——结构性错误（引用断链、被引用的空 schema、id 重复、view/form 未关联 operation）失败则启动失败；未知类型/未知属性忽略 + debug 日志（版本升级兼容）
- **新增**：OpenAPI 文档打包期导出、统一落 `META-INF/ping/openapi-<module>.json`，运行时扫描合并作为编译输入；存在 compose.json 而无任何 openapi 文档 → 启动失败（散装豁免归后续提案）；`openapi.export=true` 导出进程跳过 UI 编译
- **新增**：`$ref` 使用 `openapi://<operationId>` 协议匹配 OpenAPI operation（非 summary），冲突时追加类型+方法名
- **修改**：`compose.json` 只声明功能组件，不携带任何 UI 展示属性（width、align、icon、order 等）
- **恢复**：后端 `GET /v1/ui/components` 聚合接口，返回编译后的完整 JSON
- **新增**：DDD 代码架构——引入 `Component` 组合类型、`App` 应用根、`ComposeService` 领域服务、`ComposeCompiler` 编译器、`ComposeRepository` 仓储
- **重构**：compiler 包整合为 `OperationCompiler` + `ComponentCompiler`，符合开闭原则
- **精简**：exception 包从 4 个异常类精简到 2 个

## Capabilities

### New Capabilities

- `ui-declaration-language`: 抽象 UI 声明语言规范——定义组件类型系统（app/operation/function/view/form/input/output/pagination，无占位类型）、`$ref` 引用语法（`openapi://` 协议 + 数组 `items` 段）、编译阶段解析规则与校验分级、operation 结构约定（method/path/request/response/defaults/sortable_fields）、`$ref` 匹配规则

### Modified Capabilities

- `ui-component-sdk`: 现有 SDK spec 需要重写——从"运行时注册表 + 消息总线 + 生命周期"模型改为"编译时声明 + 静态 JSON 驱动"模型。移除处理器注册、生命周期、消息通知等需求，新增 operation 契约定义、input/output 引用、编译校验等需求。

## Impact

- `ping-ui` 模块：SDK 机制移除，重写为编译/组装器，DDD 架构重构；端点注册在 `UIAutoConfiguration`
- `ping-distribute`：打包期导出 OpenAPI 到 `META-INF/ping/openapi-<module>.json`；恢复 `GET /v1/ui/components` 端点（跟随登录拦截）
- 前端：**只做减法**——移除 SDK 机制，保留 `createPingUi` 占位 entry 与薄壳不变，渲染器重建不在本次范围
- `compose.json` 格式：破坏性变更；**不做迁移**，存量旧文件删除，业务模块按新规范自行声明
- `$ref` 引用语法：统一使用 `openapi://` 协议风格（如 `openapi://findUsers`、`openapi://findUsers/request/query/name`）
- 已知限制（归后续提案）：业务模块独立运行（有 compose.json、无 openapi 文档）会启动失败；分模块导出 openapi 前须解决全量/部分文件互斥规则
