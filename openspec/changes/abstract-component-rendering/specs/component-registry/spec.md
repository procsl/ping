# Capability: component-registry

模块声明的扫描注册、构建期 API 绑定固化，及组件树查询接口。

## ADDED Requirements

### Requirement: 启动时必须扫描各 jar 完成注册

应用启动时 SHALL 枚举类路径上所有 `META-INF/ping/ui-index.json` 并求并集，据此完成页面注册。注册结果 SHALL 只取决于 classpath 内容，MUST NOT 依赖聚合层维护的模块清单或配置项。

#### Scenario: 谁在 classpath 谁就注册

- **WHEN** 仅引入 `ping-product` 与其基础层依赖启动
- **THEN** 组件树只含 `ping-product` 声明的页面

#### Scenario: 聚合引入多个模块

- **WHEN** 聚合层同时引入 `ping-product` 与 `ping-system` 启动
- **THEN** 组件树包含两者的页面，且无遗漏、无重复

#### Scenario: 不需显式登记

- **WHEN** 新增一个业务模块并加入聚合 pom 依赖
- **THEN** 无需修改任何模块清单配置即可在组件树中出现

### Requirement: 扫描必须使用固定资源路径

注册阶段 SHALL 只对固定路径 `META-INF/ping/ui-index.json` 调用资源枚举，MUST NOT 使用 `classpath*:` 通配模式定位声明文件。该路径 SHALL 通过 `RuntimeHintsRegistrar` 注册为 native 资源 hint。

#### Scenario: 无通配扫描

- **WHEN** 搜索运行时源码中的 `classpath*:` 表达式
- **THEN** 不存在用于定位 `ui-index` 的通配模式

#### Scenario: native 资源 hint 存在

- **WHEN** 检查 AOT 处理产出的资源 hint
- **THEN** `META-INF/ping/ui-index.json` 已被注册

### Requirement: API 绑定必须在构建期固化

页面声明中的 API 引用 SHALL 在打包期解析为完整定义并写入 `META-INF/ping/ui-index.json`。运行时 MUST NOT 读取 OpenAPI 文档，MUST NOT 依赖聚合层独占的文档产物。

#### Scenario: 运行时无 OpenAPI 依赖

- **WHEN** 检查运行时代码中的 OpenAPI 读取点
- **THEN** 不存在 `openapi.json` 或 `ping-api-doc` 的读取

#### Scenario: 产物自带绑定

- **WHEN** 解压业务模块产物 jar 并读取 `ui-index.json`
- **THEN** API 引用节点已含完整定义（请求路径、方法、请求体与响应结构）

#### Scenario: 打包时序失败降级

- **WHEN** 打包期 OpenAPI 生成被 `-Dping.openapi.skip` 跳过
- **THEN** 构建仍成功，`ui-index.json` 中相应节点为未绑定状态并输出告警

### Requirement: API 关联键必须使用稳定标识

页面声明 SHALL 通过 `@ResourceReference` 的稳定标识关联后端接口，MUST NOT 以 `@Operation(summary)` 的中文文案作为关联依据。`@Operation(summary)` SHALL 保留用于接口文档展示。

#### Scenario: 标识关联

- **WHEN** 页面声明引用标识 `system.user.list`
- **THEN** 绑定阶段在 `@ResourceReference` 中定位到该标识对应的接口，且该接口的 `summary` 变更不影响绑定

#### Scenario: 标识重复

- **WHEN** 两个接口声明了相同的 `@ResourceReference` 标识
- **THEN** 打包期绑定失败并报告重复标识

#### Scenario: 引用不存在的标识

- **WHEN** 页面声明引用了未声明的标识
- **THEN** 打包期绑定失败并报告该标识

### Requirement: 必须提供组件树查询接口

系统 SHALL 提供 `GET /v1/ui/menus` 返回按当前用户权限过滤后的完整组件树，响应为 JSON。原 `GET /v1/system/menus` MUST 移除。

#### Scenario: 查询组件树

- **WHEN** 已登录用户请求 `GET /v1/ui/menus`
- **THEN** 返回 200 与组件树 JSON，字段为 schema 定义的结构

#### Scenario: 未登录访问

- **WHEN** 未登录用户请求 `GET /v1/ui/menus`
- **THEN** 按既有认证拦截规则返回未登录错误

#### Scenario: 旧接口不存在

- **WHEN** 请求 `GET /v1/system/menus`
- **THEN** 返回 404

#### Scenario: 空注册

- **WHEN** classpath 中无任何 `ui-index.json`
- **THEN** 接口返回 200 与空组件树，而非错误

### Requirement: 机制必须由 ping-ui 承载

扫描注册、`@ResourceReference` 注解、schema 类型与组件树接口 SHALL 全部位于 `ping-ui`。`ping-system` MUST NOT 保留上述任一实现，仅保留自身页面声明数据。

#### Scenario: ping-system 无机制残留

- **WHEN** 检查 `ping-system/src/main/java` 中的 `domain/ui` 包
- **THEN** 不存在 `UiSchemaRepository`、`UiSchemaService`、`ResourceReference`

#### Scenario: 业务模块依赖 ping-ui 即可用

- **WHEN** 任一业务模块仅依赖 `ping-ui` 而不依赖 `ping-system`
- **THEN** 该模块的页面声明可被扫描注册并经由 `/v1/ui/menus` 返回
