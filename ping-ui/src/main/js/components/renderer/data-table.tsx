import type { ComponentNode } from "@/schema/types"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { mockUserRows } from "@/mock"
import type { RendererContext } from "@/components/renderer/registry"

export interface DataTableProps {
  node: ComponentNode
  ctx: RendererContext
}

/** 内置 table 渲染：列与动作来自节点 containers，数据暂用本地演示行（后端 API 绑定后接入） */
export function DataTable({ node }: DataTableProps): React.ReactNode {
  const columns = (node.containers ?? []).filter((c) => c.type === "column")
  const actions = (node.containers ?? []).filter((c) => c.type === "action")

  if (columns.length === 0) {
    return (
      <div className="rounded-md border border-dashed px-3 py-2 text-xs text-muted-foreground">
        表格未声明 column：{node.id ?? node.name ?? node.type}
      </div>
    )
  }

  const rows = mockUserRows

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold">{node.name ?? "数据表"}</h2>
          {node.description && (
            <p className="text-sm text-muted-foreground">{node.description}</p>
          )}
        </div>
        <div className="flex gap-2">
          {actions.map((action) => (
            <Button key={action.id} variant="outline" size="sm">
              {action.name}
            </Button>
          ))}
        </div>
      </div>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              {columns.map((column) => (
                <TableHead key={column.id}>{column.name}</TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>
                {columns.map((column) => (
                  <TableCell key={column.id}>
                    {column.id === "col.status" ? (
                      <Badge variant="secondary">{row.status}</Badge>
                    ) : column.id === "col.account" ? (
                      row.account
                    ) : (
                      row.nickname
                    )}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <p className="text-xs text-muted-foreground">
        数据源：mock（声明 API：{node.apis?.[0]?.api ?? "未绑定"}，接入后端后自动切换）
      </p>
    </div>
  )
}
