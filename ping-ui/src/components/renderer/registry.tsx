import type { ComponentNode } from "@/schema/types"
import { Link } from "react-router"
import { cn } from "@/lib/utils"
import { UserPanel } from "@/layout/UserPanel"
import { RouteOutlet } from "@/router/RouteOutlet"
import { DataTable } from "@/components/renderer/data-table"

export interface RendererContext {
  /** 模块命名空间，动态组件按 /assets/<namespace>/components/<type>.js 寻址 */
  namespace: string
  /** 当前激活路由，用于菜单高亮 */
  activePath: string
}

export interface NodeRendererProps {
  node: ComponentNode
  ctx: RendererContext
}

export type NodeRenderer = (props: NodeRendererProps) => React.ReactNode

function renderChildren(
  node: ComponentNode,
  ctx: RendererContext,
): React.ReactNode {
  return (node.containers ?? []).map((child, i) => (
    <ComponentRenderer key={child.id ?? i} node={child} ctx={ctx} />
  ))
}

function Menu({ node, ctx }: NodeRendererProps): React.ReactNode {
  const children = node.containers ?? []

  if (children.length > 0) {
    return (
      <div className="px-2 pb-1 pt-3">
        <div className="mb-1 px-2 text-xs font-medium text-muted-foreground">
          {node.name ?? node.id}
        </div>
        <div className="space-y-0.5">{renderChildren(node, ctx)}</div>
      </div>
    )
  }

  if (!node.router) {
    return null
  }

  const path = `/${node.router.replace(/^\/+/, "")}`
  const isActive = ctx.activePath === path

  return (
    <Link
      to={path}
      className={cn(
        "flex h-8 items-center rounded-md px-2 text-sm transition-colors",
        isActive
          ? "bg-sidebar-accent font-medium text-sidebar-accent-foreground"
          : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
      )}
    >
      {node.name ?? node.id}
    </Link>
  )
}

/**
 * 内置组件注册表（spec 4.5）：渲染器按节点 type 查表，命中直接渲染；
 * 未命中由 renderNode 降级为占位节点并告警，不阻断整页（spec 5.2）。
 */
export const registry: Record<string, NodeRenderer> = {
  menu: Menu,
  user_info_panel: ({ node }) => <UserPanel node={node} />,
  main_container: () => <RouteOutlet />,
  table: ({ node, ctx }) => <DataTable node={node} ctx={ctx} />,
  application: ({ node, ctx }) => <>{renderChildren(node, ctx)}</>,
  layout: ({ node, ctx }) => <>{renderChildren(node, ctx)}</>,
}

export function isKnownType(type: string): boolean {
  return type in registry
}

/** 单节点渲染入口：查注册表 → 降级占位 */
export function ComponentRenderer({
  node,
  ctx,
}: NodeRendererProps): React.ReactNode {
  const renderer = registry[node.type]
  if (renderer) {
    return renderer({ node, ctx })
  }
  console.warn(`[ping-ui] 未知组件类型 "${node.type}"，渲染为占位节点`)
  return (
    <div className="rounded-md border border-dashed px-3 py-2 text-xs text-muted-foreground">
      未知组件：{node.type}
    </div>
  )
}
