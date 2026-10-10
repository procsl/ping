import type { ComponentNode } from "@/schema/types"
import { Search } from "lucide-react"
import { cn } from "@/lib/utils"
import { Separator } from "@/components/ui/separator"
import { Input } from "@/components/ui/input"
import {
  ComponentRenderer,
  type RendererContext,
} from "@/components/renderer/registry"

export interface SidebarProps {
  title: string
  menus: ComponentNode[]
  ctx: RendererContext
  collapsed: boolean
  /** 菜单搜索关键字（受控，由主框架持有） */
  query: string
  onQueryChange: (query: string) => void
}

/** 左侧导航：logo + 菜单搜索框 + 菜单树，支持折叠（折叠态仅展示首字且隐藏搜索框） */
export function Sidebar({
  title,
  menus,
  ctx,
  collapsed,
  query,
  onQueryChange,
}: SidebarProps): React.ReactNode {
  const searching = query.trim().length > 0
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
      {!collapsed && (
        <div className="px-2 py-2">
          <div className="relative">
            <Search className="pointer-events-none absolute left-2 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={query}
              onChange={(event) => onQueryChange(event.target.value)}
              placeholder="搜索菜单"
              aria-label="搜索菜单"
              className="h-8 pl-7 text-sm"
            />
          </div>
        </div>
      )}
      <nav className="flex-1 overflow-y-auto py-2">
        {!collapsed && searching && menus.length === 0 ? (
          <p className="px-4 py-3 text-xs text-muted-foreground">
            无匹配菜单
          </p>
        ) : (
          menus.map((node, i) => (
            <ComponentRenderer key={node.id ?? i} node={node} ctx={ctx} />
          ))
        )}
      </nav>
    </aside>
  )
}
