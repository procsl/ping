import type { ReactNode } from "react"
import type { ComponentNode } from "@/schema/types"
import { resolvedApi } from "@/schema/types"
import { Button } from "@/components/ui/button"

export interface ActionButtonProps {
  node: ComponentNode
}

/**
 * 动作按钮：action（容器级）与 row_action（行内）共用。
 * 缺 api 时按钮仍渲染，点击仅告警（见规范手册 §5.5）；
 * 声明 confirm 时必须先确认再执行。
 */
export function ActionButton({ node }: ActionButtonProps): ReactNode {
  const api = resolvedApi(node)
  const label = node.name ?? node.id ?? "操作"

  const handleClick = () => {
    if (!api) {
      console.warn(
        `[ping-ui] 动作未绑定接口（或接口引用未解析）：${node.id ?? label}`,
      )
      return
    }
    if (node.confirm && !window.confirm(node.confirm)) {
      return
    }
    console.info(
      `[ping-ui] 触发动作 ${label} → ${api.method} ${api.path}（数据接入见 design Non-Goals）`,
    )
  }

  return (
    <Button
      type="button"
      size="sm"
      variant={node.variant === "primary" ? "default" : node.danger ? "destructive" : "outline"}
      onClick={handleClick}
      title={api ? `${api.method} ${api.path}` : "未绑定接口"}
    >
      {label}
    </Button>
  )
}
