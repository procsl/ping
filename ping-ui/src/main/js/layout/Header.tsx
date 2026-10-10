import { PanelLeft } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Breadcrumb } from "@/components/renderer/breadcrumb"
import { ThemeToggle } from "@/layout/ThemeToggle"
import { UserPanel } from "@/layout/UserPanel"

export interface HeaderProps {
  activePath: string
  collapsed: boolean
  onToggleCollapsed: () => void
}

/** 顶栏：侧栏折叠开关、面包屑、主题切换、用户面板 */
export function Header({
  activePath,
  collapsed,
  onToggleCollapsed,
}: HeaderProps): React.ReactNode {
  return (
    <header className="flex h-12 shrink-0 items-center justify-between gap-4 border-b px-3">
      <div className="flex min-w-0 items-center gap-2">
        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8"
          onClick={onToggleCollapsed}
          title={collapsed ? "展开侧栏" : "折叠侧栏"}
        >
          <PanelLeft className="h-4 w-4" />
        </Button>
        <div className="min-w-0">
          <Breadcrumb activePath={activePath} />
        </div>
      </div>
      <div className="flex shrink-0 items-center gap-1">
        <ThemeToggle />
        <UserPanel />
      </div>
    </header>
  )
}
