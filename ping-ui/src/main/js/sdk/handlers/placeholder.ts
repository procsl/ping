/**
 * `placeholder` 处理器 —— 页面占位。
 *
 * 对应 `ping-ui/doc/abstract-component-schema.md` §5.5：
 * - 职责：明确告知「这个页面在建设中」，保证菜单切换有可辨识的内容差异；
 * - properties：`name`（页面名）；
 * - children：无；
 * - 渲染：居中显示 `{name} · 页面建设中`；
 * - 错误处理：`name` 缺失回落 `id`，再缺用「该页面」。
 *
 * SDK 不渲染 UI（框架无关）：处理器把「渲染意图」经 messageBus 发出，
 * 由渲染层订阅 `ui:render` 事件完成实际渲染；无订阅时静默（处理器无副作用）。
 */

import type { ComponentNode, Handler } from "../types"
import type { MessageBus } from "../message-bus"

/** `placeholder` 的渲染事件名 */
export const PLACEHOLDER_RENDER_EVENT = "ui:render:placeholder"

/** 渲染意图载荷：渲染层据此构建可见输出 */
export interface PlaceholderRenderPayload {
  /** 节点 id（可能缺省） */
  nodeId?: string
  /** 页面名：`properties.name` → `id` → 「该页面」 */
  name: string
  /** 完整节点，供渲染层取其它字段 */
  node: ComponentNode
}

/** 解析页面名：`properties.name` 缺失回落 `id`，再缺用「该页面」 */
export function resolvePlaceholderName(node: ComponentNode): string {
  const fromProps = node.properties?.name
  if (typeof fromProps === "string" && fromProps.trim() !== "") {
    return fromProps
  }
  if (node.name && node.name.trim() !== "") {
    return node.name
  }
  if (node.id) {
    return node.id
  }
  return "该页面"
}

/**
 * `placeholder` 处理器：解析页面名并发出渲染事件。
 * 无子节点派发（规范：children 无）。
 */
export const placeholderHandler: Handler = (
  _tree: ComponentNode,
  node: ComponentNode,
  messageBus: MessageBus,
): void => {
  const name = resolvePlaceholderName(node)
  const payload: PlaceholderRenderPayload = { nodeId: node.id, name, node }
  messageBus.emit(PLACEHOLDER_RENDER_EVENT, payload)
}
