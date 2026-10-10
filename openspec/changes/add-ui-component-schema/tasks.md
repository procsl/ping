# Tasks

> 全部命令前置：`export JAVA_HOME=$HOME/.jdks/graalvm-jdk-25`（默认 java 是 11），仓库根目录执行 `./mvnw`，必须带数据库 profile。

## 1. 依赖与注解迁移

- [x] 1.1 `ping-system/pom.xml` 新增 `cn.procsl:ping-ui` 依赖。验证：`./mvnw -Ph2 -pl ping-system -am -DskipTests compile` 成功，且 `./mvnw -Ph2 -pl ping-system dependency:tree -Dincludes=cn.procsl:ping-ui` 能列出 `ping-ui`
- [x] 1.2 把 `@ResourceReference` 从 `cn.procsl.ping.boot.system.domain.ui` 移到 `cn.procsl.ping.boot.ui.component.api.ResourceReference`，同步 `ConfigController` / `UIController` 的 import 并删除原文件。验证：compile 成功，且 `grep -rn "system\.domain\.ui\.ResourceReference" ping-*/src` 无命中

## 2. 规范文档与组件类型定义

- [x] 2.1 新建 `ping-ui/doc/abstract-component-schema.md`：描述 `ui/<page>/compose.json` 格式、节点通用字段（`type` `id` `name` `order` `router` `description` `api` `containers`）、组合嵌套规则、`@ResourceReference:<name>` 引用语法、接口契约（`GET /v1/ui/components` 返回 `{types, pages}`）。验证：文档中每个字段名与 `openspec/changes/add-ui-component-schema/specs/ui-component-schema/spec.md` 逐字对齐，人工审阅通过
- [x] 2.2 写 18 个组件类型定义 JSON 到 `ping-ui/src/main/resources/META-INF/ping/ui/types/`（16 个常用类型 + `user_info_panel` + `main_container`），每个定义含 `type`、展示名、语义说明与允许的子节点类型。验证：`ls ping-ui/src/main/resources/META-INF/ping/ui/types | wc -l` 为 18，且每个文件 `jq -e '.type'` 非空
- [x] 2.3 新增 `ComponentTypeDefinitionTest`：从 classpath 固定路径读取类型定义，断言 spec 列出的 16 个类型全部存在。验证：`./mvnw -Ph2 -pl ping-ui test -Dtest=ComponentTypeDefinitionTest` 通过
- [x] 2.4 把 `AGENTS.md`（第 54 行）与 `ping-ui/doc/design.md`（第 39 行）中的 `ui/<page>/index.json` 约定改为 `ui/<page>/compose.json`，并校正 `AGENTS.md` 中「ping-ui 已停用」的过期描述（`ping-parent/pom.xml:21` 已启用）。验证：`grep -rn "ui/<page>/" AGENTS.md ping-ui/doc` 全部显示 `compose.json`

## 3. 构建期聚合索引

- [x] 3.1 在根 `pom.xml` 与 `ping-ui/pom.xml` 的资源声明中，把 `ui/**` 与 `META-INF/ping/ui/**` 改为不参与 `@...@` 资源过滤（新增一个 `filtering=false` 的 resource 并从过滤集里 exclude）。验证：`./mvnw -Ph2 -pl ping-ui process-resources` 后 `diff -r ping-ui/src/main/resources/META-INF/ping/ui ping-ui/target/classes/META-INF/ping/ui` 无差异
- [x] 3.2 编写 `ping-ui-components-aggregate.groovy`：读取本模块 `target/classes/ui/**/compose.json` 与 `META-INF/ping/ui/types/*.json`，再从 `project.artifacts` 解析出的每个依赖构件按精确路径 `META-INF/ping/ui/components.json` 读取上游索引，按键去重后写出 `target/classes/META-INF/ping/ui/components.json`；索引缺失、文件非法一律打印告警并跳过，不抛异常
- [x] 3.3 在 `ping-parent/pom.xml` 与 `ping-distribute/pom.xml` 的 `build/plugins` 中通过 `gmavenplus-plugin` 绑定 `process-classes` 执行该脚本（版本与 `groovy-all` 依赖复用根 pom `pluginManagement` 的既有配置）。验证：`./mvnw -Ph2 -pl ping-ui process-classes` 后 `ping-ui/target/classes/META-INF/ping/ui/components.json` 存在且 `jq '.types | length'` 为 18
- [x] 3.4 新增 `AggregatedIndexTest`：断言 classpath 固定路径 `META-INF/ping/ui/components.json` 存在、可解析为 JSON、且包含全部 16 个常用类型。验证：`./mvnw -Ph2 -pl ping-ui test -Dtest=AggregatedIndexTest` 通过

## 4. 启动期装配与内存注册表

- [x] 4.1 实现 `UIComponentRegistry`（单例 bean，容器刷新期装配）：读取固定索引 `META-INF/ping/ui/components.json` 与固定文档 `ping-api-doc/openapi.json`；注入 MVC 的 `RequestMappingHandlerMapping`，遍历带 `@ResourceReference` 的 handler 方法建立 `name → (method, path)` 索引；把 `api` 字段中的 `@ResourceReference:<name>` 就地替换为 `{method, path, request, response}`；产出不可变快照。验证：编译通过且无 `Class.forName(String)` / `setAccessible` / `Proxy.newProxyInstance`（`grep` 断言 `ping-ui/src/main/java` 为空）
- [x] 4.2 降级策略测试 `UIComponentRegistryFallbackTest`：索引缺失 → 注册表空；装配描述非法 JSON → 跳过该页并告警；OpenAPI 文档缺失 → 引用保持原样；引用目标不存在 → 引用保持原样。四例均须断言不抛异常、上下文可正常启动。验证：`./mvnw -Ph2 -pl ping-ui test -Dtest=UIComponentRegistryFallbackTest` 通过
- [x] 4.3 引用合法性测试：带 `@ResourceReference` 的接口可被解析，未标注但同名的接口不被解析且引用保持原样。验证：`./mvnw -Ph2 -pl ping-ui test -Dtest=UIComponentReferenceTest` 通过
- [x] 4.4 装配一次性测试：装配后修改 classpath 中的描述不改变注册表内容。验证：`./mvnw -Ph2 -pl ping-ui test -Dtest=UIComponentRegistryTest` 通过
- [x] 4.5 实现装配归宿规则：顶层 `application` → 应用树；顶层 `layout` / `nav_group` / `menu` / `breadcrumb` / `tabs` → 导航片段按 `order` 合并进应用树对应方位；其余 → 页面片段按顶层 `router` 挂载到 `router` 相同的 `menu` 节点下，无匹配时原样进入 `pages` 且不报错。验证：`./mvnw -Ph2 -pl ping-ui test -Dtest=UIComponentMountTest` 通过，覆盖三种归宿与无匹配场景

## 5. 组件描述接口

- [x] 5.1 改写 `ComponentController` 为从 `UIComponentRegistry` 返回 `{types, pages}`（`produces = application/json`）；同时删除 `src/main/resources/components/ui.json`、`ui/domain/Component.java`、`component/api/ComponentRecord.java`、`component/api/ComponentMapper.java`，并去掉 `UIAutoConfiguration` 上的 `@EntityScan` / `@EnableJpaRepositories`（`ping-jpa` 依赖暂留）。验证：`./mvnw -Ph2 -pl ping-ui -am -DskipTests compile` 通过，且 `grep -rn "components/ui.json\|ui\.domain\.Component" ping-ui/src` 无命中
- [x] 5.2 接口测试 `ComponentControllerTest`：非空注册表返回 200 且媒体类型为 `application/json`，响应含 `types` 与 `pages`；空注册表返回 200 与 `{"types":[],"pages":[]}`；连续两次请求响应一致。验证：`./mvnw -Ph2 -pl ping-ui test -Dtest=ComponentControllerTest` 通过
- [x] 5.3 在 `ping-ui/doc/abstract-component-schema.md` 补齐接口章节（路径、响应结构、空态、降级行为）与 `@Operation` 注解描述。验证：文档与 `ComponentControllerTest` 的断言一致

## 6. 收敛 ping-system

- [x] 6.1 把 `ping-system/src/main/resources/ui/user-center/index.json` 改名为 `compose.json` 并改写为新节点模型（`table` → `dataset`、`apis` → `api`、`column` / `action` / `row_action` 子节点，接口引用写成 `@ResourceReference:获取用户列表`）。验证：`./mvnw -Ph2 -pl ping-system -am process-resources` 后 `diff ping-system/src/main/resources/ui/user-center/compose.json ping-system/target/classes/ui/user-center/compose.json` 无差异（证明过滤已排除）
- [x] 6.2 删除 `ping-system` 的 `domain/ui/`（`UiComponent` `UiSchemaRepository` `UiSchemaService`）与 `api/ui/UIController.java`。验证：`grep -rn "UiSchemaRepository\|UiSchemaService\|path(\"/v1/system/menus\")" ping-*/src` 无命中，且 `./mvnw -Ph2 -pl ping-system -am -DskipTests compile` 通过
- [x] 6.3 新建 `ping-ui/src/main/resources/ui/app/compose.json` 应用外壳树（`application` + `layout` 方位槽 + `user_info_panel` + `main_container` + 与 `default-tree.ts` 同构的 6 分组 15 菜单），使 `pages[0]` 成为应用外壳且页面片段有挂载目标。验证：`./mvnw -Ph2 -pl ping-ui process-classes` 后 `jq -r '.pages[0].tree.type' ping-ui/target/classes/META-INF/ping/ui/components.json` 为 `application`（索引中每条页面为 `{key, tree}` 包裹）
- [x] 6.4 集成测试 `UiComponentAssemblyTest`：`GET /v1/ui/components` 返回的 `pages` 含 `user-center` 页面树且 `types` 含 16 个常用类型；`GET /v1/system/menus` 返回 404。验证：`./mvnw -Ph2 -pl ping-system -am test -Dtest=UiComponentAssemblyTest` 通过（必须带 `-am`，否则 `ping-ui` 会解析到本地仓库里过期的 jar，导致索引缺失）

## 7. 前端改造

- [x] 7.1 `schema/types.ts`：`apis?: ApiRef[]` 改为 `api?: ApiRef`，`ApiRef` 由 `{method, api}` 改为 `{method, path, request, response}`。验证：`cd ping-ui && npm run build`（含 `tsc --noEmit`）通过
- [x] 7.2 `data/default-tree.ts`：`table` → `dataset`，为该节点补 `query` 输入参数子节点与 `row_action` 行内动作子节点。验证：`npm run build` 通过
- [x] 7.3 `data/loader.ts`：从接口响应读取 `pages[0]`，保留裸树响应的兼容分支，接口不可达或缺 `type` 时降级本地声明并告警。验证：`npm run build` 通过，且降级分支在浏览器控制台产生 `[ping-ui]` 告警（启动本地 dev 观察）
- [x] 7.4 `components/renderer/registry.tsx` 扩展到 16 + 2 个类型（新增 `nav_group` `tabs` `breadcrumb` `empty` `query` `form` `form_action` `row_action` 等），未知类型仍降级为带类型名的占位节点且不丢弃其 `containers`。验证：`npm run build` 通过，且 `grep -o '^\s*[a-z_]*:' ping-ui/src/main/js/components/renderer/registry.tsx` 覆盖全部 18 个类型键
- [x] 7.5 `components/renderer/data-table.tsx` 改造为 `dataset` 渲染：从 `containers` 分离 `column`（携带排序能力）、`action`（工具栏）、`row_action`（行内动作），`query` 子节点渲染为该数据表的输入参数表单；保持渲染形态可替换、不改变描述与接口返回。验证：`npm run build` 通过，且 `dataset` 节点无 `column` 时仍显示原有提示而非崩溃
- [x] 7.6 `components/renderer/` 下补 `form` / `field` 渲染：`field` 按字段类型映射到既有 `components/ui/*`，未映射类型降级占位。验证：`npm run build` 通过
- [x] 7.7 同步 `ping-ui/doc/design.md`：菜单数据源由「本地组件树唯一数据源」改为「后端接口优先、本地降级」，词汇由 `table` 改为 `dataset` 并补 `query` / `row_action` 语义。验证：文档中的类型名与 `ping-ui/doc/abstract-component-schema.md` 一致
- [x] 7.8 三处挂载入口补 `manifest: "/v1/ui/components"`（主壳 `META-INF/resources/index.html`、薄壳模板 `META-INF/ping/entry/index.html`、`dev-entry.tsx`），否则前端从不请求组件接口，spec「页面加载即获取组件」与 8.3 均无法成立。验证：`npm run build` 通过，且两份 HTML 中均出现 `manifest: "/v1/ui/components"`

## 8. 集成验证

- [x] 8.1 全量后端测试。验证：`./mvnw -Ph2 test` 全绿（`ping-jpa` 等模块 `Tests run: 0` 属既有状态，不算失败）
- [x] 8.2 前端构建。验证：`cd ping-ui && npm run build` 通过，产出 `target/dist/renderer.js`
- [x] 8.3 类型一致性（可观察行为）：起 `ping-ui` 或 `ping-distribute` 后访问 `/index.html`，确认 16 + 2 种类型均渲染为对应组件而非「未知组件：xxx」占位；`-Dping.npm.skip=true` 场景改用 7.4 的 `grep` 断言兜底。验证：页面上无未知组件占位节点
- [x] 8.4 聚合形态验证：`./mvnw -Ph2 -DskipTests package` 后启动 `ping-distribute`，`curl -s localhost:10000/v1/ui/components | jq '.types | length'` 为 18、`.pages[0].id` 为 `ping.ui.app` 或含 `user-center`；访问 `/index.html` 正常渲染。验证：命令输出符合预期
- [x] 8.5 缺 `openapi.json` 形态验证：在不带 `ping-distribute package` 的普通 `./mvnw -Ph2 -pl ping-ui test` 下，注册表非空、`api` 字段保持原样引用、日志出现引用解析告警且进程正常退出。验证：测试断言 + 日志检查

## Workflow follow-up

- 在评审通过后运行 `/opsx-apply` 实施本变更。
- 实现完成后运行 `/opsx-archive` 归档，确认 `openspec/specs/ui-component-schema/spec.md` 已由增量合并为主 spec。
