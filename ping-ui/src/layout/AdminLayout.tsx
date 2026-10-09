import { Outlet, useLocation } from "react-router"
import type { ComponentNode } from "@/schema/types"
import { deriveMenu } from "@/router/derive"
import {
  ComponentRenderer,
  type RendererContext,
} from "@/components/renderer/registry"
import { Separator } from "@/components/ui/separator"
import { Breadcrumb } from "@/components/renderer/breadcrumb"
import { UserPanel } from "@/layout/UserPanel"

export interface AdminLayoutProps {
  tree: ComponentNode
  namespace: string
  title: string
}

/**
 * 后台管理框架主布局：
 * 左侧菜单（组件树 layout[left] 推导）+ 顶栏（面包屑 / 用户面板）+ 内容区（路由出口）
 */
export function AdminLayout({
  tree,
  namespace,
  title,
}: AdminLayoutProps): React.ReactNode {
  const location = useLocation()
  const menus = deriveMenu(tree)
  const ctx: RendererContext = {
    namespace,
    activePath: location.pathname,
  }

  const topPanel = findNode(tree, "user_info_panel")

  return (
    <div className="flex h-screen w-full overflow-hidden bg-background">
      <aside className="flex w-56 shrink-0 flex-col border-r bg-sidebar">
        <div className="flex h-12 items-center gap-2 px-4">
          <div className="flex h-6 w-6 items-center justify-center rounded-md bg-primary text-xs font-bold text-primary-foreground">
            P
          </div>
          <span className="text-sm font-semibold">{title}</span>
        </div>
        <Separator />
        <nav className="flex-1 overflow-y-auto py-2">
          {menus.map((node, i) => (
            <ComponentRenderer key={node.id ?? i} node={node} ctx={ctx} />
          ))}
        </nav>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-12 shrink-0 items-center justify-between gap-4 border-b px-4">
          <Breadcrumb activePath={location.pathname} />
          <div className="flex items-center gap-2">
            {topPanel ? (
              <ComponentRenderer node={topPanel} ctx={ctx} />
            ) : (
              <UserPanel node={{ type: "user_info_panel" }} />
            )}
          </div>
        </header>
        <main className="flex-1 overflow-y-auto p-4">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

/** 侧边栏不需要整体渲染树，只抽取菜单；这里辅助查找顶栏面板节点 */
function findNode(node: ComponentNode, type: string): ComponentNode | null {
  if (node.type === type) {
    return node
  }
  for (const child of node.containers ?? []) {
    const found = findNode(child, type)
    if (found) {
      return found
    }
  }
  return null
}
