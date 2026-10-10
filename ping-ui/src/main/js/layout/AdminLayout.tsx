import { useEffect, useMemo, useState } from "react"
import { Outlet, useLocation } from "react-router"
import type { ComponentNode } from "@/schema/types"
import { deriveMenu, deriveRoutes, filterMenus } from "@/router/derive"
import type { RendererContext } from "@/components/renderer/registry"
import { Sidebar } from "@/layout/Sidebar"
import { Header } from "@/layout/Header"
import { TagsView } from "@/layout/TagsView"

export interface AdminLayoutProps {
  tree: ComponentNode
  namespace: string
  title: string
}

/**
 * 后台管理主框架：
 * 侧栏（可折叠，菜单由组件树 layout[left] 推导）
 * + 顶栏（折叠开关 / 面包屑 / 主题切换 / 用户面板）
 * + 多标签导航（访问过的路由可关闭回退）
 * + 内容区（路由出口）
 */
export function AdminLayout({
  tree,
  namespace,
  title,
}: AdminLayoutProps): React.ReactNode {
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(
    () => localStorage.getItem("ping.collapsed") === "true",
  )
  const [query, setQuery] = useState("")

  useEffect(() => {
    localStorage.setItem("ping.collapsed", String(collapsed))
  }, [collapsed])

  const menus = useMemo(() => deriveMenu(tree), [tree])
  const routes = useMemo(() => deriveRoutes(tree), [tree])
  const visibleMenus = useMemo(
    () => filterMenus(menus, query),
    [menus, query],
  )

  const ctx: RendererContext = {
    namespace,
    activePath: location.pathname,
    collapsed,
    searching: query.trim().length > 0,
  }

  return (
    <div className="flex h-screen w-full overflow-hidden bg-background">
      <Sidebar
        title={title}
        menus={visibleMenus}
        ctx={ctx}
        collapsed={collapsed}
        query={query}
        onQueryChange={setQuery}
      />

      <div className="flex min-w-0 flex-1 flex-col">
        <Header
          activePath={location.pathname}
          collapsed={collapsed}
          onToggleCollapsed={() => setCollapsed((prev) => !prev)}
        />
        <TagsView routes={routes} />
        <main className="flex-1 overflow-y-auto p-4">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
