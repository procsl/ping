import { useState } from "react"
import { Fragment } from "react"
import type { ReactNode } from "react"
import type { ComponentNode } from "@/schema/types"
import type { RendererContext } from "@/components/renderer/registry"
import { ChevronDown } from "lucide-react"
import { cn } from "@/lib/utils"

export interface NavGroupProps {
  node: ComponentNode
  ctx: RendererContext
  render: (node: ComponentNode) => ReactNode
}

/**
 * 菜单分组（nav_group）：可展开/折叠的分组标题，默认展开；
 * 搜索态强制展开且不可点击折叠；侧栏整体收起时只留一条分隔线。
 * 行为与 admin-navigation 规范中 menu 分组形态一致。
 */
export function NavGroup({ node, ctx, render }: NavGroupProps): ReactNode {
  const [open, setOpen] = useState(true)
  const collapsed = ctx.collapsed ?? false
  const searching = ctx.searching ?? false
  const children = node.containers ?? []

  if (collapsed) {
    return <div className="my-1 border-b border-sidebar-border/60 pb-1 pt-2" />
  }

  const expanded = open || searching

  return (
    <div className="px-2 pb-1 pt-3">
      <button
        type="button"
        aria-expanded={expanded}
        onClick={() => {
          if (!searching) {
            setOpen(!expanded)
          }
        }}
        className={cn(
          "flex w-full items-center justify-between rounded-md px-2 py-1 text-xs font-medium transition-colors",
          "text-muted-foreground hover:text-foreground",
          searching && "cursor-default",
        )}
        title={node.name ?? node.id}
      >
        <span className="truncate">{node.name ?? node.id}</span>
        <ChevronDown
          className={cn(
            "h-3.5 w-3.5 shrink-0 transition-transform duration-200",
            expanded ? "rotate-180" : "rotate-0",
          )}
        />
      </button>
      {expanded && (
        <div className="mt-1 space-y-0.5">{children.map((child, i) => (
          <Fragment key={child.id ?? i}>{render(child)}</Fragment>
        ))}</div>
      )}
    </div>
  )
}
