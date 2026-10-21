# Proposal

## Why

`redesign-ping-ui-framework` 已落地自包含功能树（`GET /v1/ui/components`，`function.properties` 为 `$ref` 全展开、仅 2xx 的 openapi operation），其 spec 明确将「轻量 client」推后到后续提案。前端目前没有任何 API 调用机制——后续渲染器与业务自定义组件都必须有一个消费该产物的客户端。编译期已把解析重活吃掉（swagger-client 需在运行时做的 $ref 解析、文档索引，ping 已在启动期完成），此刻承接客户端成本最低、与后端架构咬合最好。

## What Changes

- 新增 **`ComposeClient`**（`ping-ui/src/main/js/client/`，纯 TS、零 React 依赖）：
  - `await ComposeClient.create()` 静态工厂：内部经 `treeSource`（默认 `fetch /v1/ui/components`）取树、按 `operationId` 建索引；`treeSource` 可注入（测试/离线喂树，唯一碰网络的地方）
  - 参数绑定：`fetch(id, { params })` 的 params 按内联 `parameters[].in` 自动分拣——`path` 替换、`query` 平铺序列化、`body` JSON 序列化
  - 可插拔洋葱模型 Handler 管道（`client.register(handler)`，`ctx` 携带 operation 与请求描述，可改写请求/响应、可短路）；**core 零自带 Handler**，加密/解密/转换/鉴权/剪枝/校验/缓存全部由注册实现；副作用事件机制（emit/订阅）推后至渲染器提案
  - 错误契约：非 2xx / 网络失败 / 解析失败一律 **reject `ApiError`**（`status` / `operationId` / `message` / `fieldErrors`——对齐后端 `MessageVO` / `ParameterErrorVO` 形态）
  - 零魔法：401 不特殊处理（归鉴权 Handler）；snake_case 不自动转换（转换是注册的 Handler）
- `sdk/index.ts` 导出衔接（vite entry 与产物 `renderer.js` 形态不变，client 与其同 bundle）
- 恢复 `vitest` 测试（`npm test`）并接入 maven test 阶段（复用 `ping.npm.skip` 开关）
- 删除过时文档 `ping-ui/doc/abstract-component-schema.md` 与 `ping-ui/doc/sdk-design.md`（描述已删除的 18 类型模型与旧 SDK 机制，误导性强；新规范由本变更 design 与后续渲染器提案承载）
- **不含**（推后到渲染器提案）：渲染器（侧栏/路由/function 渲染器/渲染注册键）、登录页与 401 重放登录流程、官方 Handler 实现、demo 页

## Capabilities

### New Capabilities

- `compose-client`: 消费 compose 编译产物的 API 客户端行为——创建与树源注入、operationId 索引、参数自动绑定、可插拔洋葱管道、错误契约

### Modified Capabilities

（无——`ui-component-sdk` 尚未归档入主库；本变更不修改既有 spec 需求）

## Impact

- `ping-ui/src/main/js/client/`（新增 core：Client/Handler 接口/管道执行器/ApiError/绑定逻辑）
- `ping-ui/src/main/js/sdk/index.ts`（导出衔接）
- `ping-ui/package.json`（vitest devDependency + `test` script）
- `ping-ui/pom.xml`（exec-maven-plugin 增加 test 阶段 `npm test` 执行）
- `ping-ui/doc/`（删除两份过时文档）
- 无后端 Java 改动、无 API 契约变更、无业务模块改动
