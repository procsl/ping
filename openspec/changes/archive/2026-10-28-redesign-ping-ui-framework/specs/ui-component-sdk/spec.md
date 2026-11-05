# Spec Delta

## REMOVED Requirements

### Requirement: 处理器按类型与命名空间注册
**Reason**: 重设计后不再有运行时注册表，改为编译时静态解析
**Migration**: 组件在 compose.json 中声明，编译阶段内联 openapi 描述

### Requirement: 运行组件树并自动注入运行时对象
**Reason**: 不再有运行时 SDK 实例，前端直接消费编译后的自包含 JSON
**Migration**: 前端加载 /v1/ui/components 返回的静态 JSON，据 function 调用

### Requirement: 创建预绑定的节点处理器
**Reason**: 不再有处理器注册机制
**Migration**: 无

### Requirement: 子节点派发由处理器驱动
**Reason**: 不再有运行时派发，前端据产物自行渲染
**Migration**: 前端据 function.properties 渲染与调用

### Requirement: 生命周期阶段与钩子
**Reason**: SDK 生命周期机制移除
**Migration**: 无

### Requirement: 销毁时清理注册表与订阅
**Reason**: 无注册表和订阅
**Migration**: 无

### Requirement: 消息通知机制
**Reason**: 消息总线移除
**Migration**: 无

### Requirement: 单节点渲染错误隔离
**Reason**: 运行时错误隔离由前端框架保证
**Migration**: 前端渲染器自行处理错误边界

### Requirement: 未注册类型降级
**Reason**: 编译阶段校验，未知类型忽略 + debug
**Migration**: 编译阶段忽略未知类型

### Requirement: 错误处理器可被覆盖
**Reason**: error_handler 机制移除
**Migration**: 无

### Requirement: 保持框架无关
**Reason**: 仍然适用，但语义变化
**Migration**: 见 ADDED 需求

## ADDED Requirements

### Requirement: 静态功能树驱动

系统 SHALL 提供 `GET /v1/ui/components` 端点，返回编译后的自包含功能树 JSON（app 根，pages 含自包含 function）。前端 SHALL 直接消费该 JSON，据 function 调用与渲染，无需运行时注册表。

#### Scenario: 获取功能树
- **WHEN** 前端请求 /v1/ui/components
- **THEN** 返回 app 根节点，pages 包含自包含 function

#### Scenario: 空功能树
- **WHEN** classpath 中没有任何 compose.json
- **THEN** 返回 200 + 空 app 树

### Requirement: function 自包含可调用

每个 `function` 的 `properties` SHALL 自包含完整 openapi operation 描述（method/path/parameters/requestBody/responses，schema 全内联）。前端 SHALL 据此发起 HTTP 调用，无需额外请求 openapi 或回查引用。轻量 client 实现不在本次范围内。

`parameters` 中 query POJO 参数 SHALL 已展开为多个独立 query 参数（后端 `@ParameterObject` 保证 openapi 描述准确），前端直接平铺发送。`responses` SHALL 只含 2xx schema。

#### Scenario: 据 function 调用
- **WHEN** 前端需获取用户列表
- **THEN** 找到路由 "user/list" 的 function，据 properties 的 method/path/parameters 平铺发送 GET /v1/system/users

#### Scenario: query 参数平铺
- **WHEN** function 的 parameters 含 query 参数
- **THEN** 已展开为 name/state/gender 等独立参数，前端平铺拼 query string

### Requirement: 框架无关渲染

系统 SHALL NOT 绑定任何前端框架。同一份编译产物可由 React、Vue 或任何前端消费。布局 SHALL 由前端自定。

#### Scenario: 多框架消费
- **WHEN** 前端请求 /v1/ui/components
- **THEN** 返回与框架无关的纯 JSON 功能树

### Requirement: 编译时校验

系统 SHALL 在应用启动阶段扫描 classpath，将功能树编译并组装到内存（运行期间不重编译，直到重启）。编译输入 SHALL 为打包期导出的 OpenAPI 文档（`META-INF/ping/openapi-<module>.json`）；存在 compose.json 却无任何该类文件时 SHALL 报错、启动失败。结构性错误 SHALL 导致启动失败（详见 ui-declaration-language 编译校验）。

> ⏸️ **遗留（后续提案）**：散装运行的豁免方式、分模块导出时全量/部分文件并存的互斥规则，均延后到「分模块 openapi 导出」提案一并解决。

#### Scenario: 编译成功
- **WHEN** 所有 compose.json 编译通过
- **THEN** 应用正常启动

#### Scenario: 结构性错误
- **WHEN** 存在 id 重复、operationId 断链或冲突
- **THEN** 启动失败，报错含引用路径与声明来源

#### Scenario: 缺少 OpenAPI 输入
- **WHEN** 存在 compose.json 但 classpath 中无任何 openapi-<module>.json
- **THEN** 启动失败

### Requirement: 打包导出模式跳过编译

以 `openapi.export=true` 启动的进程（打包期负责生成 openapi 文档）SHALL 跳过组件编译与端点注册，否则首次构建时输入文档尚未生成会自锁。除此模式外 SHALL 无任何豁免路径。

#### Scenario: 导出进程不编译
- **WHEN** 以 `openapi.export=true` 启动应用
- **THEN** 跳过组件编译与端点注册，导出进程正常完成

### Requirement: 端点鉴权不作特殊处理

`GET /v1/ui/components` SHALL 跟随现有全局登录拦截器，SHALL NOT 通过 `@PermitAll` 等方式豁免。权限策略本身不在本次范围内。

#### Scenario: 未登录请求
- **WHEN** 未登录客户端请求 /v1/ui/components
- **THEN** 返回 401（跟随现有拦截器，无任何豁免配置）
