# Design

## Context

动机见 proposal.md - Why。影响方案的现状与约束：

- 菜单是组件树数据推导的：`deriveMenu` 从 `layout[left]` 取菜单根，`deriveRoutes` 从 `menu.router` 推导路由，`registry.tsx` 的 `menu` 渲染器决定分组与叶子的展示形态。侧栏搜索与分组折叠必须落在这条数据链路上，不能另起一套静态菜单配置。
- 侧栏容器 `Sidebar.tsx` 只接收已推导好的 `menus`，自身无状态；折叠状态 `collapsed` 与激活态 `activePath` 由 `AdminLayout` 统一装配进 `RendererContext`。
- 分组当前是**静态标题**：`Menu` 渲染器对有 `containers` 的节点只输出标题 + 子项，没有任何展开/折叠交互。
- `RoutePage` 对没有子内容的菜单项统一落到 `Dashboard`，因此目前切任何菜单看到的都是同一个工作台，无法区分。
- 菜单数据是本地假数据 `default-tree.ts`（2 组 2 项），后端 `/v1/system/menus` 返回的 `UiComponent` 与前端 `ComponentNode` schema 不一致且从未被请求；`ping-ui` 无前端测试框架，验证手段是 `npm run build`（`tsc --noEmit` + vite build）与 `ping-ui` 的 Maven 静态资源测试。

## Goals / Non-Goals

**Goals:**
- 假数据菜单树达到能验证侧栏布局与滚动的规模（多分组、多项）。
- 侧栏搜索：过滤、空态、清空恢复、命中分组自动展开。
- 分组展开/折叠交互，与侧栏整体折叠、多标签导航共存且互不冲突。
- 每个菜单项切换后内容区有可区分的呈现。

**Non-Goals:**
- 不接入后端菜单接口、不改 `ping-system` 的 `UiComponent`/`UIController` schema 与路径。
- 不引入菜单管理（增删改、排序、启停）、不做菜单权限过滤。
- 不引入前端测试框架或新运行时依赖（搜索用现有组件与工具函数实现）。
- 不改组件树 schema（`ComponentNode` 不新增字段）。

## Decisions

**D1：搜索过滤放在侧栏容器层，用纯函数实现**
`AdminLayout` 持有 `query` state，经新增的纯函数（`router/derive.ts` 内 `filterMenus(menus, query)`）得到过滤后的菜单子树，再传给 `Sidebar` 渲染。
- 理由：过滤需要"整体移除未命中分组"与"展示空态"，这属于容器职责；纯函数可被 `tsc` 与人工推演验证，且与 `deriveMenu`/`deriveRoutes` 同文件同风格。
- 备选：在 `RendererContext` 里塞 `query`、由 `Menu` 渲染器各自过滤 —— 否决，空态提示与"未命中分组整体消失"要在容器层才能正确表达，且每个渲染器都要读上下文，耦合面更大。
- 备选：用第三方命令面板组件（cmdk 等）—— 否决，引入新依赖，且需求是侧栏内联过滤而非弹层。

**D2：分组展开/折叠状态留在 `Menu` 渲染器组件内部（local state），默认展开、不持久化**
- 理由：展开态是纯视图细节，刷新后回到默认展开是可接受且可预期的行为；不落 `localStorage` 避免与既有 `ping.collapsed`（侧栏整体折叠）的状态语义混淆。
- 备选：提升到 `AdminLayout` 并持久化 —— 否决，跨分组的展开态字典属于额外状态面，当前没有跨刷新保持的需求。
- 搜索态下的例外：`query` 非空时命中分组一律按展开渲染（搜索期间禁用分组折叠点击），自然满足"命中分组自动展开"场景，无需在渲染器里回写状态。

**D3：搜索框由 `Sidebar` 自身渲染，折叠态隐藏**
搜索输入放在 logo 下方、菜单列表上方；`collapsed` 时不渲染搜索框（窄条放不下输入框），恢复展开后 `query` 原样保留。
- 理由：`Sidebar` 已经知道 `collapsed`，把展示位置收在组件内可避免 `AdminLayout` 再拆一层 props。
- 备选：折叠态显示放大镜图标、点击展开侧栏再聚焦 —— 否决，超出需求，属增范围。

**D4：菜单假数据扩充分组，菜单项用 `placeholder` 内置类型承载占位内容**
在 `default-tree.ts` 增加分组与菜单项（保持两段式 `/xxx/xxx` 路由、`order` 连续）；这些菜单项的 `containers` 声明 `{ type: "placeholder" }` 节点，并在 `registry.tsx` 注册内置 `placeholder` 渲染器（展示"页面建设中 + 所属菜单名"）。工作台菜单保持无子内容，继续走 `RoutePage` 的 `Dashboard` 兜底。
- 理由：不动 `RoutePage` 的既有兜底语义（无内容 → 工作台），又能让每个菜单项呈现可区分内容；`placeholder` 是注册表内的已知类型，不触发"未知组件类型"告警。
- 备选：给所有菜单都塞 Dashboard —— 否决，切换后看不出差异，不满足"内容可见差异"。
- 备选：复用未命中类型的占位展示 —— 否决，会污染控制台告警且语义是"未知"而非"待建设"。

**D5：验证以类型检查 + 构建 + 既有 Java 测试为主**
`npm run build` 必须通过；`./mvnw -Ph2 test`（含 `ping-ui` 的 `StaticAssetsTest`）必须通过。菜单过滤纯函数的正确性靠场景逐条人工核对（无前端测试框架，本次不引入）。
- 备选：引入 vitest —— 否决，属新增依赖与工具链，超出本次范围。

## Risks / Trade-offs

- [假数据菜单指向的路由只到占位页，与真实业务页面不一致] → 占位页明确标注"建设中"，且数据结构与真实接口同构，接入 `GET /v1/ui/menus` 时只替换数据装配层。
- [`Menu` 内部展开态在搜索态被强制展开，清空搜索后分组回到用户此前的折叠态] → 这是刻意的：折叠态不因搜索被永久改写，清空后行为可预期；需在实现时确认组件未因过滤结果重建而丢失 state（key 需稳定）。
- [菜单规模变大后侧栏变长] → 侧栏 `nav` 已是 `overflow-y-auto`，滚动区域现成；仍需在构建后目视核对分组间距与滚动条不遮挡内容。
- [占位页类型属于新增内置组件，后续可能被真实页面替换] → 类型名保持中性（`placeholder`），注册表条目一处可删，不留其它耦合。
- [`filterMenus` 只按菜单项名称匹配，未命中名称但子项命中的分组需保留父链] → 实现时按"命中子项 → 保留祖先分组"构造子树，而不是简单丢弃父节点；对应 spec 的"命中过滤"场景。

## Migration Plan

纯前端展示层变更，无数据迁移、无接口变更。发布方式沿用既有链路：`ping-ui` 打包期 `npm run build` 产出 `renderer.js`/`renderer.css` 进 `META-INF/resources/assets/renderer/`，随 `ping-distribute` 聚合发布。回滚 = 回退该次提交重新构建。

## Open Questions

（无——搜索交互形态与"切换"的含义已在提案前与用户确认。）
