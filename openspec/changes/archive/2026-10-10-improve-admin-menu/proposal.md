# Proposal

## Why

`ping-ui` 的后台管理主框架（侧栏 + 顶栏 + 多标签 + 内容区）已经能跑，但左侧菜单的假数据只有「首页」「系统管理」两组，无法验证框架在真实菜单规模下的表现；同时侧栏完全没有搜索能力，菜单分组也没有展开/折叠交互，用户只能靠滚动和逐项点击来切换菜单。这次变更要在不接入后端接口的前提下，用假数据把菜单浏览体验补完整，为后续接 `GET /v1/ui/menus` 打好形态基础。

## What Changes

- **扩充菜单假数据**：把 `data/default-tree.ts` 的菜单树从 2 组扩充为多个分组、多个菜单项（两段式路由 `/xxx/xxx`），覆盖首页、系统管理等更多管理场景，用于验证框架在多菜单下的布局与滚动表现。
- **新增侧栏菜单搜索**：侧栏内置搜索输入框，实时过滤菜单树，只展示命中的菜单项及其所属分组；无命中时给出空态提示；清空关键字恢复完整菜单。
- **完善菜单切换**：菜单分组标题支持点击展开/折叠（默认展开，折叠态保留分组标题），与既有的菜单项高亮、侧栏整体收起、多标签回退协同工作；搜索命中时自动展开被命中的分组。
- **新增菜单项占位页**：为扩充出来的菜单项提供统一的占位内容，避免所有菜单都落到默认工作台、切换后看不出差异。

不改动后端接口与数据契约：菜单数据仍来自本地组件树，后端 `/v1/system/menus` 的接入不在本次范围内。

## Capabilities

### New Capabilities

- `admin-navigation`: 后台管理框架的左侧导航能力——多菜单展示、菜单项与分组的切换（激活高亮、分组展开/折叠、侧栏折叠）、以及侧栏内对菜单树的搜索过滤。

### Modified Capabilities

（无：`openspec/specs/` 当前为空，没有既有能力的需求发生变化。）

## Impact

- **代码**：`ping-ui` 前端为主——`src/main/js/data/default-tree.ts`（假数据）、`src/main/js/layout/Sidebar.tsx`（搜索框与状态）、`src/main/js/components/renderer/registry.tsx`（Menu 分组渲染与展开/折叠）、`src/main/js/router/derive.ts`（菜单过滤）、`src/main/js/layout/AdminLayout.tsx`（搜索状态装配），以及新增占位页组件。
- **测试/构建**：`ping-ui` 的 `npm run build`（`tsc --noEmit` + vite build）需通过；若既有测试覆盖菜单结构则同步更新。
- **不受影响**：后端 `ping-system` 的 UI 接口、`@ResourceReference`、菜单权限、OpenAPI 生成；`META-INF/resources` 静态资源打包路径与薄壳约定保持不变。
