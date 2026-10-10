import type { ComponentNode } from "@/schema/types"
import { Link } from "react-router"
import { UserPanel } from "@/layout/UserPanel"
import { RouteOutlet } from "@/router/RouteOutlet"
import { DataTable } from "@/components/renderer/data-table"
import { NavGroup } from "@/components/renderer/nav-group"
import { Tabs } from "@/components/renderer/tabs"
import { BreadcrumbBar } from "@/components/renderer/breadcrumb-bar"
import { EmptyState } from "@/components/renderer/empty-state"
import { ColumnCell } from "@/components/renderer/column"
import { ActionButton } from "@/components/renderer/action"
import { QueryForm } from "@/components/renderer/query-form"
import { Field, Form, FormAction } from "@/components/renderer/form"
import { Placeholder } from "@/pages/Placeholder"
import { cn } from "@/lib/utils"

export interface RendererContext {
  /** 模块命名空间，动态组件按 /assets/<namespace>/components/<type>.js 寻址 */
  namespace: string
  /** 当前激活路由，用于菜单高亮 */
  activePath: string
  /** 侧栏是否折叠（菜单按此收敛为图标态） */
  collapsed?: boolean
  /** 侧栏搜索是否有关键字（搜索态下菜单分组强制展开） */
  searching?: boolean
}

export interface NodeRendererProps {
  node: ComponentNode
  ctx: RendererContext
}

export type NodeRenderer = (props: NodeRendererProps) => React.ReactNode

/** 供子渲染器使用的递归渲染器，避免渲染器之间互相 import 造成循环依赖 */
function childRenderer(ctx: RendererContext) {
  return (node: ComponentNode): React.ReactNode => (
    <ComponentRenderer node={node} ctx={ctx} />
  )
}

function renderChildren(node: ComponentNode, ctx: RendererContext): React.ReactNode {
  return (node.containers ?? []).map((child, i) => (
    <ComponentRenderer key={child.id ?? i} node={child} ctx={ctx} />
  ))
}

function Menu({ node, ctx }: NodeRendererProps): React.ReactNode {
  const children = node.containers ?? []
  const collapsed = ctx.collapsed ?? false
  const menuChildren = children.filter((child) => child.type === "menu")

  // 分组：子节点是菜单项 → 分组标题可点击展开/折叠（默认展开）
  if (menuChildren.length > 0) {
    return <NavGroup node={node} ctx={ctx} render={childRenderer(ctx)} />
  }

  // 叶子菜单项：其 containers 是页面内容（dataset / placeholder 等），由路由页渲染，侧栏只出链接
  if (!node.router) {
    return null
  }

  const path = `/${node.router.replace(/^\/+/, "")}`
  const isActive = ctx.activePath === path
  const label = node.name ?? node.id ?? ""

  if (collapsed) {
    return (
      <div className="flex justify-center px-2 py-1" title={label}>
        <Link
          to={path}
          className={cn(
            "flex h-8 w-8 items-center justify-center rounded-md text-sm transition-colors",
            isActive
              ? "bg-sidebar-accent font-medium text-sidebar-accent-foreground"
              : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
          )}
        >
          {label.slice(0, 1)}
        </Link>
      </div>
    )
  }

  return (
    <div className="px-2 py-0.5">
      <Link
        to={path}
        className={cn(
          "flex h-8 items-center truncate rounded-md px-2 text-sm transition-colors",
          isActive
            ? "bg-sidebar-accent font-medium text-sidebar-accent-foreground"
            : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
        )}
        title={label}
      >
        {label}
      </Link>
    </div>
  )
}

/**
 * 内置组件注册表（spec：内置常用后台管理抽象组件类型）：
 * 覆盖 16 个常用类型 + 2 个框架壳体类型；渲染器按节点 type 查表，
 * 命中直接渲染，未命中降级为占位节点并告警，不阻断整页。
 */
export const registry: Record<string, NodeRenderer> = {
  application: ({ node, ctx }) => <>{renderChildren(node, ctx)}</>,
  layout: ({ node, ctx }) => <>{renderChildren(node, ctx)}</>,
  menu: Menu,
  nav_group: ({ node, ctx }) => (
    <NavGroup node={node} ctx={ctx} render={childRenderer(ctx)} />
  ),
  tabs: ({ node, ctx }) => (
    <Tabs node={node} ctx={ctx} render={childRenderer(ctx)} />
  ),
  breadcrumb: ({ node }) => <BreadcrumbBar node={node} />,
  dataset: ({ node, ctx }) => (
    <DataTable node={node} ctx={ctx} render={childRenderer(ctx)} />
  ),
  query: ({ node, ctx }) => <QueryForm node={node} render={childRenderer(ctx)} />,
  column: ({ node }) => <ColumnCell node={node} />,
  row_action: ({ node }) => <ActionButton node={node} />,
  action: ({ node }) => <ActionButton node={node} />,
  form: ({ node, ctx }) => <Form node={node} render={childRenderer(ctx)} />,
  field: ({ node }) => <Field node={node} />,
  form_action: ({ node }) => <FormAction node={node} />,
  placeholder: ({ node }) => <Placeholder name={node.name} id={node.id} />,
  empty: ({ node }) => <EmptyState node={node} />,
  user_info_panel: () => <UserPanel />,
  main_container: () => <RouteOutlet />,
}

export function isKnownType(type: string): boolean {
  return type in registry
}

/** 单节点渲染入口：查注册表 → 降级占位（未知类型保留其子节点，不出现空白区） */
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
      {(node.containers ?? []).length > 0 && (
        <div className="mt-1 space-y-1">
          {(node.containers ?? []).map((child, i) => (
            <ComponentRenderer key={child.id ?? i} node={child} ctx={ctx} />
          ))}
        </div>
      )}
    </div>
  )
}
