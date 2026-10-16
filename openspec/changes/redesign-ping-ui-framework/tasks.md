# Tasks

## 1. DDD 架构重构

### 1.1 domain 层

- [ ] 1.1.1 创建 `Component` 组合类型（type + id + properties + children），验证：单测覆盖 type/id/properties/children 的读写
- [ ] 1.1.2 创建 `App` 应用根（包含编译后的 Component 树），验证：单测覆盖 children 读写

### 1.2 exception 层

- [ ] 1.2.1 精简异常到 2 个（`ComposeException` + `ComposeParseException`），合并 `RefNotFoundException` 和 `CircularReferenceException`，验证：单测覆盖各种错误场景的异常消息

### 1.3 repository 层

- [ ] 1.3.1 将 `ComposeRepository` 移到 `repository` 包，保持返回 `List<JsonNode>`，验证：单测覆盖 classpath 扫描、JSON 解析、多文件合并

### 1.4 compiler 层

- [ ] 1.4.1 创建 `CompilePhase` 接口，验证：单测覆盖接口定义
- [ ] 1.4.2 创建 `CompileContext` 编译上下文，验证：单测覆盖 operations 读写
- [ ] 1.4.3 创建 `OperationCompiler`（整合 OperationRefResolver + SchemaRefExpander + DefaultResolver），验证：单测覆盖 operation 解析、$ref 展开、默认值合并
- [ ] 1.4.4 创建 `ComponentCompiler`（整合 RefResolver + PaginationMaterializer），验证：单测覆盖 UUID 解析、分页物化
- [ ] 1.4.5 创建 `ComposeCompiler` 主编排器，验证：单测覆盖完整编译流程

### 1.5 service 层

- [ ] 1.5.1 创建 `ComposeService` 领域服务，验证：单测覆盖完整调用流程

### 1.6 组件初始化/组装

- [ ] 1.6.1 实现组件直接 new 创建（不依赖 Spring），验证：单测覆盖 new 创建 + 编译 + 释放流程
- [ ] 1.6.2 实现 `ComposeService.compile()` 无状态，每次调用创建新实例，验证：多次调用互不影响

## 2. 编译校验

- [ ] 2.1 实现引用完整性校验：所有 $ref 必须能解析到具体字段，验证：单测覆盖断链报错
- [ ] 2.2 实现空 schema 检测：被引用字段的 schema 为空对象 `{}` 时报错，验证：单测覆盖空 schema 报错
- [ ] 2.3 实现非法 UI 属性检测：input/output 上出现 width/align/icon/order 等属性时报错，验证：单测覆盖非法属性报错
- [ ] 2.4 实现编译结果缓存：编译后的 JSON 缓存为只读快照，验证：单测覆盖缓存一致性
- [ ] 2.5 实现 operation id 全局唯一性校验：多个 compose.json 中不允许重复 operation id，验证：单测覆盖重复检测
- [ ] 2.6 实现 function id 全局唯一性校验：多个 compose.json 中不允许重复 function id，验证：单测覆盖重复检测
- [ ] 2.7 实现 view/form 必须关联 operation 校验，验证：单测覆盖未关联报错
- [ ] 2.8 实现 operation 的 api 字段必须标注校验，验证：单测覆盖未标注报错

## 3. 后端聚合接口

- [ ] 3.1 实现 `GET /v1/ui/components` 端点，验证：curl 返回 200 + 合法 JSON
- [ ] 3.2 实现 platform 过滤，验证：单测覆盖不同平台返回不同子集
- [ ] 3.3 注册到 `UIAutoConfiguration`，验证：启动应用后 curl 可访问

## 4. 旧代码清理

### 4.1 移除旧 compiler 组件（已整合到 OperationCompiler/ComponentCompiler）

- [ ] 4.1.1 移除 `OperationRefResolver.java`，验证：代码中无残留引用
- [ ] 4.1.2 移除 `SchemaRefExpander.java`，验证：代码中无残留引用
- [ ] 4.1.3 移除 `DefaultResolver.java`，验证：代码中无残留引用
- [ ] 4.1.4 移除 `RefResolver.java`，验证：代码中无残留引用
- [ ] 4.1.5 移除 `PaginationMaterializer.java`，验证：代码中无残留引用

### 4.2 移除旧异常类（已合并到 ComposeException）

- [ ] 4.2.1 移除 `RefNotFoundException.java`，验证：代码中无残留引用
- [ ] 4.2.2 移除 `CircularReferenceException.java`，验证：代码中无残留引用
- [ ] 4.2.3 移除 `ComposeCompileException.java`（重命名为 `ComposeException`），验证：代码中无残留引用

### 4.3 移除旧 Compose 聚合根（已删除）

- [ ] 4.3.1 移除 `Compose.java`，验证：代码中无残留引用

### 4.4 移除旧前端 SDK 代码

- [ ] 4.4.1 移除 `ping-ui/src/main/js/sdk/` 目录（registry、message-bus、error-handler 等），验证：`npm run build` 通过
- [ ] 4.4.2 移除前端旧渲染器代码（registry.tsx、derive.ts、default-tree.ts 等），验证：`npm run build` 通过

### 4.5 移除旧测试代码

- [ ] 4.5.1 移除 `ComponentTest.java`（性能测试，与 UI 组件无关），验证：`./mvnw -Ph2 test` 通过
- [ ] 4.5.2 移除旧 component 包下的测试（ComposeRepositoryTest、DefaultResolverTest、OperationRefResolverTest、PaginationMaterializerTest、RefResolverTest、SchemaRefExpanderTest），验证：`./mvnw -Ph2 test` 通过

## 5. 验证

- [ ] 5.1 端到端验证：启动应用 → 请求 `/v1/ui/components` → 返回合法 JSON，验证完整链路畅通
- [ ] 5.2 验证编译失败场景：故意写入断链 $ref，验证启动失败且报错信息包含具体引用路径
- [ ] 5.3 验证分页场景：编译后 JSON 中包含 pagination 组件和 offset/size input
- [ ] 5.4 验证 DDD 架构：`ComposeService.compile()` 返回 `App`，包含编译后的 `List<Component>`
