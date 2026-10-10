import type { ReactNode } from "react"
import { Inbox } from "lucide-react"
import type { ComponentNode } from "@/schema/types"
import { Button } from "@/components/ui/button"

export interface EmptyStateProps {
  node: ComponentNode
}

/** 空态：图标 + 主文案 + 次文案 + 可选引导按钮（文案取自 action 属性） */
export function EmptyState({ node }: EmptyStateProps): ReactNode {
  const action = typeof node.action === "string" ? node.action : ""
  return (
    <div className="flex flex-col items-center justify-center gap-2 rounded-md border border-dashed py-10 text-center">
      <Inbox className="h-8 w-8 text-muted-foreground" aria-hidden />
      <p className="text-sm font-medium">{node.name ?? "暂无数据"}</p>
      {node.description && (
        <p className="text-xs text-muted-foreground">{node.description}</p>
      )}
      {action && (
        <Button type="button" variant="outline" size="sm">
          {action}
        </Button>
      )}
    </div>
  )
}
