# Tasks

> 状态：架构已按新模型定稿（`app > pages > function` + openapi 内联，见 design.md）。任务清单已重排。实现由 `/opsx-apply` 启动；使用意图（裁剪/默认/排序/分页）不在本次，仅留 `Transformer` 扩展点。

## 1. DDD 架构

### 1.1 domain 层

- [x] 1.1.1 创建统一节点组合类型（`type` + `id` + `properties` + `children`），验证：单测覆盖 type/id/properties/children 读写
- [x] 1.1.2 按 `type` 分派（app/page/function 各自约束），验证：单测覆盖按 type 分派

### 1.2 exception 层

- [x] 1.2.1 精简异常到 2 个（`ComposeException` + `ComposeParseException`），验证：单测覆盖各错误场景的异常消息

### 1.3 repository 层

- [x] 1.3.1 实现 `ComposeRepository` 扫描 `META-INF/ping/ui/compose.json`，验证：单测覆盖 classpath 扫描、JSON 解析、多文件合并
- [x] 1.3.2 实现 `OpenApiRepository` 扫描 `META-INF/ping/openapi-<module>.json`，验证：单测覆盖多文件扫描合并

### 1.4 compiler 层

- [x] 1.4.1 创建 `Transformer` 接口（开闭原则扩展点），验证：单测覆盖接口定义
- [x] 1.4.2 创建 `CompileContext` 编译上下文（operations 索引），验证：单测覆盖读写
- [x] 1.4.3 实现 `OperationTransformer` 阶段（按 operationId 定位 operation、展开 `$ref`、内联 method/path/parameters/requestBody/responses 到 `function.properties`，接受冗余、无 `$ref` 残留），验证：单测覆盖内联、`$ref` 展开、无残留
- [x] 1.4.4 实现 `ComposeCompiler` 主编排（协调各 Transformer，返回 `App`），验证：单测覆盖完整编译流程

## 2. 编译校验

- [x] 2.1 `page.id` 全局唯一校验，验证：单测覆盖重复报错
- [x] 2.2 `function.id` 在 page 内唯一校验，验证：单测覆盖重复报错
- [x] 2.3 `operationId` 断链校验（openapi 中找不到），验证：单测覆盖断链报错含引用路径
- [x] 2.4 openapi 内 `operationId` 冲突校验，验证：单测覆盖冲突报错
- [x] 2.5 未知属性/未知类型忽略 + debug 日志，验证：单测覆盖不报错
- [x] 2.6 `function.id` 路由规则（`page.id/function.id`），验证：单测覆盖路由生成

## 3. 后端聚合接口

- [x] 3.1 实现 `GET /v1/ui/components` 端点，返回编译产物，验证：curl 返回 200 + 合法 JSON
- [x] 3.2 注册到 `UIAutoConfiguration`，验证：启动应用后 curl 可访问
- [x] 3.3 `openapi.export=true` 时跳过编译与端点注册，验证：单测覆盖导出模式不编译
- [x] 3.4 存在 compose.json 而无 openapi 文件 → 启动失败，验证：单测覆盖缺输入报错

## 4. 旧代码清理

### 4.1 移除旧编译器（$ref/UUID/pagination/排序/默认值那套）

- [x] 4.1.1 移除 `OperationRefResolver`、`SchemaRefExpander`、`RefResolver`、`PaginationMaterializer`、`DefaultResolver`，验证：代码无残留引用
- [x] 4.1.2 移除旧 `Component`/`Compose` 等聚合类型，验证：代码无残留引用

### 4.2 移除旧异常与旧测试

- [x] 4.2.1 移除 `RefNotFoundException`、`CircularReferenceException`、`ComposeCompileException`，验证：代码无残留引用
- [x] 4.2.2 移除旧 component 包测试，验证：`./mvnw -Ph2 test` 通过

### 4.3 移除旧前端 SDK

- [x] 4.3.1 移除 SDK 机制（registry、message-bus、sdk-error、handlers），保留 `createPingUi` 占位 entry，验证：`npm run build` 通过

### 4.4 移除存量旧格式文件

- [x] 4.4.1 删除 `ping-system/src/main/resources/ui/user-center/compose.json`，验证：代码无残留引用

## 5. 验证

- [x] 5.1 端到端：启动应用 → 请求 `/v1/ui/components` → 返回自包含 function JSON，验证完整链路
- [x] 5.2 编译失败：故意写断链 `operationId`，验证启动失败且报错含具体引用路径
- [x] 5.3 operation 内联：验证产物 function.properties 无 `$ref` 残留，method/path/parameters/responses 完整
- [x] 5.4 DDD 架构：`ComposeCompiler` 返回 `App`，含 `pages`/`functions`

## 6. 后端 query 参数纠正

- [x] 6.1 给 query POJO 参数（如 `UserQuery`）加 `@ParameterObject` 或自定义组合注解，验证：openapi 中 UserQuery 展开为 name/account/state/gender 等独立 query 参数
- [x] 6.2 验证后端实际绑定与 openapi 描述一致（平铺参数），验证：curl `?name=x&state=enable` 可正确查询
