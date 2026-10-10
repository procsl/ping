import { Wrench } from "lucide-react"

export interface PlaceholderProps {
  /** 所属菜单项名称 */
  name?: string
  /** 节点标识，name 缺省时兜底 */
  id?: string
}

/**
 * 菜单项占位页：菜单已可达但页面尚未建设时展示。
 * 与框架默认工作台（无内容菜单项落到的 Dashboard）区分，
 * 保证切换不同菜单项时内容区有可辨识的差异。
 */
export function Placeholder({ name, id }: PlaceholderProps): React.ReactNode {
  return (
    <div className="flex min-h-64 flex-col items-center justify-center gap-3 rounded-lg border border-dashed bg-muted/30">
      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-muted">
        <Wrench className="h-5 w-5 text-muted-foreground" />
      </div>
      <p className="text-sm font-medium">{name ?? id ?? "页面"}</p>
      <p className="text-xs text-muted-foreground">页面建设中</p>
    </div>
  )
}
