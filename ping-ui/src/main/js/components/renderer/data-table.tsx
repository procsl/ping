import { useState } from "react"
import type { ReactNode } from "react"
import type { ComponentNode } from "@/schema/types"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { ArrowDown, ArrowUp, ArrowUpDown } from "lucide-react"
import { mockUserRows } from "@/mock"
import type { RendererContext } from "@/components/renderer/registry"
import { cn } from "@/lib/utils"

export interface DataTableProps {
  node: ComponentNode
  ctx: RendererContext
  /** 由注册表注入，用于渲染 query / 嵌套等子节点，避免与注册表循环依赖 */
  render?: (node: ComponentNode) => ReactNode
}

type SortDir = "asc" | "desc"

const HANDLED = new Set(["column", "action", "row_action", "query"])

/**
 * dataset 渲染：从 containers 分离 query（输入参数）、column（展示列）、
 * action（工具栏）、row_action（行内动作）。
 *
 * 描述只提供语义与数据契约，本实现可整体替换为卡片、看板等其它形态，
 * 而组件描述与接口返回保持不变。
 */
export function DataTable({ node, render }: DataTableProps): ReactNode {
  const children = node.containers ?? []
  const columns = children.filter((child) => child.type === "column")
  const toolbar = children.filter((child) => child.type === "action")
  const rowActions = children.filter((child) => child.type === "row_action")
  const queries = children.filter((child) => child.type === "query")
  const others = children.filter((child) => !HANDLED.has(child.type))

  const [sort, setSort] = useState<{ field: string; dir: SortDir } | null>(null)

  if (columns.length === 0) {
    return (
      <div className="rounded-md border border-dashed px-3 py-2 text-xs text-muted-foreground">
        表格未声明 column：{node.id ?? node.name ?? node.type}
      </div>
    )
  }

  const rows = sort
    ? [...mockUserRows].sort((a, b) => {
        const source = a as unknown as Record<string, unknown>
        const target = b as unknown as Record<string, unknown>
        const left = String(source[sort.field] ?? "")
        const right = String(target[sort.field] ?? "")
        return sort.dir === "asc"
          ? left.localeCompare(right, "zh-CN")
          : right.localeCompare(left, "zh-CN")
      })
    : mockUserRows

  const toggleSort = (field: string) => {
    setSort((prev) =>
      prev?.field === field
        ? prev.dir === "asc"
          ? { field, dir: "desc" }
          : null
        : { field, dir: "asc" },
    )
  }

  const cellValue = (row: Record<string, unknown>, field?: string) => {
    if (!field) {
      return undefined
    }
    if (!(field in row)) {
      console.warn(
        `[ping-ui] 列字段 ${field} 不在渲染数据中（数据契约与实际行数据不一致）`,
      )
      return undefined
    }
    return row[field]
  }

  return (
    <div className="space-y-3">
      {queries.map((query, i) => (
        <div key={query.id ?? i}>{render ? render(query) : null}</div>
      ))}

      <div className="flex items-center justify-between gap-2">
        <div>
          <h2 className="text-lg font-semibold">{node.title ?? node.name ?? "数据表"}</h2>
          {node.description && (
            <p className="text-sm text-muted-foreground">{node.description}</p>
          )}
        </div>
        {toolbar.length > 0 && (
          <div className="flex gap-2">
            {toolbar.map((action) => (
              <div key={action.id ?? action.name}>
                {render ? render(action) : null}
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="overflow-hidden rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              {columns.map((column) => {
                const field = column.field
                const sortable = column.sortable === true && Boolean(field)
                const active = sort?.field === field
                return (
                  <TableHead
                    key={column.id ?? field ?? column.name}
                    style={column.width ? { width: column.width } : undefined}
                    className={cn(column.align === "right" && "text-right")}
                  >
                    {sortable ? (
                      <button
                        type="button"
                        onClick={() => toggleSort(field as string)}
                        className="inline-flex items-center gap-1 hover:text-foreground"
                        title="按此列排序"
                      >
                        {column.title ?? column.name ?? field}
                        {active ? (
                          sort?.dir === "asc" ? (
                            <ArrowUp className="h-3 w-3" />
                          ) : (
                            <ArrowDown className="h-3 w-3" />
                          )
                        ) : (
                          <ArrowUpDown className="h-3 w-3 opacity-50" />
                        )}
                      </button>
                    ) : (
                      (column.title ?? column.name ?? field)
                    )}
                  </TableHead>
                )
              })}
              {rowActions.length > 0 && <TableHead className="text-right">操作</TableHead>}
            </TableRow>
          </TableHeader>
          <TableBody>
            {rows.map((row) => (
              <TableRow key={row.id}>
                {columns.map((column) => {
                  const raw = cellValue(row as unknown as Record<string, unknown>, column.field)
                  const mapped = column.enum && raw != null
                    ? (column.enum[String(raw)] ?? String(raw))
                    : raw
                  return (
                    <TableCell
                      key={column.id ?? column.field ?? column.name}
                      className={cn(column.align === "right" && "text-right")}
                    >
                      {column.format === "enum" ? (
                        <Badge variant={mapped === "停用" ? "secondary" : "outline"}>
                          {String(mapped ?? "")}
                        </Badge>
                      ) : (
                        String(mapped ?? "")
                      )}
                    </TableCell>
                  )
                })}
                {rowActions.length > 0 && (
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-1.5">
                      {rowActions.map((action) => (
                        <div key={action.id ?? action.name}>
                          {render ? render(action) : null}
                        </div>
                      ))}
                    </div>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <p className="text-xs text-muted-foreground">
        数据源：mock（声明 API：
        {node.api
          ? typeof node.api === "object"
            ? node.api.path
            : node.api
          : "未绑定"}
        ，接入后端后自动切换） · 行动作 {rowActions.length} 个 · 列 {columns.length} 列
      </p>

      {others.map((child) => (
        <div key={child.id ?? child.name}>{render ? render(child) : null}</div>
      ))}
    </div>
  )
}
