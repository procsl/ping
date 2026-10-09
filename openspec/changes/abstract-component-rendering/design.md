# Design: abstract-component-rendering

## Context

**现状**

```
ping-ui（已停用，挂 ping-parent）
  doc/design.md            抽象组件设计稿：id / name / events / containers
  resources/components/ui.json   应用壳 schema：application→layout→menu/user_info_panel
  /v1/ui/components        单读 components/ui.json
  domain/{Component, ...}  JPA 实体 + @RepositoryCreator

ping-system（在用）
  domain/ui/UiSchemaRepository    classpath*:ui/**/index.json  通配扫描
  domain/ui/UiSchemaService       读 ping-api-doc/openapi.json 按中文 summary 绑定
  domain/ui/ResourceReference     已存在，目前仅作装饰
  api/ui/UIController             GET /v1/system/menus
  resources/ui/user-center/index.json   type/functions/api 结构
```

两套 schema 结构完全不同（`containers`/`layout` vs `functions`/`api`），机制只在 `ping-system`，设计稿在 `ping-ui`，且 `ping-system` 反向依赖聚合层独占的 `ping-api-doc/openapi.json`。

**已确认的前提**

- 微前端技术难度过高，否决；改为抽象组件 + 后端动态渲染。
- schema 以 `ping-ui` 为准，`ping-system` 的旧结构作废（后者仍在规划期，作废成本低）。
- OpenAPI 文档是开发时产物，由 `standalone-module-packaging` 在打包期按模块生成到 `META-INF/ping/openapi/<module>.json`。
- 扫描注册发生在**启动时**（谁在 classpath 谁就注册），但实现必须对 GraalVM 友好。
- 关联变更已把构建期生成收敛到 `META-INF/ping/` 固定落位。

## Goals / Non-Goals

**Goals:**

- 定义唯一一套抽象组件 schema，可同时表达应用壳与页面内容。
- `ping-ui` 成为需要前端的模块的公共依赖，承载机制；业务模块只交数据。
- 启动时扫描各 jar 完成注册，运行时零 glob、零 OpenAPI 依赖，GraalVM native 可用。
- API 关联键从中文文案改为稳定标识。
- 散/聚两种形态共用同一份渲染器，散访问 `<module>/index.html`，聚访问根 `index.html`。
- 以组件粒度的动态加载替代微前端。

**Non-Goals:**

- 不实现微前端、不引入 iframe/沙箱/独立路由栈。
- 不为每个业务模块构建完整 SPA；不引入每模块 node 构建的强制要求（仅有自定义组件时才需要）。
- 不改后端 API 路径约定（`/v1/...`）、不改数据库 schema、不改分页与 JSON 命名约定。
- 不补齐各业务模块的页面声明内容（模块尚未搭建完成）。

## Decisions

### D1. Schema 以 `ping-ui` 为准，页面内容纳入同一节点模型而非另立体系

**选择**：采用 `ping-ui` 的节点模型——`type` / `id` / `name` / `containers`（或叶子的 `children`）/ `layout` / `router` / `order` / `apis`。`ping-system` 的页面内容表达（表格、列、动作）**改为该模型下的叶子组件节点**（`type: "table"`、`type: "column"`、`type: "action"`），不再使用 `functions` 嵌套与中文 `api` 挂载。

**理由**：用户明确"以 ping-ui 为准，其他作废"。但 `ping-ui` 现有 schema 只描述**应用壳**（layout/menu），`ping-system` 描述的是**页面内容** —— 两者是同一棵树的不同层级，不是竞争关系。把页面内容降级为叶子节点，一套 schema 同时覆盖，避免再造第三套。

**替代方案**：

- 完全照搬 `ping-ui/components/ui.json` 不做扩展 —— 无法表达页面内容，业务模块无法声明。
- 保留 `ping-system` 的 `functions` 结构 —— 违背"作废"决定，且两套并存的根因未消除。

**待定细节**：`type` 取值域（内置组件清单）由渲染器实现阶段确定，schema 层只约束结构与必填字段。

### D2. 扫描注册：启动时并集，扫描固定文件名而非通配

**选择**：`ping-apt` 在编译期把本模块全部页面声明规范化并汇总为**单个固定文件名** `META-INF/ping/ui-index.json`。启动时用 `PathMatchingResourcePatternResolver.getResources("META-INF/ping/ui-index.json")` 枚举所有 jar 的副本并求并集。

**理由**：

- 语义满足"启动时扫描 jar 中的资源实现后端注册"——谁在 classpath 谁就出现，无需在 pom 中登记模块清单。
- 与 GraalVM 兼容：只需 `RuntimeHintsRegistrar` 注册**一个固定资源路径**，无需 `ui/**` 通配 glob（后者在 native 下依赖模式匹配，易漏）。
- 与 D5 的构建期绑定天然衔接：解析好的内容直接进这一个文件。

**替代方案**：

| 方案 | 弃用理由 |
|---|---|
| 运行时 `classpath*:ui/**/index.json`（现状） | 通配模式在 native 下需逐模式注册，且把"解析"留在运行时 |
| 聚合层生成单一总清单 | 业务模块独立运行时无总清单 → 违背散的要求 |
| 模块在 pom 中显式登记到聚合清单 | 破坏"谁在 classpath 谁就注册"，每加模块要改聚合 pom |

### D3. OpenAPI 绑定在打包期固化，关联键改用 `@ResourceReference`

**选择**：

1. 打包期顺序：生成 `<module>.json`（OpenAPI）→ 解析 `ui-index` 声明中的 API 引用 → 把完整 `apiDefinition` 写入 `META-INF/ping/ui-index.json`。
2. 关联键从 `@Operation(summary)` 中文文案改为 `@ResourceReference` 的稳定标识（`name` 为展示名，新增 `value` 为标识，默认取 `name`）。
3. 运行时 `UiSchemaService` 不再读 OpenAPI，只读 `META-INF/ping/ui-index.json`。

**理由**：用户明确"OpenAPI 是开发时使用"。构建期固化同时解决：运行时零 OpenAPI 依赖、native 零反射、`ping-api-doc` 单点消失、关联键可重构。

**这同时回答了 `standalone-module-packaging` 的一个 open question**：聚合层不需要合并 OpenAPI —— 各模块分片自带绑定，聚合时只是 `ui-index` 分片的并集；`_aggregate.json` 仅供开发查阅，不参与运行时绑定。

**替代方案**：运行时读 openapi（现状）—— 与"开发时产物"定位矛盾，且散跑必炸；index.json 写死 `path`+`method` —— 丢掉从后端注解自动推断的能力。

### D4. `ping-ui` 承载机制，`ping-system` 只留数据

**选择**：迁移并合并：

```
ping-ui（基础层，启用）
  schema 定义（Java 类型 + 校验）
  META-INF/ping/ui-index.json 扫描与并集
  @ResourceReference 注解
  GET /v1/ui/menus        组件树 / 菜单查询
  渲染器 bundle + 主壳 index.html
  ping-apt 生成 ui-index 的处理器

ping-system（业务层）
  resources/ui/user-center/index.json   按新 schema 重写
  各 Controller 上的 @ResourceReference
  （删除 domain/ui 的 UiSchemaRepository / UiSchemaService / ResourceReference）
```

**理由**：`ping-ui` 是"需要前端的模块都要引入"的公共依赖，机制放这里才能被复用；`ping-system` 保留机制等于机制藏在某个业务模块里，别家无法依赖。

**BREAKING**：`GET /v1/system/menus` → `GET /v1/ui/menus`，响应结构随 schema 统一而变化。

**关联键变更的影响**：既有 `@Operation(summary)` 保留用于 OpenAPI 文档；`@ResourceReference` 新增稳定标识并被 `ui-index` 引用。迁移期两者同时存在，绑定以 `@ResourceReference` 优先。

### D5. 静态资源落位与薄壳入口

**选择**：

```
ping-ui jar
  META-INF/resources/
    ├── assets/renderer/**     渲染器 bundle（ESM，共享）
    └── index.html              主壳：读组件树 → 渲染全站

module-foo jar
  META-INF/resources/foo/
    ├── index.html              薄壳（共享模板生成，见 D6）
    └── components/*.js         可选：自定义组件
  META-INF/ping/ui-index.json   页面声明（含已绑定的 apiDefinition）
```

Spring Boot 默认静态位含 `classpath:/META-INF/resources/`，两者零配置即可访问。命名空间由子目录天然隔离。

**散/聚路径**：

| 形态 | 主入口 | 模块深链 |
|---|---|---|
| 散（独立模块） | `/<module>/index.html` | 同左 |
| 聚（单体） | `/index.html` | `/<module>/index.html` |

**理由**：用户要求"独立模块直接访问 `xxx/index.html`"。散形态下 `ping-ui` 是依赖，其渲染器自动在位，薄壳可用；聚形态主壳与薄壳共用同一渲染器，不存在两套前端。

**替代方案**：每模块完整 SPA —— 需要每模块 node 构建、独立路由栈与沙箱，即被否决的微前端。

### D6. 薄壳由共享模板生成，node 构建仅在有自定义组件时需要

**选择**：`ping-ui` 提供 `module-entry.html` 模板；打包期由共享配置按 `${ping.module}` 复制为 `META-INF/resources/<module>/index.html`，内容为 import 共享渲染器并传入命名空间：

```
import { mount } from '../assets/renderer.js'
mount({ namespace: '<module>', manifest: '/v1/ui/menus?ns=<module>' })
```

模块 `pom.xml` 只需设置 `<ping.module>`（与 `standalone-module-packaging` 注入的处理器选项同源）。仅当模块含自定义组件时才需要 node 构建，产物输出到 `META-INF/resources/<module>/components/`。

**理由**：避免"每个模块都要配 node"这一不必要的负担；`ping-editor` 这类复杂交互才需要构建，其余交 json 即可。

**替代方案**：每模块手写 `index.html` —— N 处重复且易与渲染器接口漂移；`ping-editor` 现有的 `dist → META-INF/resources/editor` 模式可作为自定义组件的构建先例保留。

### D7. 动态加载粒度为组件，不是子应用

**选择**：渲染器按节点 `type` 查内置注册表；未命中则 `import(`/assets/<module>/components/<type>.js`)`。加载对象是单个组件，模块的 JS 模块作用域与 CSS 类名前缀即隔离手段。

**理由**：用户判定微前端难度过高。组件粒度下无独立路由栈、无沙箱、无跨应用通信诉求；命名空间冲突从"整应用"缩小到"单文件作用域"。

**替代方案**：qiankun/wujie HTML entry（否决）、iframe（否决）、原生 ESM 加载整个子应用（等价于微前端，否决）。

### D8. 路由前缀与权限拦截的分工

**选择**：

- **路由**：由菜单声明中的 `router` 字段驱动，前缀 `<module>/` 用于命名空间隔离（防未来冲突）。
- **权限拦截**：落在**菜单层**（返回的组件树按用户权限过滤）与 **API 层**（既有 `AuthenticateInterceptor`）。
- **静态资源保持放行**：沿用 `RestWebAutoConfiguration.PUBLIC_STATIC_RESOURCES`，因为登录页本身必须可达。

**理由**：静态 html 若纳入拦截则登录页不可达；前端路由是客户端行为，服务端拦截器无法覆盖。因此"前缀方便权限拦截"的正确落点是菜单数据与 API，而非静态文件。

**替代方案**：对 `<module>/**` 静态路径加拦截 —— 破坏登录流程，且前端路由可被绕过，给虚假的安全感。

## Risks / Trade-offs

- **Schema 统一导致既有数据作废** → `ping-system/ui/user-center/index.json` 与 `ping-ui/components/ui.json` 均在规划期，作废成本低；变更内完成迁移，不留双读兼容。
- **`GET /v1/system/menus` 破坏性变更** → 本项目无外部前端消费者（渲染器同期诞生），迁移窗口为零成本。
- **`ping-ui` 启用引入 JPA 实体与扫描顺序问题** → D4 中 `UIAutoConfiguration` 补 `@AutoConfiguration(after=...)`；与 `standalone-module-packaging` 任务 1.3 联动。
- **构建期绑定依赖打包时序**（OpenAPI 先于 ui-index）→ 生成步骤串行绑定在 `prepare-package`，任一跳过时降级为"无绑定的声明"并告警，不阻断构建。
- **`ping-apt` 需同时理解 Java 注解与 JSON 声明** → 处理器拆分为「注解扫描」与「资源规范化」两段，资源段不参与注解轮次。
- **渲染器 ESM 输出的 chunk 加载路径** → 构建配置 `publicPath` 用相对路径，使薄壳与主壳在不同目录层级下均可加载。
- **`@ResourceReference` 标识与展示名分离后仍可能重名** → 生成期校验重复标识并使构建失败。

## Migration Plan

1. **阶段一（schema 定稿）**：确定节点模型与必填字段；重写 `components/ui.json` 与 `ui/user-center/index.json` 为新 schema；`ping-ui` 从 `ping-parent` 移入根 pom 并启用（与 `standalone-module-packaging` 阶段一联动）。
2. **阶段二（机制迁移）**：`UiSchemaRepository` / `UiSchemaService` / `ResourceReference` 迁入 `ping-ui`；接口改为 `GET /v1/ui/menus`；`ping-system` 删除旧实现。
3. **阶段三（构建期绑定）**：`ping-apt` 新增 `ui-index` 生成器；打包期串联 OpenAPI → 绑定 → `META-INF/ping/ui-index.json`；运行时删除 OpenAPI 读取。
4. **阶段四（渲染器）**：`ping-ui` 建立前端工程与 ESM 构建，产出主壳与渲染器 bundle；共享薄壳模板与复制配置。
5. **阶段五（动态加载与权限）**：实现组件注册表与动态 `import`；菜单按权限过滤；命名空间与前缀约定落地。

**回滚**：阶段一、二独立可回滚（仅涉及尚未上线的规划期数据）；阶段三回滚需恢复运行时 OpenAPI 读取；阶段四、五回滚不影响后端。

每阶段以 `./mvnw -Ph2 test` 验证；阶段四起另需 `npm run build` 与浏览器手工验证散/聚两条路径。

## Open Questions

- **组件 `type` 的取值域与内置清单**：schema 只约束结构，具体 type（`application`/`layout`/`menu`/`table`/`column`/`action`/`user_info_panel`/`main_container`…）需在渲染器实现阶段收敛为受控枚举，未知 type 的降级策略（忽略 / 报错 / 占位）待定。
- **`ping-ui` 既有 JPA 实体 `Component` 的去留**：机制迁移后该实体（`ui_component` 表）是否仍承载运行时数据，还是仅作 schema 编辑器的数据源。
- **菜单权限过滤的数据来源**：复用 `ping-system` 的 AC 域（`Subject`/`Action`/`Resource`）还是新引入声明级权限点。
- **是否保留 `ping-editor` 作为自定义组件构建先例**：其 webpack 输出 `target/dist → META-INF/resources/editor` 与 D6 的薄壳模板机制如何并存。
