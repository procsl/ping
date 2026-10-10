import type { ComponentNode } from "@/schema/types"
import { cn } from "@/lib/utils"
import { Separator } from "@/components/ui/separator"
import {
  ComponentRenderer,
  type RendererContext,
} from "@/components/renderer/registry"

export interface SidebarProps {
  title: string
  menus: ComponentNode[]
  ctx: RendererContext
  collapsed: boolean
}

/** 左侧导航：logo + 菜单树，支持折叠（折叠态仅展示首字） */
export function Sidebar({
  title,
  menus,
  ctx,
  collapsed,
}: SidebarProps): React.ReactNode {
  return (
    <aside
      className={cn(
        "flex shrink-0 flex-col border-r bg-sidebar transition-[width] duration-200",
        collapsed ? "w-14" : "w-56",
      )}
    >
      <div
        className={cn(
          "flex h-12 items-center gap-2",
          collapsed ? "justify-center px-2" : "px-4",
        )}
        title={collapsed ? title : undefined}
      >
        <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-md bg-primary text-xs font-bold text-primary-foreground">
          P
        </div>
        {!collapsed && (
          <span className="truncate text-sm font-semibold">{title}</span>
        )}
      </div>
      <Separator />
      <nav className="flex-1 overflow-y-auto py-2">
        {menus.map((node, i) => (
          <ComponentRenderer key={node.id ?? i} node={node} ctx={ctx} />
        ))}
      </nav>
    </aside>
  )
}
