# Design

## Context

动机见 `proposal.md - Why`。此处只列影响方案选择的现状与硬约束。

**现状**

- `ping-ui` 已具备前端骨架：`schema/types.ts` 定义 `ComponentNode`，`components/renderer/registry.tsx` 是内置渲染注册表，`data/loader.ts` 已实现「远程优先、失败降级本地」，`router/derive.ts` 从树推导菜单与路由。
- 前端现有词汇与本次 16 个类型的差集：现用 `table`（→ `dataset`）、`apis: ApiRef[]`（→ `api` 单字段）、`user_info_panel`、`main_container` 不在 16 个类型内；新增 `query` `row_action` `form` `field` `form_action` `nav_group` `tabs` `breadcrumb` `empty` 目前无前端实现。
- `ping-system` 的 `SystemAutoConfiguration` 在 `openapi.export=true` 时把 OpenAPI 导出为 `openapi.output.dir` 指定目录下的 `openapi.json`；`ping-distribute` 在 `prepare-package` 用 `exec-maven-plugin` 起真实应用并传 `--openapi.output.dir=${project.build.outputDirectory}/ping-api-doc/`，因此**打包产物内固定存在 `ping-api-doc/openapi.json`**。默认构建（不带 `ping-distribute package`）无此文件。
- 现有 `UiSchemaRepository` 运行时读该文件并按 `summary` 反查；`ping-ui` 的 `ComponentController` 运行时读硬编码 `components/ui.json`。

**约束**（`AGENTS.md` 架构要求）

- 依赖三层：`ping-common` / `ping-web` / `ping-jpa` / **`ping-ui`** 属基础层，`ping-system` 属业务层，业务层依赖基础层合法，反向禁止。
- 「构建期产物统一落 `META-INF/ping/`，运行时只读该固定路径，**禁 `classpath*:` 通配扫描**」——本变更的聚合方案必须满足。
- GraalVM：`src/main/java` 禁 `Class.forName(String)`、`setAccessible`、`Proxy.newProxyInstance`。
- 配置只 properties；资源过滤定界符为 `@...@`；JSON 字段 SNAKE_CASE。
- 业务模块 pom 不写 `<mainClass>`；业务模块 `src/main` 不得自带 `main`。

## Goals / Non-Goals

**Goals**

- 一份权威的抽象组件规范文档与 16 个常用后台管理组件类型的 JSON 定义，作为组件模型的唯一事实来源。
- 单一装配链路：构建期聚合 → 启动期一次性装配进内存注册表 → 接口下发 → 前端按类型渲染；运行时不出现任何 classpath 通配扫描。
- 散（业务模块独立 jar）与聚（`ping-distribute` 大单体）两种打包形态下，注册表内容一致且完整。
- 在 `openapi.json` 缺失、装配描述损坏、类型未知、接口不可用等情况下**一律降级并告警，启动永不失败**。
- 既支持 `./mvn -Ph2 test`（不打包），也支持 `./mvn -Ph2 -DskipTests package` 与 `-Dping.npm.skip=true`。

**Non-Goals**

- 不做组件描述的运行时热重载、后台编辑或版本协商。
- 不做组件级权限、多租户、国际化。
- 不改变现有前端演示数据的取数实现：本变更把接口出入参结构**交付**给前端作为渲染数据契约，`dataset` 仍可先用本地演示行渲染，真实取数另议。
- 不迁移既有 `ping-api-doc/openapi.json` 到 `META-INF/ping/`（属既有约定，见 Open Questions）。
- 不重写 `admin-navigation` 的既有交互行为（展开/折叠、搜索、激活态）。
- 不引入微前端或子应用拆分。

## Decisions

### D1 描述文件的位置与命名

| 角色 | 位置 | 提供方 |
|---|---|---|
| 页面装配描述（源） | `src/main/resources/ui/<page>/compose.json` | 各业务模块 |
| 组件类型定义（源） | `src/main/resources/META-INF/ping/ui/types/<type>.json` | `ping-ui` |
| 聚合索引（构建期产物） | `META-INF/ping/ui/components.json` | 各模块构建期生成 |

聚合索引为单一文件，内含全部组件类型定义与全部页面装配树，按 `page` 键与 `type` 键去重。

**装配归宿规则**（顶层 `type` 决定）：

| 顶层 `type` | 归宿 |
|---|---|
| `application` | 作为应用树进入 `pages`；按 `id` 去重，约定仅 `ping-ui` 提供这一棵 |
| `layout` `nav_group` `menu` `breadcrumb` `tabs` | 导航片段，按 `order` 合并进应用外壳树的对应方位（`layout` 按方位并入，其余并入 `layout[left]`） |
| 其它任意类型 | 页面片段，按顶层节点的 `router` 挂载到外壳树中 `router` 相同的 `menu` 节点 `containers` 末尾；**无匹配时不报错**，原样进入 `pages`，前端按其 `router` 自行路由 |

该规则由 `UIComponentRegistry` 在启动装配时执行，对应 spec 的「装配描述按顶层类型归宿」。

- **备选 A：源直接放 `META-INF/ping/`** —— 否。违背「业务模块只交 `ui/<page>/…`」的既有约定，且 `META-INF/ping/` 语义是构建期产物而非开发期输入。
- **备选 B：每模块一个固定名贡献文件，运行时逐个读取** —— 否。运行时无法枚举模块清单，等价于通配扫描。
- **备选 C：构建期生成 Spring `@Configuration` bean，运行时收集 `List<ComponentSource>`** —— 可行且 GraalVM 最友好，但需要为每个模块生成类并解决跨模块 bean 注册；列为备选，未采纳。采纳的 A 路径更贴合用户已确认的「构建期生成索引 + 运行时读固定路径」。

### D2 聚合的执行者与时机

- 执行者：`gmavenplus-plugin` + groovy 脚本 `ping-ui-components-aggregate.groovy`，挂在 `process-classes`。项目已有 `build-profile-filter.groovy` 同款先例，无需新增模块。
- 输入：
  1. 本模块 `target/classes/ui/**/compose.json`；
  2. 依赖构件内的 `META-INF/ping/ui/components.json` —— 由 `project.artifacts` 解析得到的 classpath 元素（目录或 jar）逐一按该**精确路径**读取，**缺失即跳过**。
- 输出：`target/classes/META-INF/ping/ui/components.json`。
- 关键点：**构建期枚举依赖构件是允许的**，禁令只约束运行时。同一模块在 `mvn test` 下其依赖解析为 `target/classes`，在 `mvn package` 下解析为已打包 jar，两种形态都包含同一固定路径的聚合索引（`ping-ui` 排在 `ping-system` 之前由新增依赖边保证）。
- **备选：新建 maven 插件** —— 否，新模块 + 跨 reactor 版本管理成本。**备选：`exec-maven-plugin` 跑 Java main** —— 否，需要先完成编译，phase 更靠后且与 `mvn test` 不兼容。

### D3 启动期装配与内存注册表

`UIComponentRegistry` 为单例 bean，在**所有单例就绪后**（`SmartInitializingSingleton.afterSingletonsInstantiated()`，实现期由 `InitializingBean` 调整）一次性装配，产出不可变快照（`List.copyOf`）。装配读取两个**固定路径**：

1. `META-INF/ping/ui/components.json` —— 类型定义与页面装配树；
2. `ping-api-doc/openapi.json` —— 打包期导出的 OpenAPI 文档（可缺失）。

同时注入 Spring MVC 的 `RequestMappingHandlerMapping`，遍历带 `@ResourceReference` 的 handler 方法建立 `name → (HTTP method, path)` 索引，用于判定「该引用是否合法」。

> 生命周期选 `SmartInitializingSingleton` 而非 `InitializingBean`：`RequestMappingHandlerMapping` 只在自身 `afterPropertiesSet()` 时扫描 handler，若在注册表构造期间被过早创建，会漏掉尚未就绪的 `@Controller`。推迟到全部单例实例化之后可保证索引完整，外部可观察行为（启动后一次性装配、之后只读）不变。

`api` 字段解析规则：值形如 `@ResourceReference:<name>` → 用注解索引定出 method/path → 从 OpenAPI 文档取对应 operation → 就地替换为 `{method, path, request, response}`；任一环节失败则**保留原值并记录告警**。

- **备选：沿用现状按 OpenAPI `summary` 反查** —— 否。`summary` 与注解无强绑定，重名即静默错绑，且无法表达「未标注的接口不可被引用」。
- **备选：只读 OpenAPI、不查注解** —— 否，同上。
- **备选：把替换前移到打包期** —— 更贴合「OpenAPI 运行时不读」，但 `openapi.json` 由 `ping-distribute` 在 `prepare-package` 才导出，早于 `process-classes`，散包模块拿不到完整文档；张力已在 `proposal.md - Impact` 记录，留待后续（见 Open Questions）。

所有异常（索引缺失、描述损坏、OpenAPI 缺失、引用未命中）均降级为告警，装配继续，启动不失败——对应 spec 中「打包产物缺少聚合索引」「装配描述不是合法 JSON」「打包期文档缺失」「引用目标不存在」四个场景。

### D4 接口契约

`GET /v1/ui/components`，`produces = application/json`，返回：

```json
{ "types": [ /* 组件类型定义 */ ], "pages": [ /* 装配后的组件树 */ ] }
```

注册表为空时返回 `{"types":[],"pages":[]}` 与 HTTP 200。**BREAKING**：现有接口返回的是 `components/ui.json` 的原始文本。

前端 `loadManifest` 相应改为读取 `pages[0]`，并对裸树响应保留兼容分支，本地 `default-tree.ts` 降级路径不受影响。

- **备选：类型定义与页面树分成两个接口** —— 否，前端一次取全更省请求，且注册表为只读快照，无并发分页诉求。
- **备选：分页查询接口** —— 否，组件量级小，且前端需要全树推导菜单与路由。

### D5 组件词汇的迁移

- **命名约束**：全部组件类型名与 JSON 属性名一律 **snake_case**（AGENTS.md「JSON SNAKE_CASE」），与既有的 `user_info_panel` / `main_container` 一致；控件类型同理（`date_picker` 而非 `date-picker`）。
- 16 个常用后台管理抽象组件类型：`application` `layout` `menu` `nav_group` `tabs` `breadcrumb` / `dataset` `query` `column` `row_action` / `form` `field` `form_action` / `action` `placeholder` `empty`。
- 另有 **2 个框架壳体类型** `user_info_panel`、`main_container` 随内置集合一并发布。它们不是「常用后台管理抽象组件」，故不计入 16；spec 的「返回内容包含上述全部 16 种类型的定义」仍成立。
- `table` → `dataset`（**BREAKING**）：`default-tree.ts`、`data-table.tsx` 同步改名，不做别名兼容——`components/ui.json` 本就删除，`default-tree.ts` 在同一变更内改写，仓库内无外部调用方。
- `apis: ApiRef[]` → `api` 单字段（**BREAKING**）：一个节点绑定一个数据契约，需要多接口时用子节点表达（这与「组件可组合嵌套」一致）。`ApiRef` 结构由 `{method, api}` 改为 `{method, path, request, response}`。
- 语义分工：`action` 为工具栏/页面级动作，`row_action` 为数据表行内动作；现有 `dataset` 子节点中的 `action` 语义保持不变。

### D6 `@ResourceReference` 的迁移

- 新位置：`cn.procsl.ping.boot.ui.component.api.ResourceReference`，与组件接口同包。
- `ping-system/pom.xml` 新增 `ping-ui` 依赖（业务层 → 基础层，方向合规），该依赖同时保证 reactor 中 `ping-ui` 先于 `ping-system` 构建。
- OpenAPI 导出逻辑（`SystemAutoConfiguration` 的 `openapi.export` 分支）**留在 `ping-system`**，它属文档导出而非组件机制；`ping-ui` 只按固定路径读取产物，不依赖 `ping-system` 的类型。
- 删除 `ping-system` 的 `domain/ui`（`UiComponent` `UiSchemaRepository` `UiSchemaService` `ResourceReference`）、`api/ui/UIController`；`GET /v1/system/menus` **BREAKING**，由 `GET /v1/ui/components` 承载。
- 删除 `ping-ui` 的 `ui/domain/Component`、`ui/component/api/ComponentRecord`、`ComponentMapper`、`ComponentController` 的硬编码读取分支，以及 `UIAutoConfiguration` 上的 `@EntityScan` / `@EnableJpaRepositories`；`ping-ui` 的 `ping-jpa` 依赖暂保留，避免影响 `ping-distribute` 的实体扫描配置。

### D7 前端渲染

- `registry.tsx` 扩展至 16 + 2 个类型；未知类型维持现有降级为占位节点的行为。
- `data-table.tsx` → `dataset` 渲染：从 `containers` 分离 `column`（展示列，可携带排序能力）、`action`（工具栏）、`row_action`（行内动作）；`query` 子节点渲染为该数据表的输入参数表单。
- `field` 按字段类型映射到既有 `components/ui/*`（`input` `select` `date_picker` 等），未映射到的字段类型同样降级占位。
- `form` / `form_action` 复用 `components/ui/button` 等既有原子组件。
- 保持「前端可自主决定渲染形态」：`dataset` 的描述只提供语义与数据契约，渲染器可换成卡片、看板等，描述与接口返回不变。

## Risks / Trade-offs

- **聚合结果依赖 reactor 构建顺序与依赖构件已产出** → 缓解：从 `project.artifacts` 解析、缺失即跳过并告警；spec 显式规定「打包产物缺少聚合索引 → 空集合、启动不失败」；用例覆盖 `mvn test`（依赖为 `target/classes`）与 `mvn package`（依赖为 jar）两种形态。
- **`mvn -pl` 单模块构建时注册表可能不完整** → 缓解：文档说明，聚合按可得构件尽力而为。
- **默认构建无 `openapi.json`** → 缓解：引用保持原样 + 告警，组件仍可装配与渲染；只有数据契约缺失。
- **运行时读 `ping-api-doc/openapi.json` 与 `AGENTS.md`「运行时不读」存在张力** → 已在 `proposal.md - Impact` 显式呈现，未代为裁定；见 Open Questions。
- **多处 BREAKING（接口结构、`/v1/system/menus`、`apis`→`api`、`table`→`dataset`、`index.json`→`compose.json`）** → 缓解：同一变更内同步改前端与 `ping-system`，仓库内无其他调用方；`ui_component` 表本就未写入数据，删除实体无数据损失。
- **类型定义与前端渲染实现漂移** → 缓解：集成测试断言接口返回的 16 个类型全部存在于前端注册表；`-Dping.npm.skip=true` 时该断言跳过。
- **groovy 聚合脚本的失败模式不明显** → 缓解：脚本内对每类失败打印告警而非抛出；对「描述文件非法 JSON」的场景单独留测试。

## Migration Plan

1. **依赖与注解迁移**：`ping-system/pom.xml` 增加 `ping-ui`；`@ResourceReference` 移入 `ping-ui`，`ping-system` 改 import。此步完成即可编译，行为不变。
2. **规范与类型定义**：`ping-ui` 新增规范文档与 16 + 2 个类型定义 JSON；新增聚合脚本并挂 `process-classes`。
3. **装配链路**：实现 `UIComponentRegistry`（读固定路径 + 注解索引 + OpenAPI 解析），改写 `ComponentController` 为读注册表；删除 JPA 实体与硬编码 `components/ui.json`。
4. **收敛 ping-system**：`ui/user-center/index.json` → `compose.json`；删除 `domain/ui`、`api/ui`；`GET /v1/system/menus` 下线。
5. **前端改造**：`types.ts`（`api` 字段）、`default-tree.ts`（`table`→`dataset`）、`loader.ts`（读 `pages[0]`）、`registry.tsx` 与 `components/renderer/*` 扩展到 16 + 2 类型。
6. **文档同步**：`ping-ui/doc/design.md`、`ping-ui/doc/test.md`、`AGENTS.md`（`compose.json` 新约定、校正「ping-ui 已停用」）。

**回滚**：按提交逐步回退即可，无数据迁移，无状态残留（内存注册表与 `ui_component` 表均无写入）。

**验证**：`./mvn -Ph2 test`；`./mvn -Ph2 -DskipTests package`；`cd ping-ui && npm run build`；起 `ping-distribute` 后访问 `/index.html`、`/v1/ui/components`，并确认默认构建（无 `openapi.json`）下仍能启动与渲染。

## Open Questions

- `ping-api-doc/openapi.json` 是否应迁入 `META-INF/ping/`？触及既有约定，且会让 D3 的读取路径更规范，但不在本变更范围。
- `user_info_panel` / `main_container` 是否长期保留为壳体类型，或归并进 `layout` 的方位槽？不影响本变更的验收。
- 是否需要构建期对 `compose.json` 做 schema 校验（防错在构建期而非启动期）？可作为后续增强。
