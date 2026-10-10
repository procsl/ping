import { useState } from "react"
import type { ReactNode } from "react"
import type { ComponentNode } from "@/schema/types"
import type { RendererContext } from "@/components/renderer/registry"
import { cn } from "@/lib/utils"

export interface TabsProps {
  node: ComponentNode
  ctx: RendererContext
  render: (node: ComponentNode) => ReactNode
}

/**
 * 页签容器：页签标题取子节点的 name，未激活页签不渲染其内容（避免无谓的数据请求）。
 * active 指向不存在的 id 时回落到第一个子节点。
 */
export function Tabs({ node, render }: TabsProps): ReactNode {
  const children = node.containers ?? []
  const [active, setActive] = useState<string | undefined>(node.active)
  const initial = active ?? children[0]?.id
  const current = children.find((child) => child.id === active) ??
    children.find((child) => child.id === initial) ??
    children[0]

  if (children.length === 0) {
    return null
  }

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap items-center gap-1 border-b pb-1">
        {children.map((child, i) => {
          const key = child.id ?? String(i)
          const selected = current?.id === child.id || (current === child && active === undefined)
          return (
            <button
              key={key}
              type="button"
              onClick={() => setActive(child.id)}
              className={cn(
                "px-3 py-1.5 text-sm transition-colors",
                selected
                  ? "border-b-2 border-primary font-medium text-foreground"
                  : "text-muted-foreground hover:text-foreground",
              )}
            >
              {child.name ?? child.id ?? child.type}
            </button>
          )
        })}
      </div>
      {current ? render(current) : null}
    </div>
  )
}
