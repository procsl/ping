# Phase 2: 后端聚合接口

## 任务范围概述

实现后端 `GET /v1/ui/components` 聚合接口，返回编译产物（type:app 根）。对应 tasks 3.x。

## 逐任务实现细节

### 3.1 GET /v1/ui/components 端点

调用 `ComposeCompiler.compile()` 的结果（内存只读快照），序列化为 JSON 返回。启动时编译一次，运行期不重编译直到重启。

验证：curl 返回 200 + 合法 JSON

### 3.2 注册到 UIAutoConfiguration

`UIAutoConfiguration` 注册 controller + 编译产物 bean，`AutoConfiguration.imports` 已登记。

验证：启动应用后 curl 可访问

### 3.3 打包导出模式跳过编译

`openapi.export=true` 时跳过组件编译与端点注册（否则首次构建时 openapi 文档尚未生成会自锁）。除此模式外无豁免。

验证：单测覆盖导出模式不编译

### 3.4 缺 OpenAPI 输入报错

存在 compose.json 却无任何 `META-INF/ping/openapi-<module>.json` → 启动失败。

验证：单测覆盖缺输入报错

## 边界与降级契约

1. **无 compose.json**：返回 200 + 空 app 树（children 空）
2. **编译失败**：启动阶段即失败，进程非 0 退出，**不**在请求时返回 500（启动失败语义，见 design D8）
3. **鉴权**：跟随现有全局登录拦截器，未登录 401，不加 `@PermitAll`

## 验证方式

1. 单元测试：controller 单测
2. 集成测试：端到端（启动 → curl `/v1/ui/components` → 合法 JSON）
