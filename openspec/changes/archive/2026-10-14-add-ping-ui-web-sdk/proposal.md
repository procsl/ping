# Proposal

## Why

`ping-ui/doc/sdk-design.md` 已确立 Web SDK 的完整架构（处理器注册、`run`/`create`、生命周期、`error_handler` 降级），但代码里**没有任何对应实现**——现有 `components/renderer/registry.tsx` 是一张硬编码的 `Record<string, NodeRenderer>`，与 React 强耦合，既无法被其它前端框架复用，也没有错误隔离、生命周期与可覆盖的错误降级能力。同时前端仍处在**初始阶段**：节点模型是扁平的（类型专有字段平铺在节点上、子节点键为 `containers`），与 `abstract-component-schema.md` 定义的目标态（`children` + `properties`）不一致，两套模型并存的成本远大于迁移成本。因此本次不迁移、而是**按文档从头重建起点**：先把 SDK 这块唯一未落地的主文档做出来，旧渲染器一并移除，为后续按新模型重建留出干净基线。

## What Changes

- **新增 SDK 核心模块**（`ping-ui/src/main/js/sdk/`），框架无关，提供：
  - 组件 type 的处理器注册表（`register` / `unregister` / `has` / `get`），支持「命名空间 + type」两级查找与 `override` 覆盖控制；
  - 运行入口 `run(tree, messageBus)` 与预绑定处理器 `create(tree, messageBus)`，处理器按 4 参数 `(tree, node, messageBus, sdk)` 调用，其中 `messageBus` 与 `sdk` 自动注入；
  - **子节点派发由处理器主动驱动**：SDK 不自动遍历树，处理器通过 `create` 返回的预绑定处理器逐个派发子节点，可自行跳过某些子节点（支撑 `tabs` 不渲染未激活页签、侧栏只渲染 `layout[left]`）；错误包装在该派发路径内完成，单节点失败不影响整棵树。
  - 实例生命周期 `create → init → running → destroy`，各阶段触发事件与 `SdkHooks` 钩子，`destroy` 幂等；
  - 消息通知机制 `on/off/emit`（仅机制，不定义消息组件语义）；
  - 错误捕获与降级：处理器抛错包装为 `SdkError`（含 `nodeId`/`type`/`phase`/`stack`），交由可覆盖的 `error_handler` buildin 组件决定降级形态。
  - 新增目标态节点类型 `ComponentNode`（`type`/`id`/`name`/`children`/`properties` 5 通用字段）。
- **删除现有前端渲染器与扁平模型实现**，从头按文档重建起点：
  - **BREAKING** 删除 `components/renderer/*`（含 `registry.tsx` 与 18 个类型的 React 渲染器）、`RendererRoot.tsx`、`layout/*`、`router/*`、`pages/*`、`data/default-tree.ts`、`data/loader.ts`、`schema/types.ts`（扁平 `ComponentNode`）、`mock/*`。
  - **BREAKING** 后台管理页面的侧栏、路由、菜单切换、分组折叠、搜索能力本次**不再提供**（详见 `admin-navigation` delta）。
  - 薄壳 `/ui/index.html` 与 `META-INF/resources` 落位机制保持不变。
- **新增可运行 demo**（原生 HTML/JS/CSS，无框架依赖）：注册若干自造 type 的处理器、跑通一份 `children` + `properties` 组件树，覆盖命中、未注册降级、抛错隔离三种路径。
- **`npm test` 纳入 Maven 测试周期**：`ping-ui/pom.xml` 在 `test` 阶段执行 `npm test`，复用既有 `ping.npm.skip` 开关，不自动执行 `npm install`；此后 `./mvnw -Ph2 test` 会连带跑前端测试。
- **BREAKING** `ping-system/src/main/resources/ui/user-center/compose.json` 同步改写为目标态 `children` + `properties` 形态（当前不参与运行时，仅保持声明与规范一致）。

## Capabilities

### New Capabilities

- `ui-component-sdk`: Web 端抽象组件 SDK —— 框架无关的处理器注册表、组件树运行入口（`run`/`create`）与处理器主动驱动的子节点派发、实例生命周期与钩子、消息通知机制，以及基于可覆盖 `error_handler` 的单节点错误隔离与降级。

### Modified Capabilities

- `admin-navigation`: 该能力的 4 条需求（侧栏展示菜单树、菜单项切换当前页面、菜单分组展开与折叠、侧栏菜单搜索）依赖被删除的旧渲染器实现，本次一并移除；后续变更用 SDK 处理器重建后重新提出该能力。

## Impact

- **删除代码**：`ping-ui/src/main/js/` 下的渲染器、布局、路由、数据降级与扁平类型定义（清单见 What Changes）。
- **新增代码**：`ping-ui/src/main/js/sdk/`（`createPingUi`、注册表、生命周期、`MessageBus`、`SdkError` 与默认 `error_handler`、目标态 `ComponentNode`）、`sdk/__tests__/`、原生 demo。
- **构建链路**：`ping-ui/vite.config.ts` 的 lib 入口由 `main.tsx` 改为 SDK 入口；产物名 `renderer.js` 与薄壳引用路径保持不变。`ping-ui/pom.xml` 新增 `test` 阶段的 `npm test` execution。
- **依赖**：`ping-ui/package.json` 新增 devDependency `vitest` 与 `test`/`demo` 脚本；`dependencies` 不变（SDK 与 demo 不引第三方运行时依赖）。
- **测试命令**：新增 `cd ping-ui && npm test`；`./mvnw -Ph2 test` 自此后包含前端测试（可用 `-Dping.npm.skip=true` 跳过）。
- **文档**：`sdk-design.md` 状态转为已实现；`abstract-component-schema.md` §11 迁移对照表按本次实际改动重写。
- **不在范围内**：用 SDK 重建渲染器与 18 个类型的 handler；后端 `GET /v1/ui/components` 聚合链路恢复；消息组件语义；`logger` buildin 组件。
