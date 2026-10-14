# Design

## Context

动机参见 `proposal.md` - Why。此处只列影响方案选择的现状与约束：

- **前端处于初始阶段**，用户明确授权按文档随意修改、删除现有代码 —— 不需要为兼容旧实现留过渡层。
- **节点模型分歧**：`abstract-component-schema.md` 定义目标态 `children` + `properties`（5 通用字段），现有代码是扁平模型 + `containers`（`schema/types.ts`）。本次选择删除扁平实现、以 SDK 为目标态的唯一起点。
- **文档即规格**：`sdk-design.md` §9 列出 7 项待协商问题（A–G），本设计逐条落定。
- **Maven 已有前端集成模式**：`ping-ui/pom.xml` 用 `exec-maven-plugin` 在 `prepare-package` 阶段跑 `npm run build`，受 `ping.npm.skip` 控制；`ping-ui` 在 reactor 内（`ping-parent/pom.xml` 引 `../ping-ui`），故 `./mvnw -Ph2 test` 会经过该模块。
- **构建为单产物 lib 模式**：`vite.config.ts` 产出固定名 `renderer.js`，薄壳 `META-INF/resources/<ping.module>/index.html` 写死引用路径；`tsc --noEmit` 严格模式会在改 `ComponentNode` 后立即暴露所有扁平字段消费点。
- **旧渲染器删除导致 `admin-navigation` 4 条已归档需求失去实现支撑**，需在 spec 层面给出 delta。

## Goals / Non-Goals

**Goals:**

- 交付一个**不依赖 DOM、不依赖任何 UI 框架**的运行时核心，可在纯 Node 环境完成注册、运行、销毁全流程 —— 这是 `ui-component-sdk`「保持框架无关」需求的直接支撑，也使 vitest 核心测试无需 jsdom。
- 让 `sdk-design.md` 的公共 API（§7）与实际导出签名逐字对得上，文档可直接当使用手册。
- 错误隔离落在**单节点**粒度：一个处理器抛错，其后待派发的子节点与兄弟子树照常执行。
- 删除后不留死代码：`tsc --noEmit` 全绿，仓库内不存在扁平 `ComponentNode` 的残留引用。
- `./mvnw -Ph2 test` 能拦住前端测试失败。

**Non-Goals:**

- **不重建渲染器**：侧栏、路由、菜单切换、分组折叠、搜索、18 个类型的 React handler 本次全部不交付（能力回退，见 `admin-navigation` delta）。这是本次的明确边界，不是遗漏。
- 不定义消息组件语义（`MessageBus` 只承载 `on/off/emit`）。
- 不实现 `logger` buildin 组件（文档 §6.3 标注「后续定义」），`debug` 开关本期只控制 SDK 自身控制台输出。
- 不做动态远程处理器加载（文档 §1 非目标，留扩展点）。
- 不提供 18 个组件类型的内置处理器 —— 类型语义归 `abstract-component-schema.md`，处理器由使用方注册。
- 不动后端：`GET /v1/ui/components` 聚合链路的恢复不在本次范围。

## Decisions

### D1. 落位：`ping-ui/src/main/js/sdk/`，不建独立包

**决定**：作为 `ping-ui` 内建模块，由现有 vite/tsc 编译，导出 `createPingUi`。

**理由**：根目录无 `package.json`、无 workspace；新建子包需新增 `package.json` + `tsconfig` + 打包配置，且 `ping-ui` 以单产物交付到 jar 的 `META-INF/resources/`，子包要被 rollup 正确内联。可独立发 npm 的收益在当前「不发 npm」的现实下不成立。

**备选**：独立子包 `@ping-ui/sdk`（文档 §9 F 字面形态）—— 被零新构建系统约束否决；因 D2 已保证 SDK 内部不依赖 `ping-ui` 其余代码，抽包时只需移动目录 + 补配置。

### D2. SDK 内部自持类型，不 import `ping-ui` 其余路径

**决定**：`src/main/js/sdk/**` 内不得 import `components/`、`layout/`、`router/`、`data/`、`schema/`；`ComponentNode` 与 `SdkError` 等类型在 `sdk/` 内定义。

**理由**：本次删除了 `schema/types.ts`（扁平 `ComponentNode`）与全部渲染器，SDK 天然成为唯一类型来源 —— 这是「框架无关」的可执行形式，靠目录边界而非事后重构保证可抽包，也让 vitest 测试核心时不加载 React 链路。

**备选**：把 `ComponentNode` 放在 `schema/types.ts` 供全局共用 —— 会让 SDK 依赖一个将来可能被多种前端实现共享的公共包，模糊边界；且该文件本次已删除。

### D3. 子节点派发由处理器驱动，经 `create` 完成且在该路径内包装错误

**决定**：`run` 只执行根节点处理器，**SDK 不自动遍历树**。处理器需要渲染子节点时，用 `create(tree, messageBus)` 拿到预绑定处理器 `CreatedHandler(node)`，自行遍历 `node.children` 逐个派发。**错误包装实现于 `create` 返回的处理器内部** —— 任何经它派发的节点，其异常都被捕获为 `SdkError` 并交给 `error_handler`，失败只影响该节点，处理器的遍历循环继续执行下一个。

**理由**：
- 文档 §2.2 与 §3 已定义 `create(tree, messageBus)`，无需新增第二个派发入口（用户确认「SDK 已经提供了方法，即 `create`」）。
- `abstract-component-schema.md` §5.2 规定 `tabs`「未激活页签默认不渲染其内容（避免无谓的数据请求）」、§6.1 规定侧栏只渲染 `layout[left]` —— 自动深度遍历会让处理器失去「不渲染某些子节点」的能力，直接违背既有语义。
- 错误包装绑在 `create` 而非裸 handler 上，才能同时满足 spec 的「派发时的错误隔离」与「同级后续节点继续执行」：包装后异常不再冒泡进处理器的循环。

**备选**：
- *SDK 自动深度遍历* —— 被 tabs 语义否决。
- *额外提供 `sdk.renderChildren(node)` 递归原语* —— 与 `create` 职责重叠，两套派发路径会让错误包装出现两个落点；已否决（用户明确指定用 `create`）。
- *暴露 `sdk.get(type)` 供处理器自行调用原始 handler* —— 原始 handler 未经包装，隔离语义会随处理器实现质量而漂移，spec 的「单节点错误隔离」无法统一保证。

**未注册类型**同理：派发路径命中无处理器的 type 时走降级，`error_handler` 给出降级形态后，由该节点的处理器（或降级实现）继续派发其子节点，从而满足「子节点保留」。

### D4. handler 签名：4 固定位置参数，`messageBus` 与 `sdk` 自动注入

**决定**：`(tree, node, messageBus, sdk) => void`，注册时只写前两个，后两个由 SDK 调用时补上。

**理由**：文档 §2.2 与 §9 A 已定；位置参数让处理器定义最短，注入对处理器透明。`sdk` 参数正是 D3 中处理器取得预绑定处理器的入口（处理器内 `sdk.create(...)`）。

**实现方式**：`register` 内部统一包装为单一内部签名，`run` 与 `create` 共用同一执行路径，注册表只存一种形态。

**备选**：*单一 `ctx` 对象* —— 更易扩展，但与文档 §7 公开 API 逐字不符，会连带改 `sdk-design.md`。

### D5. 注册表：`Map<namespace, Map<type, handler>>`，冲突告警不抛错

**决定**：二维 Map，查找顺序「当前命名空间 → `default`」；同命名空间重复注册且未声明 `override` 时返回 `false` + `console.warn`，**不抛异常**。

**理由**：文档 §9 B 定为两级；二维 Map 比拼接键 `"ns:type"` 类型安全，`unregister` 可精确到命名空间下的一条。不抛错与仓库「降级 + 告警，启动永不失败」的不变量一致，注册冲突属配置错误而非异常状态。

**备选**：*抛 `Error`* —— 会让一次配置笔误拖垮整个初始化；已否决。

**告警通道**：`console.warn`，vitest 用 `vi.spyOn(console, "warn")` 断言；不引入自定义 logger（Non-Goal）。

### D6. `error_handler` 存于同一注册表，但不可被当作可渲染 type 命中

**决定**：走文档 §6.1 的 `register("error_handler", ..., { override: true })` 覆盖方式，注册进同一张表；按节点 type 查找时排除该保留名。

**理由**：文档要求「默认注册、可被覆盖」，复用同一套注册与覆盖机制最省事，不必另开槽位；排除只是一个保留名名单。默认实现于 `init()` 注册，职责为输出错误 + 返回占位降级形态。

**备选**：*独立 `errorHandler` 槽位* —— 需另写覆盖语义，且 `inspect()` 要展示两处状态，复杂度更高。

### D7. 消息总线内建，不作为可覆盖组件

**决定**：`MessageBus` 由 `createPingUi` 内部创建，`run`/`create` 显式传入时优先用传入的；实现 `on`/`off`/`emit`，`off` 不传回调时移除该事件全部回调。

**理由**：文档 §4 明确「只保留机制、消息组件暂未定义」，且 §9 的 7 项争议项中没有消息总线 —— 它是确定的基础设施。内建可避免「未注册总线就无法 `run`」的额外状态。文档中 `interface MessageBus { /* 待定义 */ }` 是空占位，实现时补 `on/off/emit` 即可，不需额外决策。

### D8. 生命周期：显式状态机 + `run` 未初始化时自动补 `init()`

**决定**：状态 `created → initialized → running → destroyed`，非法转换按 D5「告警 + 不抛错」。

| 转换 | 方法 | 事件 |
|---|---|---|
| created → created | `createPingUi()` | `sdk:created` |
| created → initialized | `init()` | `sdk:initialized` |
| initialized/running → running | `run()` | `sdk:run` |
| （运行期，无状态变更） | `create()` | `component:created` |
| initialized/running → running | `update(tree)` | `sdk:updated` |
| initialized/running → destroyed | `destroy()` | `sdk:destroyed` |

**理由**：spec 要求「阶段顺序」与「销毁幂等」可测，显式状态机让断言直接落到状态值；`destroy()` 重复调用直接返回。`run` 在 `created` 态**自动补一次 `init()` 并 `console.warn`** —— 最可能是调用方忘记 init，自动补全比拒绝更贴合「永不失败」不变量。

**`update(tree)`**（文档 §5 标「可选」）：实现为整树替换 + 重新 `run`，事件名不同；成本接近零，留空反而要多维护一个状态分支。

**备选**：*多个布尔标志位* —— 会组合出不可达状态，状态机更易测。

### D9. 删除清单与 vite 入口改造

**决定**：删除下列文件，`schema/types.ts` 一并删除（其 `ComponentNode` 由 D2 取代）：

```
components/renderer/*        （registry.tsx + 18 类型 React 渲染器 + 辅助）
RendererRoot.tsx  main.tsx
layout/*          router/*          pages/*
data/loader.ts    data/default-tree.ts
schema/types.ts   mock/*
```

`vite.config.ts` 的 lib entry 由 `src/main/js/main.tsx` 改为 `src/main/js/sdk/index.ts`，**产物名保持 `renderer.js`**（薄壳路径写死，改名需同步改模板，收益为零）。

**薄壳** `META-INF/ping/entry/index.html`：原本挂载 React 渲染器，删除后其脚本会因缺导出而报错，故**必须**同步改写为原生 HTML 占位页 —— 加载 `renderer.js`（此时是 SDK bundle）并显示「渲染器重建中」，附 demo 入口。这保证 `/ui/index.html` 是可访问、可理解的页面而非白屏或报错页，同时守住 AGENTS.md「薄壳 `<module>/index.html`」的约定。

**理由**：删渲染器的连带影响面（`tsc --noEmit` 严格模式）必须一次清干净，否则构建红。薄壳改动是「能力回退」下保持入口可用的最小代价。

**备选**：*薄壳原样不动* —— 访问即 JS 报错，体验劣于白屏；已否决。*薄壳直接当 demo 页* —— 会把演示内容混进产品入口，demo 独立更清晰。

### D10. `npm test` 接入 Maven `test` 阶段

**决定**：`ping-ui/pom.xml` 新增 `exec-maven-plugin` execution，`<phase>test</phase>` 跑 `npm test`，`<skip>${ping.npm.skip}</skip>` 复用既有开关，**不新增 `npm install` 步骤**。

**理由**：与既有 `npm run build` execution（`prepare-package` 阶段）完全同构，开关、工作目录、跳过方式一致，维护心智最低。不自动 install 是用户选择：现网 `npm run build` 也假设已 `npm install`，新增 install 会拖慢每次 `test` 且无网络环境必失败。`test` 阶段早于 `prepare-package`，两者不冲突。

**验证**：`./mvnw -Ph2 test` 中 `ping-ui` 出现 `npm test` 执行记录；改一个测试为失败，`./mvnw -Ph2 test` 非零退出；`-Dping.npm.skip=true` 时跳过。

**备选**：
- *独立开关 `ping.npm.test.skip`* —— 便于只跳测试不跳构建，但多一个开关与既有约定不一致；暂不需要，后续有需求再加。
- *`npm ci` 后 `npm test`* —— 更鲁棒但每次 test 都付依赖检查成本，无网络必失败；已按用户选择否决。
- *只跑 `npm test` 不接 Maven* —— 会让前端测试脱离 `./mvnw -Ph2 test` 这一仓库唯一验证入口（AGENTS.md：「验证就靠 `./mvnw -Ph2 test`」），已否决。

### D11. demo：原生 DOM，源码为 TS，经 vite dev server 运行

**决定**：demo 源码 `src/main/js/sdk/demo/`（`index.html` + `demo.ts` + `demo.css`），**不用任何 UI 框架**，用原生 `document.createElement` / `textContent` 构建 DOM；`npm run demo` = 起 vite dev server 并打开该页面。

**理由**：用户要求原生 HTML/JS/CSS。源码用 TS 是为了与 SDK 类型同源（`ComponentNode` 直接 import，不必手写平行类型），由既有 vite 编译 —— TS 是语言与构建工具，不是 UI 框架，不违背「原生」诉求。vite dev server 提供 HTTP，规避 ESM 在 `file://` 下的跨域限制，且**不进 build 产物**，`renderer.js` 与 chunk 结构不变。

**demo 内容**：注册若干自造 type 的处理器，跑一份 `children` + `properties` 树，覆盖三条路径 —— 正常命中、未注册 type 降级、处理器抛错后同级继续。三种路径各自在页面上可见，使 demo 同时具备「演示」与「验收证据」两种作用。

**备选**：
- *demo 进 vite lib build 多入口* —— lib 模式与多 HTML 入口冲突，要拆两套配置；已否决。
- *demo 挂进薄壳* —— 见 D9，会混进产品入口。
- *用 React 写 demo* —— 无法证明 SDK「框架无关」这条 spec 需求；已按用户选择否决。

## Risks / Trade-offs

- **能力回退是用户可见的破坏**（`/ui/index.html` 不再有后台管理界面）→ proposal 已标 **BREAKING**，`admin-navigation` 4 条需求以 `REMOVED` 表达并在每条给出 `Migration`，归档后主 spec 不留虚假需求；D9 的占位页让入口不报错。
- **删除量大、`git` 外无回滚点** → 全部为删文件，`git revert` 即完整回滚；删除单独成任务组，与 SDK 新增任务分开提交，便于按组回退。
- **`ComponentNode` 只剩一份**，但重建渲染器时需在 SDK 之外重新引入布局与路由 → SDK 的 `sdk/**` 不依赖任何被删路径（D2），重建时不会反向牵连 SDK。
- **`renderer.js` 内容从「React 渲染器」变成「SDK bundle」**，外部若有人直接引它会得到不同 API → 薄壳是唯一已知消费方，D9 同步改写；产物名不变保证 jar 内路径稳定。
- **`npm test` 进 Maven 后，无 `node_modules` 的环境会让 `./mvnw -Ph2 test` 失败** → 与既有 `npm run build` 的失败模式一致；`-Dping.npm.skip=true` 可整体跳过前端步骤（AGENTS.md 已记载该属性）。
- **D3 依赖处理器自觉派发子节点**，处理器漏写派发会导致子树静默不渲染 → 属处理器实现质量问题，由重建渲染器时的集成测试覆盖；SDK 侧不强加遍历是刻意为之（tabs 语义）。

## Migration Plan

1. **先立后破**：先建 SDK + 测试 + demo 并跑绿，再删旧渲染器 —— 中途任一时刻 `npm test` 可解释、可回退。
2. 删除任务组单独提交（`git rm` 清单见 D9），随后立即改 `vite.config.ts` 入口与薄壳模板，保证 `npm run build` 恢复绿色。
3. `ping-ui/pom.xml` 加 `test` 阶段 execution；用「临时改红一个测试 → `./mvnw -Ph2 test` 非零 → 改回」验证拦截有效。
4. 文档状态更新（`sdk-design.md` 状态行与 §9 结论、`abstract-component-schema.md` §11）与代码同批提交。
5. **回滚**：`git revert` 整个变更即恢复旧渲染器与扁平模型；Maven 改动随 `ping-ui/pom.xml` 一并回退，不影响后端测试。

## Open Questions

- 重建渲染器时 `renderer.js` 是否改名（如 `sdk.js`）—— 当前为保薄壳路径稳定而沿用旧名，重建时薄壳必然重写，届时可一并决定；不改变本次 specs 与任务拆分。
- `admin-navigation` 重建后的 spec 是原样重新提出，还是按新模型拆分需求 —— 取决于重建时的实现形态，超本次范围。
- 递归派发是否需要 `sdk` 暴露「仅取处理器不执行」的查询（供工具类用途）—— 当前 `registry.get` 已具备，无需新增；不影响需求。
