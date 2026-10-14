/**
 * `application` 处理器 —— 应用根。
 *
 * 对应 `ping-ui/doc/abstract-component-schema.md` §5.1：
 * - 职责：整棵组件树的根，声明应用标识与标题；
 * - properties：`name`（应用名，显示在侧栏 logo 位）；
 * - children：`layout`、`nav_group`、`menu`、`tabs`、`breadcrumb`、`user_info_panel`、`main_container`；
 * - 渲染：不直接渲染，向布局透出 `name` 与子树；
 * - 错误处理：缺失 `application` 根 → 前端降级本地声明并 WARN。
 *
 * SDK 不渲染 UI（框架无关）：处理器把「渲染意图」经 messageBus 发出，
 * 由渲染层订阅 `ui:render:application` 事件完成实际渲染；随后按 design D3
 * 主动派发全部子节点（布局、导航等由各自的处理器继续处理）。
 */

import type { ComponentNode, Handler, PingUi } from "../types"
import type { MessageBus } from "../message-bus"

/** `application` 的渲染事件名 */
export const APPLICATION_RENDER_EVENT = "ui:render:application"

/** 渲染意图载荷：渲染层据此构建可见输出 */
export interface ApplicationRenderPayload {
  /** 节点 id（可能缺省） */
  nodeId?: string
  /** 应用名：`properties.name` → `node.name` → 「Ping Admin」 */
  name: string
  /** 完整节点，供渲染层取子树等字段 */
  node: ComponentNode
}

/** 缺省应用名（规范接口契约示例 `pages[0].name` 为 "Ping Admin"） */
export const DEFAULT_APPLICATION_NAME = "Ping Admin"

/** 解析应用名：`properties.name` → `node.name` → 「Ping Admin」 */
export function resolveApplicationName(node: ComponentNode): string {
  const fromProps = node.properties?.name
  if (typeof fromProps === "string" && fromProps.trim() !== "") {
    return fromProps
  }
  if (node.name && node.name.trim() !== "") {
    return node.name
  }
  return DEFAULT_APPLICATION_NAME
}

/**
 * `application` 处理器：解析应用名发出渲染事件，并派发全部子节点。
 * 子节点派发由处理器主动驱动（design D3），SDK 不自动遍历树。
 */
export const applicationHandler: Handler = (
  tree: ComponentNode,
  node: ComponentNode,
  messageBus: MessageBus,
  sdk: PingUi,
): void => {
  const name = resolveApplicationName(node)
  const payload: ApplicationRenderPayload = { nodeId: node.id, name, node }
  messageBus.emit(APPLICATION_RENDER_EVENT, payload)

  // 派发全部子节点（layout / nav_group / menu / tabs / breadcrumb / user_info_panel / main_container）
  const created = sdk.create(tree)
  for (const child of node.children ?? []) {
    created(child)
  }
}
