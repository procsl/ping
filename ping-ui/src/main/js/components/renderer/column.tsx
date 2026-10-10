import type { ReactNode } from "react"
import type { ComponentNode } from "@/schema/types"
import { Badge } from "@/components/ui/badge"
import { ArrowDownUp } from "lucide-react"

export interface ColumnCellProps {
  node: ComponentNode
}

/**
 * 展示列的独立渲染：在 dataset 内部由数据渲染器统一处理，
 * 这里只负责 dataset 之外出现时的兜底展示，避免未知组件占位。
 */
export function ColumnCell({ node }: ColumnCellProps): ReactNode {
  const title = node.title ?? node.name ?? node.field ?? node.id ?? "列"
  return (
    <div className="flex items-center gap-1.5 rounded-md border border-dashed px-2 py-1 text-xs text-muted-foreground">
      <span className="truncate">{title}</span>
      {node.sortable && <ArrowDownUp className="h-3 w-3 shrink-0" aria-label="可排序" />}
      {node.format && <Badge variant="outline">{node.format}</Badge>}
    </div>
  )
}
