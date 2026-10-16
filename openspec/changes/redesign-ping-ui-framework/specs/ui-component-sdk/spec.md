# Spec Delta

## REMOVED Requirements

### Requirement: 处理器按类型与命名空间注册
**Reason**: 重设计后不再有运行时注册表，改为编译时静态解析
**Migration**: 组件类型在 compose.json 中声明，编译阶段校验

### Requirement: 运行组件树并自动注入运行时对象
**Reason**: 不再有运行时 SDK 实例，前端直接消费编译后的 JSON
**Migration**: 前端加载 /v1/ui/components 返回的静态 JSON

### Requirement: 创建预绑定的节点处理器
**Reason**: 不再有处理器注册机制
**Migration**: 无

### Requirement: 子节点派发由处理器驱动
**Reason**: 不再有运行时派发，前端递归渲染 JSON 树
**Migration**: 前端按 type 递归渲染

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
**Reason**: 编译阶段校验，不存在运行时未注册场景
**Migration**: 编译阶段报错

### Requirement: 错误处理器可被覆盖
**Reason**: error_handler 机制移除
**Migration**: 无

### Requirement: 保持框架无关
**Reason**: 仍然适用，但语义变化
**Migration**: 见 ADDED 需求

## ADDED Requirements

### Requirement: 静态组件树驱动

系统 SHALL 提供 `GET /v1/ui/components` 端点，返回编译后的完整组件树 JSON。前端 SHALL 直接消费该 JSON，按 `type` 递归渲染，无需运行时注册表。

#### Scenario: 获取组件树
- **WHEN** 前端请求 /v1/ui/components
- **THEN** 返回 app 根节点，children 包含 operation 和 function

#### Scenario: 空组件树
- **WHEN** 无 compose.json 或全部编译失败
- **THEN** 返回 200 + 空 app 树

### Requirement: 框架无关渲染

系统 SHALL NOT 绑定任何前端框架。同一份编译后的 JSON 可由 React、Vue 或任何前端实现消费。

#### Scenario: 多框架消费
- **WHEN** 前端请求 /v1/ui/components
- **THEN** 返回与框架无关的纯 JSON 声明

### Requirement: 编译时校验

系统 SHALL 在启动阶段编译所有 compose.json，校验所有引用完整性。任何断链、空 schema 或非法属性 SHALL 导致启动失败。

#### Scenario: 编译成功
- **WHEN** 所有 compose.json 编译通过
- **THEN** 应用正常启动

#### Scenario: 编译失败
- **WHEN** 任何引用断链或 schema 为空
- **THEN** 启动失败，报错信息包含具体引用路径

### Requirement: operation 可调用

前端 SHALL 能从编译后的 JSON 中提取 operation 注册为可调用方法。operation 包含 method/path/request/response，前端可直接发起 HTTP 调用。

#### Scenario: 调用 operation
- **WHEN** 前端需要获取用户列表
- **THEN** 找到 id="findUsers" 的 operation，使用其 method/path/request 发起 GET /v1/system/users

### Requirement: 平台过滤

系统 SHALL 支持在组装时按平台过滤组件。前端请求时携带平台标识，后端返回该平台支持的组件子集。

#### Scenario: Web 平台
- **WHEN** 前端请求 /v1/ui/components?platform=web
- **THEN** 只返回 web 平台支持的组件

#### Scenario: 桌面平台
- **WHEN** 前端请求 /v1/ui/components?platform=desktop
- **THEN** 只返回 desktop 平台支持的组件
