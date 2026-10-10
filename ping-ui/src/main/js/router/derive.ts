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

/**
 * 侧栏菜单搜索：按关键字过滤菜单树。
 * 关键字忽略首尾空白与大小写，按菜单项名称匹配；
 * 只有命中菜单项的分组会连同其祖先链保留，未命中的分组整体移除；
 * 关键字为空时原样返回整棵树。
 */
export function filterMenus(
  menus: ComponentNode[],
  query: string,
): ComponentNode[] {
  const keyword = query.trim().toLowerCase()
  if (!keyword) {
    return menus
  }

  const matches = (node: ComponentNode): boolean =>
    (node.name ?? node.id ?? "").toLowerCase().includes(keyword)

  const isGroup = (node: ComponentNode): boolean =>
    (node.containers ?? []).some((child) => child.type === "menu")

  const filterGroup = (group: ComponentNode): ComponentNode | null => {
    const items = (group.containers ?? [])
      .filter((child) => child.type === "menu")
      .map((child) =>
        isGroup(child)
          ? filterGroup(child)
          : matches(child)
            ? child
            : null,
      )
      .filter((child): child is ComponentNode => child !== null)
    return items.length > 0 ? { ...group, containers: items } : null
  }

  return menus
    .map((menu) => (isGroup(menu) ? filterGroup(menu) : null))
    .filter((menu): menu is ComponentNode => menu !== null)
}
