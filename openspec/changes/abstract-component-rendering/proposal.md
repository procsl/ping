# Proposal: abstract-component-rendering

## Why

前端要走"前后端分离 + 抽象组件动态渲染"，但当前抽象组件这条线是断的，且存在两套互不兼容的 schema 平行生长：

- `ping-ui/doc/design.md` + `components/ui.json`：树形 `containers` 嵌套、`layout`、`router`
- `ping-system/ui/user-center/index.json`：扁平 `functions`、按 OpenAPI 中文 `summary` 挂载

机制（扫描 `ui/**/index.json`、绑定 OpenAPI、菜单接口）只存在于 `ping-system`，而组件 schema 的设计稿在已停用的 `ping-ui` —— 同一个功能被劈成两半，谁都无法单独完成渲染。`ping-system` 还反向硬读聚合层独占的 `ping-api-doc/openapi.json`，模块独立运行必然失败。

同时，"微前端嵌入"被评估为技术难度过高：它要求每个模块构建完整 SPA、独立路由栈、沙箱与跨应用通信。而实际需要的"动态引入"可以降到组件粒度，代价小一个量级。

## What Changes

- **统一抽象组件 schema，以 `ping-ui` 为准**：`ping-system/ui/user-center/index.json` 按新 schema 重写，旧的 `functions`/中文 summary 挂载方式作废。
- **`ping-ui` 成为抽象组件的公共依赖**：任何需要前端的业务模块 MUST 依赖 `ping-ui`；`ping-ui` 承载 schema 定义、扫描注册机制、菜单/组件树接口、渲染器与静态产物。业务模块只提供声明数据与可选的自定义组件。
- **启动时扫描 jar 资源完成注册**：扫描产物统一落位 `META-INF/ping/`，运行时只读固定路径，不做 `classpath*:` 通配扫描（配合变更 `standalone-module-packaging` 的构建期生成）。
- **OpenAPI 绑定改为构建期固化**：`ui/**/index.json` 中的 API 引用在打包期解析为完整定义写入 jar，运行时零 OpenAPI 依赖。关联键由中文 `summary` 改为 `@ResourceReference(name)` 稳定标识。
- **模块静态资源打包进各自 jar**：`META-INF/resources/<module>/` 下的薄壳 `index.html` + 可选自定义组件 JS。
- **访问路径约定**：散（独立模块）访问 `<module>/index.html`；聚（单体）访问根 `index.html` 主壳，`<module>/index.html` 仍可作为深链。同一份渲染器 bundle 服务两种形态。
- **组件粒度的动态加载取代微前端**：渲染器按组件 `type` 查内置注册表，未命中时动态 `import` 该组件 JS。**不做**微前端、不做独立路由栈、不做沙箱。
- **路由前缀与权限拦截**：路由由菜单声明数据驱动，前缀用于命名空间隔离与权限拦截点划分；静态资源放行，权限落在菜单层与 API 层。

## Capabilities

### New Capabilities

- `abstract-component-schema`: 抽象组件树的 schema 定义、节点类型、声明落位，及旧 schema 的作废。
- `component-registry`: 模块声明的扫描注册、构建期 OpenAPI 绑定固化、组件树/菜单查询接口。
- `component-rendering`: 渲染器职责、组件解析与组件粒度动态加载、命名空间与路由前缀。
- `module-static-assets`: 模块静态资源打包落位、薄壳入口生成、散/聚两种访问路径。

### Modified Capabilities

<!-- openspec/specs/ 当前为空，无既有能力需求变更 -->

## Impact

- **模块结构**：`ping-ui` 启用并承载机制；`ping-system` 移除 `domain/ui` 中的扫描与绑定机制（`UiSchemaRepository`、`UiSchemaService`、`ResourceReference` 迁移或重写），仅保留页面声明数据。
- **BREAKING**：`GET /v1/system/menus` 的响应结构随 schema 统一而变化；`GET /v1/ui/components` 与之合并或重定义。
- **数据**：`ping-system/src/main/resources/ui/user-center/index.json` 按新 schema 重写；`ping-ui/src/main/resources/components/ui.json` 作为 schema 样例对齐。
- **关联键**：API 引用从 `@Operation(summary)` 中文文案改为 `@ResourceReference(name)`，既有 summary 注解保留用于文档。
- **构建**：依赖变更 `standalone-module-packaging` 产出的 `META-INF/ping/` 约定；`ping-editor` 若重新启用，其 webpack 构建改输出到 `META-INF/resources/editor/`。
- **不影响**：后端 API 路径前缀约定（`/v1/...`）、数据库 schema、分页与 JSON 命名约定。

## 关联

- 前置变更：`standalone-module-packaging`（分层、`META-INF/ping/` 产物约定、打包期生成器、OpenAPI 分片落位）。
