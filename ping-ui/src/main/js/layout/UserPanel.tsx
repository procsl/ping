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
import { mockUser } from "@/mock"

/** 顶栏用户面板（内置 type: user_info_panel），数据来自 mock，与后端认证解耦 */
export function UserPanel(): React.ReactNode {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="h-8 gap-2 px-2">
          <Avatar className="h-6 w-6">
            <AvatarFallback>{mockUser.avatarText}</AvatarFallback>
          </Avatar>
          <span className="text-sm">{mockUser.nickname}</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuLabel>
          {mockUser.nickname}（{mockUser.role}）
        </DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuItem>个人中心</DropdownMenuItem>
        <DropdownMenuItem>项目设置</DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem>退出登录</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
