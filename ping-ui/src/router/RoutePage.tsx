import type { ComponentNode } from "@/schema/types"
import { ComponentRenderer } from "@/components/renderer/registry"
import { Dashboard } from "@/pages/Dashboard"
import { NotFound } from "@/pages/NotFound"

export interface RoutePageProps {
  node?: ComponentNode
  namespace: string
  activePath: string
}

/** 路由页：菜单节点携带页面内容时按组件树渲染，否则落到框架默认工作台 */
export function RoutePage({
  node,
  namespace,
  activePath,
}: RoutePageProps): React.ReactNode {
  const ctx = { namespace, activePath }
  const content = (node?.containers ?? []).filter(
    (c) => c.type !== "main_container",
  )

  if (content.length === 0) {
    return <Dashboard />
  }

  return (
    <div className="space-y-4">
      {content.map((child, i) => (
        <ComponentRenderer key={child.id ?? i} node={child} ctx={ctx} />
      ))}
    </div>
  )
}

export { NotFound }
