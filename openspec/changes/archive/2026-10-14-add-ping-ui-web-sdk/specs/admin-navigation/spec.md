# Spec Delta

## REMOVED Requirements

### Requirement: 侧栏展示菜单树

**Reason**: 该需求的实现是旧的 React 渲染器（`registry.tsx` 推导的侧栏树，数据来自 `data/default-tree.ts` 的扁平 `containers` 声明）。本次变更按 `ping-ui/doc/abstract-component-schema.md` 的目标节点模型从头重建前端起点，旧渲染器与扁平声明一并删除，侧栏展示能力在重建完成前不再提供。

**Migration**: 后续变更用 `ui-component-sdk` 注册 `application` / `layout` / `nav_group` / `menu` 类型的处理器重建侧栏，数据改由目标态 `children` + `properties` 声明提供，重建后重新提出本需求。

### Requirement: 菜单项切换当前页面

**Reason**: 切换与激活态由旧的 `router/derive.ts` 路由推导与 `Sidebar` 实现承载，两者均随本次删除移除。

**Migration**: 重建时以 SDK 处理器驱动路由推导（`properties.router` 两段式相对路径），重建后重新提出本需求。

### Requirement: 菜单分组展开与折叠

**Reason**: 分组展开态由旧的 `nav-group.tsx` 渲染器本地状态承载，该渲染器随本次删除移除。

**Migration**: 重建时由 `nav_group` 类型的处理器实现展开/折叠（默认展开、搜索态强制展开），重建后重新提出本需求。

### Requirement: 侧栏菜单搜索

**Reason**: 搜索由旧的 `router/derive.ts` 中 `filterMenus` 纯函数与 `AdminLayout` 搜索态承载，两者均随本次删除移除。

**Migration**: 重建时在 SDK 处理器之上重新实现 `filterMenus`（含祖先链保留、无命中空态、搜索态强制展开），重建后重新提出本需求。
