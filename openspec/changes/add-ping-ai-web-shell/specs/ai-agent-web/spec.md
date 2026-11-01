# Spec Delta

## Purpose

把同一套 agent 能力以 web 形态交付：显式启动时监听 HTTP 与 WebSocket，被其他模块依赖时以自动配置暴露能力，并与终端交互形态共用同一套消息语义。

## ADDED Requirements

### Requirement: Web 形态显式启动
指定 web 形态时，系统 SHALL 以 web 应用形态启动并监听 HTTP 与 WebSocket。

#### Scenario: 显式要求 web
- **WHEN** 以 web 形态参数启动
- **THEN** 进程正常监听并接受 HTTP 与 WebSocket 连接

### Requirement: 作为依赖时提供 web 能力
模块被 web 应用作为依赖引入时 SHALL 通过自动配置提供 HTTP 与 WebSocket 能力，SHALL NOT 要求宿主额外装配。

#### Scenario: web 宿主引入本模块
- **WHEN** 一个 web 应用把本模块加入依赖并启动
- **THEN** 本模块的端点与消息通道可用，无需额外配置

#### Scenario: 非 web 宿主引入本模块
- **WHEN** 一个非 web 应用把本模块加入依赖并启动
- **THEN** 本模块不注册 web 组件，也不启动监听

#### Scenario: 关闭自动配置
- **WHEN** 宿主显式关闭本模块的自动配置
- **THEN** 不注册任何 web 组件

### Requirement: HTTP 端点
web 形态 SHALL 提供会话相关的 HTTP 端点，路径与字段遵循仓库既有约定（`/v1/...` 与 SNAKE_CASE）。

#### Scenario: 请求端点
- **WHEN** 客户端请求会话相关端点
- **THEN** 返回符合约定的响应结构

### Requirement: WebSocket 消息通道
web 形态 SHALL 提供 WebSocket 通道，支持下行增量与上行输入、确认响应。

#### Scenario: 建立连接
- **WHEN** 客户端连接 WebSocket 通道
- **THEN** 连接建立并可收发消息

#### Scenario: 确认响应
- **WHEN** 服务端发出工具确认请求
- **THEN** 客户端可经同一通道返回允许或拒绝

### Requirement: 与交互形态语义一致
web 形态与终端交互形态 SHALL 使用同一套消息语义，差异仅限于传输编码。

#### Scenario: 同一事件两种传输
- **WHEN** 同一次对话分别在两种形态下运行
- **THEN** 两者表达的事件类型与含义一致
