# Proposal: redesign-ping-ui-framework

## Why

上一版抽象 UI 框架（`add-ping-ui-web-sdk` 产物）过度依赖自定义组件类型系统 + `$ref`/UUID 字段引用：18 种组件类型中大量与前端导航壳耦合，SDK 注册表 + 消息总线 + 生命周期机制过重，后端聚合链路已断裂（`GET /v1/ui/components` 不存在）。

本变更早期草案虽精简了类型，但仍自造字段路径 DSL（`openapi://<operationId>/<request|response>/<path>` + `items` 段语义），表达力低于 JSON Schema——遇到嵌套对象、oneOf、discriminator 等 OpenAPI 已支持的形态就要扩语法；且字段级声明量大、新增接口成本高。

需要重新设计为「功能树 + openapi 内联」模型：**compose.json 只声明功能树与接口引用，字段细节全部来自 openapi 内联，表达力天花板 = OpenAPI 全集**。

## What Changes

- **BREAKING**：组件结构从五层（function/view/form/input/output + 平级 operation）改为统一节点树 `type: app > page > function`，节点用 `type` 字段标识，domain 用单一组合类型承载
- **BREAKING**：移除 `$ref`/UUID 字段引用机制、`openapi://` 字段路径 DSL、pagination 容器、分页物化、排序 input、默认值/overrides——字段细节全来自 openapi
- **BREAKING**：compose.json 源中开发者只声明 `page`/`function` 标识 + `operationId` 引用，零字段声明
- **新增**：`function` = openapi operation 的自包含镜像（method/path/parameters/requestBody/responses 内联，schema 全展开，接受冗余），前端据单个 function 即可渲染 + 调用
- **新增**：`function.id` 路由规则 = `page.id + "/" + function.id`，菜单以路由标识平铺
- **新增**：编译器 `Transformer` 开闭原则，使用意图（裁剪/默认/可排序/分页模式）未来往 `function.properties` 扩展，本次不做
- **新增**：编译校验（id 重复、operationId 断链/冲突 → 启动失败；未知属性/类型忽略 + debug）
- **恢复**：后端 `GET /v1/ui/components` 聚合接口，返回编译产物
- **保留**：OpenAPI 文档打包期导出到 `META-INF/ping/openapi-<module>.json`，运行时扫描合并；`openapi.export=true` 导出进程跳过编译；存在 compose.json 而无 openapi 文件 → 启动失败
- **DDD**：统一节点组合类型（type + id + properties + children，`type` 分派）、`ComposeCompiler` 编排、`ComposeRepository`/`OpenApiRepository` 仓储、exception 精简到 2 个

## Capabilities

### New Capabilities

- `ui-declaration-language`: 抽象 UI 声明语言规范——功能树结构（app/page/function）、`function.id` 路由规则、operationId 引用与 openapi 内联、使用意图扩展点、编译校验

### Modified Capabilities

- `ui-component-sdk`: 从「运行时注册表 + 消息总线 + 生命周期」模型改为「静态自包含功能树驱动」模型。移除处理器注册、生命周期、消息通知；新增 function 自包含可调用、编译校验、编译输入、打包导出跳过等需求。

## Impact

- `ping-ui` 模块：SDK 机制移除，重写为「openapi 内联」编译器 + `Transformer` 骨架，DDD 架构重构；端点注册在 `UIAutoConfiguration`
- `ping-distribute`：打包期导出 OpenAPI 到 `META-INF/ping/openapi-<module>.json`；恢复 `GET /v1/ui/components`（跟随登录拦截）
- 前端：**只做减法**——移除 SDK 机制，保留 `createPingUi` 占位 entry 与薄壳；渲染器与轻量 client（据 function.properties 拼 fetch）不在本次范围
- `compose.json` 格式：彻底变更；**不做迁移**，存量旧文件删除，业务模块按新规范声明
- 已知限制（归后续提案）：业务模块独立运行（有 compose.json、无 openapi 文档）会启动失败；分模块导出 openapi 前须解决全量/部分文件互斥规则
