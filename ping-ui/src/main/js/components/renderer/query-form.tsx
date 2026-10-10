import { Fragment } from "react"
import type { ReactNode } from "react"
import type { ComponentNode } from "@/schema/types"
import { Button } from "@/components/ui/button"

export interface QueryFormProps {
  node: ComponentNode
  render: (node: ComponentNode) => ReactNode
}

/**
 * 数据表的输入参数容器：渲染该 dataset 的查询条件字段。
 * 提交只做演示提示 —— 取数接入属 design 的 Non-Goals。
 */
export function QueryForm({ node, render }: QueryFormProps): ReactNode {
  const children = node.containers ?? []
  if (children.length === 0) {
    return null
  }

  return (
    <div className="rounded-md border p-3">
      {node.name && (
        <p className="mb-2 text-xs font-medium text-muted-foreground">{node.name}</p>
      )}
      <div className="flex flex-wrap items-end gap-3">
        <div className="flex flex-1 flex-wrap items-end gap-3">
          {children.map((child, i) => (
            <Fragment key={child.id ?? i}>{render(child)}</Fragment>
          ))}
        </div>
        <div className="flex shrink-0 gap-2">
          <Button type="button" size="sm" variant="outline">
            重置
          </Button>
          <Button type="button" size="sm">
            查询
          </Button>
        </div>
      </div>
    </div>
  )
}
