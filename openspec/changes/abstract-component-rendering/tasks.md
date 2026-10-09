# Tasks: abstract-component-rendering

> 前置变更：`standalone-module-packaging`（阶段一完成 `ping-ui` 移层、阶段三完成 `META-INF/ping/` 打包期生成约定）。本变更任务 1 起依赖其任务 1.1–1.3、2.x、3.x、4.x。

## 1. Schema 定稿与声明迁移

- [ ] 1.1 依据 `ping-ui/doc/design.md` 与 `components/ui.json` 定稿节点模型：`type`/`id`/`name`/`containers`/`layout`/`router`/`order`/`apis`，补齐叶子组件（`table`/`column`/`action`）的字段定义
- [ ] 1.2 在 `ping-ui` 实现 schema 的 Java 类型与结构校验（必填字段、`id` 唯一性）
- [ ] 1.3 重写 `ping-ui/src/main/resources/components/ui.json` 为定稿 schema，作为应用壳样例
- [ ] 1.4 重写 `ping-system/src/main/resources/ui/user-center/index.json` 为定稿 schema，移除 `functions` 与中文 `api` 挂载
- [ ] 1.5 删除 `ping-ui/doc/design.md` 中与定稿不一致的描述，或标注为定稿来源
- [ ] 1.6 `./mvnw -Ph2 test` 回归，确认无解析失败

## 2. 机制迁移至 ping-ui

- [ ] 2.1 将 `@ResourceReference` 注解从 `ping-system` 移入 `ping-ui`，新增稳定标识属性（默认取 `name`）
- [ ] 2.2 将 `UiSchemaRepository` / `UiSchemaService` 迁入 `ping-ui`，移除按 OpenAPI 中文 `summary` 建索引的逻辑
- [ ] 2.3 在 `ping-ui` 新增 `GET /v1/ui/menus`，返回权限过滤后的组件树
- [ ] 2.4 删除 `ping-system` 的 `GET /v1/system/menus` 与 `domain/ui` 下的 `UiSchemaRepository`、`UiSchemaService`、`ResourceReference`
- [ ] 2.5 `ping-system` 各 Controller 补充带稳定标识的 `@ResourceReference`
- [ ] 2.6 `ping-ui` 的 `UIAutoConfiguration` 补 `@AutoConfiguration` 与 `@AutoConfigureAfter`（与 `standalone-module-packaging` 任务 1.3 联动）
- [ ] 2.7 校验：请求 `GET /v1/system/menus` 返回 404，`GET /v1/ui/menus` 返回 200
- [ ] 2.8 校验：仅依赖 `ping-ui` 的模块声明可被扫描注册并返回

## 3. 扫描注册与构建期绑定

- [ ] 3.1 `ping-apt` 新增 `ui-index` 生成器：扫描 `src/main/resources/ui/**/index.json`，按定稿 schema 校验（必填、`id` 唯一），规范化后汇总输出 `META-INF/ping/ui-index.json`
- [ ] 3.2 生成器校验失败时使构建失败并报告文件与节点位置；模块无声明时不生成该文件
- [ ] 3.3 `ping-apt` 新增 API 绑定步骤：读取本模块 `META-INF/ping/openapi/<module>.json`，按 `@ResourceReference` 稳定标识把完整定义写入 `ui-index.json`
- [ ] 3.4 绑定阶段校验：标识重复、引用不存在均使构建失败；OpenAPI 被跳过时降级为未绑定并告警，不阻断构建
- [ ] 3.5 在 `ping-ui` 新增 `RuntimeHintsRegistrar`，注册 `META-INF/ping/ui-index.json` 资源 hint
- [ ] 3.6 `ping-ui` 注册实现改为 `getResources("META-INF/ping/ui-index.json")` 枚举并求并集，删除 `classpath*:ui/**` 通配
- [ ] 3.7 删除运行时 OpenAPI 读取（`loadOpenapiDoc`），全仓确认无 `ping-api-doc` 引用
- [ ] 3.8 打包时序：确认 OpenAPI 生成先于 `ui-index` 生成，二者串行于 `prepare-package`
- [ ] 3.9 校验：单模块产物含绑定后的 `ui-index.json`；聚合产物含多模块分片并集；`-Dping.openapi.skip` 下降级告警

## 4. 渲染器与前端工程

- [ ] 4.1 在 `ping-ui` 建立前端工程（`package.json`、构建配置），复用 `ping-editor` 的 `exec-maven-plugin` + `maven-resources-plugin` 模式
- [ ] 4.2 渲染器构建为 ESM 输出，`publicPath` 使用相对路径，产物落位 `META-INF/resources/assets/renderer/`
- [ ] 4.3 实现主壳 `META-INF/resources/index.html`：加载渲染器并拉取 `/v1/ui/menus` 渲染
- [ ] 4.4 实现组件树 → 前端路由表推导；按 `order` 排序同级节点
- [ ] 4.5 实现内置组件注册表（`application`/`layout`/`menu`/`table`/`column`/`action` 等定稿 type）
- [ ] 4.6 在 `ping-ui` 提供共享薄壳模板 `module-entry.html`
- [ ] 4.7 打包期按 `${ping.module}` 复制模板为 `META-INF/resources/<module>/index.html`
- [ ] 4.8 业务模块 `pom.xml` 声明模块标识属性（与处理器选项同源），不手写入口
- [ ] 4.9 `./mvnw -Ph2 -DskipTests package` 后手工验证：聚形态根 `index.html` 可渲染全站

## 5. 组件动态加载与命名空间

- [ ] 5.1 渲染器实现 `type` 解析：先查内置注册表，未命中则 `import('/assets/<module>/components/<type>.js')`
- [ ] 5.2 实现加载失败的非阻断降级（占位节点 + 控制台告警），确保整页不中断
- [ ] 5.3 模块组件 JS 封装进模块作用域；CSS 类名约定模块前缀并在构建期校验
- [ ] 5.4 菜单 `router` 校验：模块声明的路由必须以模块名为前缀
- [ ] 5.5 为含自定义组件的样例模块接入 node 构建，产物复制到 `META-INF/resources/<module>/components/`
- [ ] 5.6 校验：无前端工程的模块打包不执行 node 且不失败

## 6. 权限与静态资源放行

- [ ] 6.1 `/v1/ui/menus` 接入权限过滤：无权限节点及其子树不返回（复用或接入 `ping-system` 的 AC 域，见 design Open Questions）
- [ ] 6.2 确认 `PUBLIC_STATIC_RESOURCES` 覆盖模块命名空间下的 html/js/css/图片
- [ ] 6.3 校验：未登录访问 `/<module>/index.html` 返回资源；未登录调用受保护接口返回认证错误

## 7. 验证与收口

- [ ] 7.1 散形态端到端验证：独立 jar 运行 → `/<module>/index.html` → 渲染器加载 → 组件树渲染
- [ ] 7.2 聚形态端到端验证：单体运行 → 根 `index.html` 全站渲染 → `/<module>/index.html` 深链可用
- [ ] 7.3 native 验证：`./mvnw -Ph2 -Pnative -DskipTests package`，确认 `ui-index` 资源 hint 生效、启动注册正常
- [ ] 7.4 `./mvnw -Ph2 test` 全量回归
- [ ] 7.5 更新 `AGENTS.md`：`ping-ui` 职责、`/v1/ui/menus`、`META-INF/ping/ui-index.json` 约定、薄壳生成与前端构建时机
- [ ] 7.6 `openspec verify abstract-component-rendering` 通过后按需归档
