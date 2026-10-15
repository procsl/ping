# Tasks

## 1. 准备工作

- [ ] 1.1 列出所有 `@ResourceReference` 注解及其当前 name 值，对照 openapi.json 确认对应的 operationId，验证：grep 输出清单
- [ ] 1.2 批量修改 `@ResourceReference(name=...)` 为对应 operationId（如 `findUsers`、`accountStatusSetting`），验证：`grep -r "@ResourceReference" --include="*.java"` 输出
- [ ] 1.3 为缺少 operationId 的 Controller 方法添加 `@Operation(operationId=...)` 注解，验证：openapi.json 中所有目标方法都有唯一 operationId

## 2. 编译/组装器核心

### 2.1 compose.json 加载器

- [ ] 2.1.1 实现 classpath 扫描：查找所有 `META-INF/ping/ui/compose.json` 文件，验证：单测覆盖找到多个模块的 compose.json
- [ ] 2.1.2 实现 JSON 解析：将 compose.json 解析为组件数组（operation/function 列表），验证：单测覆盖正常解析
- [ ] 2.1.3 实现非法 JSON 检测：解析失败时抛出包含文件路径的异常，验证：单测覆盖损坏 JSON 报错
- [ ] 2.1.4 实现多文件合并：所有 compose.json 的数组合并为一个 app.children，验证：单测覆盖多模块合并

### 2.2 @ResourceReference 解析器

- [ ] 2.2.1 实现按 operationId 匹配 openapi.json 中的 path + method，验证：单测覆盖匹配成功
- [ ] 2.2.2 实现未找到 operationId 时的报错（包含可用 operationId 列表），验证：单测覆盖未找到报错
- [ ] 2.2.3 实现 `parameters` 数组解析：区分 `in: query`/`in: path`/`in: body`，分别放入 request.query/request.path/request.body，验证：单测覆盖三种参数位置
- [ ] 2.2.4 实现 `requestBody` 解析：拉取 `content.application/json.schema` 作为 request.body，验证：单测覆盖有/无 requestBody
- [ ] 2.2.5 实现 `responses` 解析：取 200/201 的 `content.application/json.schema` 作为 response，204 则为空对象，验证：单测覆盖 200/204 响应

### 2.3 query $ref 展开

- [ ] 2.3.1 实现 `$ref` 递归展开：将 `schema.$ref` 指向的 object 展开到 operation 中，验证：单测覆盖一层 $ref
- [ ] 2.3.2 实现嵌套 $ref 展开：处理 schema 内部嵌套的 `$ref`（如 UserRecord.account → UserAccountRecord），验证：单测覆盖嵌套 $ref
- [ ] 2.3.3 实现循环引用检测：展开时检测 $ref 循环并报错，验证：单测覆盖循环引用报错

### 2.4 ref → UUID 解析

- [ ] 2.4.1 实现路径解析：将 `findUsers.request.query.name` 解析为 operation 树中的具体字段节点，验证：单测覆盖正常解析
- [ ] 2.4.2 实现 UUID 生成 + 替换：为被引用字段生成 UUID，替换 ref 值，在字段上写入 `id` 属性，验证：单测覆盖替换正确
- [ ] 2.4.3 实现断链检测：ref 指向不存在的 operation 或字段时报错（包含完整 ref 路径），验证：单测覆盖断链报错

### 2.5 默认值合并

- [ ] 2.5.1 实现 operation defaults 写入：将 `defaults.request` 合并到 operation.request 对应字段的 `default` 属性，验证：单测覆盖合并成功
- [ ] 2.5.2 实现 function overrides 覆盖：将 function 级别的 overrides 覆盖 operation defaults，验证：单测覆盖覆盖成功
- [ ] 2.5.3 实现优先级链：实际调用值 > 覆盖默认值 > operation 默认值，验证：单测覆盖三级优先级

### 2.6 分页自动检测 + 物化

- [ ] 2.6.1 实现分页检测：request 有 `offset` + `size` 且 response 有 `content` + `total`，验证：单测覆盖检测成功/失败
- [ ] 2.6.2 实现 pagination 容器生成：创建 `type: "pagination"` 组件，children 为 output 子节点，验证：单测覆盖生成正确
- [ ] 2.6.3 实现 offset/size input 生成：自动创建 `type: "input"` 组件，验证：单测覆盖生成正确
- [ ] 2.6.4 实现 mode 属性：默认 `page`，支持 `scroll`，验证：单测覆盖默认值和自定义值

### 2.7 编译校验

- [ ] 2.7.1 实现引用完整性校验：所有 ref 必须能解析到具体字段，验证：单测覆盖断链报错
- [ ] 2.7.2 实现空 schema 检测：被引用字段的 schema 为空对象 `{}` 时报错，验证：单测覆盖空 schema 报错
- [ ] 2.7.3 实现非法 UI 属性检测：input/output 上出现 width/align/icon/order 等属性时报错，验证：单测覆盖非法属性报错
- [ ] 2.7.4 实现编译结果缓存：编译后的 JSON 缓存为只读快照，验证：单测覆盖缓存一致性

## 3. 后端聚合接口

- [ ] 3.1 实现 `GET /v1/ui/components` 端点：调用编译器，返回完整 JSON，验证：curl 返回 200 + 合法 JSON
- [ ] 3.2 实现 platform 过滤：按请求参数 `platform=web|desktop` 过滤不支持的组件，验证：单测覆盖不同平台返回不同子集
- [ ] 3.3 注册到 `UIAutoConfiguration`：确保 `/v1/ui/components` 在 Spring Boot 启动后可访问，验证：启动应用后 curl 可访问

## 4. 旧代码清理

- [ ] 4.1 移除 `ping-ui` 旧 SDK 代码（registry、lifecycle、message-bus、error_handler、sdk/ 目录），验证：`./mvnw -Ph2 test` 通过
- [ ] 4.2 移除前端旧渲染器代码（registry.tsx、derive.ts、default-tree.ts 等），验证：`npm run build` 通过

## 5. 验证

- [ ] 5.1 端到端验证：启动应用 → 请求 `/v1/ui/components` → 返回合法 JSON，验证完整链路畅通
- [ ] 5.2 验证编译失败场景：故意写入断链 ref，验证启动失败且报错信息包含具体引用路径
- [ ] 5.3 验证分页场景：编译后 JSON 中包含 pagination 组件和 offset/size input
