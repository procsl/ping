import type { ComponentNode } from "@/schema/types"

export interface DerivedRoute {
  /** 前端路由路径，两段式 /xxx/xxx */
  path: string
  /** 挂载该路由的菜单节点 */
  node: ComponentNode
  /** 展示名 */
  name: string
}

const EXTERNAL = /^(https?:)?\/\//

function normalize(router: string): string | null {
  const trimmed = router.trim().replace(/^\/+|\/+$/g, "")
  if (!trimmed || EXTERNAL.test(router)) {
    return null
  }
  return `/${trimmed}`
}

/** 深度优先遍历，收集声明了 router 的菜单节点 */
function walk(node: ComponentNode, out: DerivedRoute[]): void {
  const children = node.containers ?? []
  for (const child of children) {
    walk(child, out)
  }
  if (node.type === "menu" && node.router) {
    const path = normalize(node.router)
    if (path) {
      out.push({ path, node, name: node.name ?? path })
    }
  }
}

/**
 * 组件树 → 前端路由表（spec: 路由由数据推导，MUST NOT 由前端静态配置维护）。
 * 同级按 order 升序，缺省 order 视为 0。
 */
export function deriveRoutes(tree: ComponentNode): DerivedRoute[] {
  const routes: DerivedRoute[] = []
  walk(tree, routes)
  return routes.sort((a, b) => (a.node.order ?? 0) - (b.node.order ?? 0))
}

/** 收集左侧导航菜单（application → layout[left] 下的 menu 树），按 order 排序 */
export function deriveMenu(tree: ComponentNode): ComponentNode[] {
  const roots: ComponentNode[] = []
  const visit = (node: ComponentNode): void => {
    if (
      node.type === "layout" &&
      node.layout === "left" &&
      node.containers
    ) {
      roots.push(...node.containers)
      return
    }
    node.containers?.forEach(visit)
  }
  visit(tree)
  return roots.sort((a, b) => (a.order ?? 0) - (b.order ?? 0))
}
