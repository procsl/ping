# Phase 2: 后端聚合接口

## 任务范围概述

实现后端 `GET /v1/ui/components` 聚合接口，返回编译后的完整 JSON。

## 逐任务实现细节

### 2.1 实现 GET /v1/ui/components 端点

调用 `ComposeService.compile()` 返回 `App`，直接序列化为 JSON 返回。

验证：curl 返回 200 + 合法 JSON

### 2.2 实现 platform 过滤

按请求参数 `platform=web|desktop` 过滤不支持的组件。

验证：单测覆盖不同平台返回不同子集

### 2.3 注册到 UIAutoConfiguration

确保 `/v1/ui/components` 在 Spring Boot 启动后可访问。

验证：启动应用后 curl 可访问

## 边界与降级契约

1. **无组件**：返回空 JSON 数组
2. **编译失败**：返回 500 错误，包含具体错误信息

## 验证方式

1. **单元测试**：`UiController` 的单元测试
2. **集成测试**：端到端测试，验证完整链路
