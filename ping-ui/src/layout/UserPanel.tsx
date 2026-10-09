import type { ComponentNode } from "@/schema/types"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

/** 顶栏用户信息面板（内置 type: user_info_panel），接口未就绪时展示占位身份 */
export function UserPanel({ node }: { node: ComponentNode }): React.ReactNode {
  const api = node.apis?.[0]?.api
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="h-8 gap-2 px-2">
          <Avatar className="h-6 w-6">
            <AvatarFallback>P</AvatarFallback>
          </Avatar>
          <span className="text-sm">admin</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuLabel>{api ?? "当前用户"}</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem>个人中心</DropdownMenuItem>
        <DropdownMenuItem>退出登录</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
