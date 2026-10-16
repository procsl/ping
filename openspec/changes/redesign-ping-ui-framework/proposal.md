# Proposal: redesign-ping-ui-framework

## Why

当前抽象 UI 框架（`add-ping-ui-web-sdk` 产物）过度依赖 JSON 驱动：18 种组件类型中大量与前端导航壳耦合（layout、nav_group、menu、breadcrumb、tabs 等），SDK 注册表 + 消息总线 + 生命周期机制过重，且后端聚合链路已断裂（`GET /v1/ui/components` 不存在）。需要重新设计一个更克制、更专注的声明式 UI 框架：**JSON 只声明功能组件及其与接口的输入/输出映射，渲染细节交给前端**。

## What Changes

- **BREAKING**：移除 12 种前端耦合组件类型（application、layout、nav_group、menu、breadcrumb、tabs、user_info_panel、main_container、row_action、form_action、empty、column 作为独立类型）
- **BREAKING**：移除 SDK 注册表、生命周期、消息总线、错误处理器等重机制
- **新增**：`operation` 组件类型——接口契约定义（含 method/path/request/response），编译时解析 `$ref` 引用（`openapi://<operationId>`）替换为 OpenAPI schema
- **新增**：`input` / `output` 组件类型——通过 `$ref` 引用 operation 的 request/response 字段（`openapi://<operationId>/<request|response>/<path>`），编译时解析为 UUID
- **新增**：`function` 组件类型——功能入口（唯一标识 + children），不含导航 UI 概念
- **新增**：`view` / `form` 组件类型——数据视图（读）/ 表单（写），包含 input/output 子节点
- **新增**：编译阶段全量校验（引用完整性、schema 非空），失败则启动失败
- **新增**：`$ref` 使用 `openapi://<operationId>` 协议匹配 OpenAPI operation（非 summary），冲突时追加类型+方法名
- **修改**：`compose.json` 只声明功能组件，不携带任何 UI 展示属性（width、align、icon、order 等）
- **恢复**：后端 `GET /v1/ui/components` 聚合接口，返回编译后的完整 JSON

## Capabilities

### New Capabilities

- `ui-declaration-language`: 抽象 UI 声明语言规范——定义组件类型系统（app/operation/function/view/form/input/output/placeholder）、`$ref` 引用语法（`openapi://` 协议）、编译阶段解析规则、operation 结构约定（method/path/request/response/defaults/sortable_fields）、`$ref` 匹配规则

### Modified Capabilities

- `ui-component-sdk`: 现有 SDK spec 需要重写——从"运行时注册表 + 消息总线 + 生命周期"模型改为"编译时声明 + 静态 JSON 驱动"模型。移除处理器注册、生命周期、消息通知等需求，新增 operation 契约定义、input/output 引用、编译校验等需求。

## Impact

- `ping-ui` 模块：SDK 核心重写，新增编译/组装器
- `ping-distribute`：恢复 `GET /v1/ui/components` 端点，打包期导出 OpenAPI + 编译组件 JSON
- 前端渲染器：重写，从"运行时注册表分发"改为"静态 JSON + 按 type 渲染"
- `compose.json` 格式：破坏性变更，所有业务模块需迁移
- `$ref` 引用语法：统一使用 `openapi://` 协议风格（如 `openapi://findUsers`、`openapi://findUsers/request/query/name`）
