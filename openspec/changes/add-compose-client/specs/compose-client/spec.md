# Spec Delta

## Purpose

消费 compose 编译产物（自包含功能树）的 API 客户端：按 operationId 索引 function、自动绑定参数、经可插拔管道发起调用，并以统一错误契约暴露失败，为渲染器与业务自定义组件提供零框架依赖的数据调用层。

## ADDED Requirements

### Requirement: 创建时经树源获取功能树

客户端 SHALL 在创建阶段经树源获取编译产物功能树并建立索引，默认树源为 `GET /v1/ui/components`；树源 SHALL 可注入（测试与离线场景直接提供树数据）；除树源加载与调用链终点的 HTTP 发送外，客户端 SHALL NOT 发起其他网络请求。

#### Scenario: 默认树源创建

- **WHEN** 以默认配置创建客户端且端点可达
- **THEN** 客户端获取功能树并可立即调用，期间不发生第二次树请求

#### Scenario: 注入树源创建

- **WHEN** 创建客户端时注入返回本地树数据的树源
- **THEN** 客户端不发起任何网络请求即完成索引建立

#### Scenario: 树源失败

- **WHEN** 默认树源请求返回 401 或网络失败
- **THEN** 创建以失败结束（reject），错误携带失败原因，不产生半初始化的客户端

### Requirement: 功能树只读暴露

客户端 SHALL 在创建完成后以只读形式暴露已加载的功能树，供渲染层消费；暴露的树 SHALL 与索引同源（同一次树源加载的结果），消费方 SHALL 无法经公开接口改变客户端内部树状态。

#### Scenario: 渲染层共享树

- **WHEN** 客户端创建完成且渲染层需要功能树
- **THEN** 渲染层从客户端直接取得该树，期间不发生额外的树端点请求

#### Scenario: 树只读

- **WHEN** 消费方尝试修改暴露的树
- **THEN** 客户端内部树状态不受影响，后续调用行为不变

### Requirement: 按 operationId 索引与调用定位

客户端 SHALL 以树中每个 function 的 `operationId` 为调用键建立索引；以未知 operationId 调用时 SHALL 以 `ApiError` 拒绝，错误消息指明未知键。

#### Scenario: 已知键调用

- **WHEN** 以树中存在的 operationId 调用
- **THEN** 客户端按该 function 内联的 method/path/parameters 发起 HTTP 请求

#### Scenario: 未知键调用

- **WHEN** 以树中不存在的 operationId 调用
- **THEN** 以 `ApiError` 拒绝，消息含未知的键名

### Requirement: 参数按内联声明自动绑定

调用参数 SHALL 按该 function 内联 `parameters[].in` 自动分拣：`path` 参数替换 URL 模板段，`query` 参数平铺序列化（数组按重复键展开），`body` 参数 JSON 序列化为请求体；调用方 SHALL 无需知晓目标接口的传输细节。请求体 SHALL 仅在该 operation 内联声明 requestBody 时发送。

#### Scenario: path 参数替换

- **WHEN** operation 的 path 含模板段（如 `/v1/users/{id}`）且调用提供了对应参数
- **THEN** URL 模板段被替换为参数值后发起请求

#### Scenario: query 参数平铺

- **WHEN** operation 的 parameters 含多个 query 参数
- **THEN** 按声明平铺拼入 query string（数组参数按重复键展开）

#### Scenario: body 序列化

- **WHEN** operation 内联声明了 requestBody 且调用提供了 body 参数
- **THEN** 请求体为该参数的 JSON 序列化，content-type 为 application/json

#### Scenario: 缺失 path 参数

- **WHEN** 调用缺少 operation 所需的 path 参数
- **THEN** 以 `ApiError` 拒绝，不发起请求

#### Scenario: 未声明参数忽略

- **WHEN** 调用提供了未在 parameters/requestBody 中声明的参数
- **THEN** 该参数被忽略，请求照常发起

### Requirement: 成功响应解析为数据

2xx 响应 SHALL 一律视为成功（RESTful 状态码判错，客户端 SHALL NOT 检查响应体内的业务错误码字段）；JSON 响应解析后以数据兑现，空响应体（204 或零长度）SHALL 兑现 `undefined` 且不视为解析失败。兑现值不含 HTTP 元数据，仅在管道内经 ctx 获取原始响应。

#### Scenario: 2xx JSON 响应

- **WHEN** 请求返回 200 与 JSON 响应体
- **THEN** 调用兑现为解析后的数据对象

#### Scenario: 空响应体

- **WHEN** 请求返回 204 或零长度响应体
- **THEN** 调用兑现为 undefined，不产生错误

#### Scenario: 2xx 不做业务码判错

- **WHEN** 2xx 响应体含任何业务相关字段
- **THEN** 一律按成功兑现，判错仅依据 HTTP 状态码

### Requirement: 失败统一为 ApiError 拒绝

非 2xx、网络失败与响应解析失败 SHALL 一律以 `ApiError` 拒绝；错误对象含 `status`（网络失败为 0）、`operationId`、`message`（自响应体 message 字段提取，缺失时回落状态文本）与 `fieldErrors`（响应体字段级错误对象原样透出，缺失时为空）。

#### Scenario: 非 2xx 带业务消息

- **WHEN** 请求返回 400 且响应体含 message 字段
- **THEN** 以 `ApiError` 拒绝，status 为 400，message 为响应体的 message 值

#### Scenario: 字段级错误透出

- **WHEN** 请求返回 400 且响应体含字段级错误对象
- **THEN** `ApiError.fieldErrors` 为该对象原样内容

#### Scenario: 网络失败

- **WHEN** 请求因网络不可达失败
- **THEN** 以 `ApiError` 拒绝，status 为 0

#### Scenario: 请求被取消

- **WHEN** 调用经 AbortSignal 取消
- **THEN** 以取消错误（AbortError）拒绝，不包装为 ApiError，与网络失败可区分

### Requirement: 可插拔洋葱管道

客户端 SHALL 支持注册多个 Handler 组成洋葱管道，执行顺序即注册顺序；Handler SHALL 可改写请求与响应、可短路（不经由下一层与最终 HTTP 调用直接兑现）；Handler 与客户端核心 SHALL NOT 返回或构造 UI 对象，管道兑现值 SHALL 恒为数据；Handler 抛出的非 ApiError 异常 SHALL 原样传播，不被包装。

#### Scenario: 链按注册顺序执行

- **WHEN** 依次注册 A、B 两个 Handler 后发起调用
- **THEN** A 的请求逻辑先于 B 执行，B 更贴近 HTTP 调用

#### Scenario: 请求改写

- **WHEN** 某 Handler 在请求下行中向请求头注入字段
- **THEN** 最终发出的 HTTP 请求携带该字段

#### Scenario: 短路

- **WHEN** 某 Handler 不调用 next 直接兑现响应
- **THEN** 不发生 HTTP 请求，调用以该响应兑现

#### Scenario: Handler 内部异常传播

- **WHEN** 某 Handler 抛出非 ApiError 的异常
- **THEN** 该异常原样传播至调用方，不被包装为 ApiError

### Requirement: 核心零魔法

客户端核心 SHALL NOT 内置聪明行为：401 与普通失败一致处理（登录流程由注册的鉴权 Handler 实现）；响应字段名 SHALL 保持后端原文（snake_case 不自动转换，转换由注册的 Handler 实现）。

#### Scenario: 401 未注册鉴权处理

- **WHEN** 请求返回 401 且未注册处理 401 的 Handler
- **THEN** 以 `status` 为 401 的 `ApiError` 拒绝，与普通失败无差别

#### Scenario: 字段名保持原文

- **WHEN** 响应体含 snake_case 字段（如 create_time）
- **THEN** 兑现数据中该字段名原样保留

### Requirement: 零框架依赖

客户端 SHALL 为纯 TypeScript 实现，不依赖任何 UI 框架；注入树源后 SHALL 可在 Node 环境完整运行（创建、调用、管道、错误契约全部成立）。

#### Scenario: Node 环境运行

- **WHEN** 在 Node 环境注入树源与 mock HTTP 层后发起调用
- **THEN** 创建、调用、管道执行与错误契约行为与浏览器环境一致
