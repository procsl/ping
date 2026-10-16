# Tasks

## 1. 编译/组装器核心

### 1.1 compose.json 加载器

- [x] 1.1.1 实现 classpath 扫描：查找所有 `META-INF/ping/ui/compose.json` 文件，验证：单测覆盖找到多个模块的 compose.json
- [x] 1.1.2 实现 JSON 解析：将 compose.json 解析为组件数组（operation/function 列表），验证：单测覆盖正常解析
- [x] 1.1.3 实现非法 JSON 检测：解析失败时抛出 `ComposeParseException`（包含文件路径 + 行号 + 错误原因），验证：单测覆盖损坏 JSON 报错
- [x] 1.1.4 实现空数组检测：compose.json 为空数组 `[]` 时正常通过（表示该模块无 UI 组件），验证：单测覆盖空数组
- [x] 1.1.5 实现顶层类型检测：数组元素必须是 `operation` 或 `function` 类型，其他类型报错，验证：单测覆盖非法顶层类型
- [x] 1.1.6 实现多文件合并：所有 compose.json 的数组合并为一个 app.children，验证：单测覆盖多模块合并

### 1.2 $ref 解析器（openapi:// 协议）

- [x] 1.2.1 实现按 operationId 匹配 openapi.json 中的 path + method，验证：单测覆盖匹配成功
- [x] 1.2.2 实现未找到 operationId 时的报错 `ReferenceNotFoundException`（包含可用 operationId 列表），验证：单测覆盖未找到报错
- [x] 1.2.3 实现重复 operationId 检测：多个方法有相同 operationId 时报错，验证：单测覆盖重复检测
- [x] 1.2.4 实现 `parameters` 数组解析：区分 `in: query`/`in: path`，分别放入 request.query/request.path，验证：单测覆盖两种参数位置
- [x] 1.2.5 实现 `requestBody` 解析：拉取 `content.application/json.schema` 作为 request.body，验证：单测覆盖有/无 requestBody
- [x] 1.2.6 实现 requestBody 非 application/json 时跳过（如 multipart/form-data），验证：单测覆盖非 JSON body
- [x] 1.2.7 实现 `responses` 解析：取 200/201 的 `content.application/json.schema` 作为 response，204 则为空对象，验证：单测覆盖 200/204 响应
- [x] 1.2.8 实现 response 无 200/201/204 时报错（只有 4xx/5xx），验证：单测覆盖无成功响应报错
- [x] 1.2.9 实现 parameter 级 `required` 标记：将 required=true 的 parameter 在 schema 中标记 `required: true`，验证：单测覆盖 required 标记

### 1.3 $ref 展开

- [x] 1.3.1 实现 `$ref` 递归展开：将 `schema.$ref` 指向的 object 展开到 operation 中，验证：单测覆盖一层 $ref
- [x] 1.3.2 实现嵌套 $ref 展开：处理 schema 内部嵌套的 `$ref`（如 UserRecord.account → UserAccountRecord），验证：单测覆盖嵌套 $ref
- [x] 1.3.3 实现循环引用检测：展开时检测 $ref 循环并报错 `CircularReferenceException`，验证：单测覆盖循环引用报错
- [x] 1.3.4 实现 `$ref` 目标不存在时报错，验证：单测覆盖断链 $ref
- [x] 1.3.5 实现 array items 中的 `$ref` 展开（如 `items: { $ref: ... }`），验证：单测覆盖数组 items 展开

### 1.4 $ref → UUID 解析（openapi:// 协议）

- [x] 1.4.1 实现路径解析：将 `openapi://<operationId>/<request|response>/<path>` 解析为 operation 树中的具体字段节点，验证：单测覆盖正常解析
- [x] 1.4.2 实现 UUID 生成 + 替换：为被引用字段生成 UUID，替换 $ref 值，在字段上写入 `id` 属性，验证：单测覆盖替换正确
- [x] 1.4.3 实现断链检测：$ref 指向不存在的 operation 或字段时报错 `RefNotFoundException`（包含完整 $ref 路径），验证：单测覆盖断链报错
- [x] 1.4.4 实现重复 $ref 检测：同一 compose.json 中多个组件引用同一字段时，复用相同 UUID（不重复生成），验证：单测覆盖复用
- [x] 1.4.5 实现跨 operation $ref 检测：$ref 指向其他 operation 的字段时报错（$ref 仅限同一 operation），验证：单测覆盖跨 operation 报错
- [x] 1.4.6 实现 $ref 路径格式校验：必须为 `openapi://<operationId>/<request|response>/<path>` 格式，验证：单测覆盖格式错误

### 1.5 默认值合并

- [x] 1.5.1 实现 operation defaults 写入：将 `defaults.request` 合并到 operation.request 对应字段的 `default` 属性，验证：单测覆盖合并成功
- [x] 1.5.2 实现 function overrides 覆盖：将 function 级别的 overrides 覆盖 operation defaults，验证：单测覆盖覆盖成功
- [x] 1.5.3 实现优先级链：实际调用值 > 覆盖默认值 > operation 默认值，验证：单测覆盖三级优先级
- [x] 1.5.4 实现 defaults 目标字段不存在时报错，验证：单测覆盖 defaults 断链
- [x] 1.5.5 实现 overrides 类型校验：overrides 值类型必须与 schema 声明类型一致，验证：单测覆盖类型不匹配报错

### 1.6 分页自动检测 + 物化

- [x] 1.6.1 实现分页检测：request 有 `offset` + `size` 且 response 有 `content` + `total`，验证：单测覆盖检测成功/失败
- [x] 1.6.2 实现 pagination 容器生成：创建 `type: "pagination"` 组件，children 为 output 子节点，验证：单测覆盖生成正确
- [x] 1.6.3 实现 offset/size input 生成：自动创建 `type: "input"` 组件，验证：单测覆盖生成正确
- [x] 1.6.4 实现 mode 属性：默认 `page`，支持 `scroll`，验证：单测覆盖默认值和自定义值
- [x] 1.6.5 实现 pagination 插入位置：pagination 组件插入到 view children 末尾（output 之后），验证：单测覆盖位置正确
- [x] 1.6.6 实现非分页 operation 不生成 pagination，验证：单测覆盖无分页场景

### 1.7 编译校验

- [ ] 1.7.1 实现引用完整性校验：所有 $ref 必须能解析到具体字段，验证：单测覆盖断链报错
- [ ] 1.7.2 实现空 schema 检测：被引用字段的 schema 为空对象 `{}` 时报错，验证：单测覆盖空 schema 报错
- [ ] 1.7.3 实现非法 UI 属性检测：input/output 上出现 width/align/icon/order 等属性时报错，验证：单测覆盖非法属性报错
- [ ] 1.7.4 实现编译结果缓存：编译后的 JSON 缓存为只读快照，验证：单测覆盖缓存一致性
- [ ] 1.7.5 实现 operation id 全局唯一性校验：多个 compose.json 中不允许重复 operation id，验证：单测覆盖重复检测
- [ ] 1.7.6 实现 function id 全局唯一性校验：多个 compose.json 中不允许重复 function id，验证：单测覆盖重复检测
- [ ] 1.7.7 实现 view/form 必须关联 operation 校验，验证：单测覆盖未关联报错
- [ ] 1.7.8 实现 operation 的 api 字段必须标注校验，验证：单测覆盖未标注报错

## 2. 后端聚合接口

- [ ] 2.1 实现 `GET /v1/ui/components` 端点：调用编译器，返回完整 JSON，验证：curl 返回 200 + 合法 JSON
- [ ] 2.2 实现 platform 过滤：按请求参数 `platform=web|desktop` 过滤不支持的组件，验证：单测覆盖不同平台返回不同子集
- [ ] 2.3 注册到 `UIAutoConfiguration`：确保 `/v1/ui/components` 在 Spring Boot 启动后可访问，验证：启动应用后 curl 可访问

## 3. 旧代码清理

- [ ] 3.1 移除 `ping-ui` 旧 SDK 代码（registry、lifecycle、message-bus、error_handler、sdk/ 目录），验证：`./mvnw -Ph2 test` 通过
- [ ] 3.2 移除前端旧渲染器代码（registry.tsx、derive.ts、default-tree.ts 等），验证：`npm run build` 通过

## 4. 验证

- [ ] 4.1 端到端验证：启动应用 → 请求 `/v1/ui/components` → 返回合法 JSON，验证完整链路畅通
- [ ] 4.2 验证编译失败场景：故意写入断链 $ref，验证启动失败且报错信息包含具体引用路径
- [ ] 4.3 验证分页场景：编译后 JSON 中包含 pagination 组件和 offset/size input

## 5. 重构（基于 openapi:// 协议）

### 5.1 $ref 解析器重构

- [ ] 5.1.1 移除 `@ResourceReference` 注解匹配逻辑，改为 `openapi://` 协议解析，验证：单测覆盖 openapi:// 前缀解析
- [ ] 5.1.2 移除旧的 `@ResourceReference` 注解定义和处理代码，验证：代码中无 @ResourceReference 残留
- [ ] 5.1.3 更新 operationId 匹配逻辑，支持 `openapi://<operationId>` 格式，验证：单测覆盖匹配成功/失败

### 5.2 $ref → UUID 解析重构

- [ ] 5.2.1 移除旧的 `ref` 路径解析逻辑，改为 `openapi://` 协议路径解析，验证：单测覆盖 openapi:// 路径解析
- [ ] 5.2.2 更新路径格式校验，支持 `openapi://<operationId>/<request|response>/<path>` 格式，验证：单测覆盖格式校验
- [ ] 5.2.3 移除旧的 `ref` 路径相关代码，验证：代码中无旧 ref 路径解析残留

### 5.3 编译校验重构

- [ ] 5.3.1 更新引用完整性校验，支持 `openapi://` 协议，验证：单测覆盖断链报错
- [ ] 5.3.2 更新 operation 的 api 字段校验，支持 `openapi://` 协议，验证：单测覆盖未标注/格式错误报错

### 5.4 旧代码清理

- [ ] 5.4.1 移除 `@ResourceReference` 注解定义，验证：代码中无 @ResourceReference 注解
- [ ] 5.4.2 移除旧的 `ref` 路径解析相关代码，验证：代码中无旧 ref 解析逻辑
- [ ] 5.4.3 移除旧的 JSON Pointer 解析相关代码，验证：代码中无 JSON Pointer 解析逻辑
