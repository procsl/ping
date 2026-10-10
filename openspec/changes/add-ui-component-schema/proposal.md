# Proposal

## Why

`ping-ui/doc/design.md` 早已确立「抽象组件使用 JSON schema 描述组件的功能骨架，以实现布局与样式分离」以及「机制归属 `ping-ui`，业务模块只交 `ui/<page>/` 描述」，但至今没有一份成文的组件 schema 规范，机制代码反而分裂成两套互不相通的实现：

- `ping-system` 的 `cn.procsl.ping.boot.system.domain.ui`（`UiComponent` / `UiSchemaRepository` / `UiSchemaService` / `UIController` / `@ResourceReference`）用 `PathMatchingResourcePatternResolver` 扫 `classpath*:ui/**/index.json` —— 这正是架构要求明确禁止的 `classpath*:` 通配扫描，且机制长在业务层而非 `ping-ui`。
- `ping-ui` 的 `Component`（JPA 实体）/ `ComponentController` 硬编码读一个 `components/ui.json` 原样回吐，前端 `data/loader.ts` 的远程分支因此始终打在一段与描述模型无关的静态字符串上。

两套的 API 引用方式也互相矛盾（`ping-system` 用 OpenAPI `summary` 字符串反查，`ping-ui/schema/types.ts` 用 `method + path`）。结果是：规范没有权威来源，业务模块无从声明页面，表格/查询参数/展示列/表单等最常用的后台组件缺乏统一语义，新增一个组件类型要同时改两处后端加一处前端，且没有任何校验。

## What Changes

- **新增抽象组件规范描述文档**：定义抽象组件描述文件（`ui/<page>/compose.json`）的格式、组件类型定义结构、属性与子节点槽位、组合嵌套规则、API 引用与 schema 契约。
- **新增 16 个常用后台管理抽象组件类型定义**（每类型一份 JSON 描述）：
  - 导航类 6 个：`application` `layout` `menu` `nav_group` `tabs` `breadcrumb`
  - 数据类 4 个：`dataset` `query` `column` `row_action` —— 按「展示类的表可理解为数据库的表」建模，`dataset` 即逻辑数据表，`query` 是该表的输入参数，`column` 是展示列（可附排序等行列级功能）；**描述只表达语义，不限制前端渲染形态**
  - 表单与输入类 3 个：`form` `field` `form_action`
  - 其他 3 个：`action` `placeholder` `empty`
- **构建期聚合**：将组件类型定义与各模块 `ui/<page>/compose.json` 聚合为单个固定索引产物，落 `META-INF/ping/`，运行时只读该固定路径（消除 `classpath*:` 通配扫描）。
- **启动期装配进内存**：启动后读固定索引，结合 `ping-api-doc/openapi.json` 把 `@ResourceReference` 引用替换为含入参、出参 schema 的完整 API 描述，装入内存注册表。入参成为组件的输入参数，出参成为组件的渲染数据契约。
- **`@ResourceReference` 迁移**：由 `ping-system` 迁至 `ping-ui`，机制统一归口基础层；`ping-system` 退化为只提供 `ui/<page>/compose.json` 与被标注的接口。
- **接口改走内存注册表**：`GET /v1/ui/components` 返回内存注册表中的抽象组件。
- **前端接通**：页面加载后通过接口加载抽象组件，`components/renderer/registry.tsx` 为 16 个类型提供渲染实现，未知类型降级占位；保留本地 `default-tree.ts` 作为接口不可用时的降级源。
- **BREAKING** 移除硬编码与 JPA 依赖：删除 `ping-ui/src/main/resources/components/ui.json` 的静态回吐、`ui/domain/Component` 实体及其 `@EntityScan` / `@EnableJpaRepositories` 开关、`ComponentRecord`、`ComponentMapper`。
- **BREAKING** 移除或委托 `ping-system` 的 `GET /v1/system/menus` 与 `UiComponent` / `UiSchemaRepository` / `UiSchemaService`，由 `ping-ui` 统一承载；`ping-system` 需新增对 `ping-ui` 的依赖。
- **BREAKING** 描述文件名由文档中的 `ui/<page>/index.json` 统一为 `ui/<page>/compose.json`，同步修订 `ping-ui/doc/design.md` 与 `AGENTS.md` 的约定。

## Capabilities

### New Capabilities

- `ui-component-schema`: 抽象组件的描述模型与系统行为 —— 描述文件格式、16 种常用后台管理组件类型的语义与槽位规则、组合嵌套、`@ResourceReference` 到 OpenAPI 的 API 引用解析、启动期内存注册表、组件描述接口契约、前端按类型加载渲染并降级。

### Modified Capabilities

（无。既有 `admin-navigation` 的 4 条需求不变：菜单数据源改为后端装配后，`data/loader.ts` 仍保留本地降级，「数据可来自内置的本地假数据而不依赖后端接口」这条允许性描述不被违反。）

## Impact

- **代码**：`ping-ui`（新增 schema 资源、装配与注册表、`UIAutoConfiguration` 改造、前端 loader / registry 扩展）；`ping-system`（移除 `domain/ui`、`api/ui`，`pom.xml` 增加 `ping-ui` 依赖，`ui/user-center/index.json` 改名为 `compose.json`）；`ping-distribute`（依赖已含 `ping-ui`，无结构变化）。
- **API**：`GET /v1/ui/components` 返回结构由静态字符串变为组件注册表 —— **BREAKING**；`GET /v1/system/menus` **BREAKING**（移除或委托）。
- **数据模型**：`ui_component` / `ui_component_tags` 表不再被实体引用（`ddl-auto=update` 不回滚表结构，仅停止生成）。
- **构建**：`ping-ui` 与 `ping-distribute` 的 `package` 增加索引聚合步骤；`-Dping.npm.skip=true` 时需保证聚合步骤不依赖前端产物。
- **依赖方向**：`ping-system`（业务层）→ `ping-ui`（基础层）为新增依赖，方向与架构要求一致；`ping-ui` 不得反向依赖任何业务模块。
- **文档冲突（显式呈现，不代为裁定）**：
  - 原始请求要求「通过 classpath 扫描抽象组件 JSON」，而 `AGENTS.md` 架构要求「统一落 `META-INF/ping/`，运行时只读该固定路径，禁 `classpath*:` 通配扫描」。已确认按架构要求走「构建期生成索引 + 运行时读固定路径」。
  - 启动期读取 `ping-api-doc/openapi.json` 与 `AGENTS.md`「OpenAPI 打包期按模块生成，运行时不读」存在张力：现有 `ping-system` 代码即为运行时读取，本变更沿用该行为并在 design 中说明；若要求严格满足，需把 API 引用替换前移到打包期（另议）。
  - `AGENTS.md` 中「ping-ui 已停用」的描述与代码不符（`ping-parent/pom.xml:21` 已启用、`ping-distribute/pom.xml:58` 已依赖），本次一并校正。
