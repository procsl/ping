import { Link } from "react-router"
import { Button } from "@/components/ui/button"

export function NotFound() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-4">
      <p className="text-4xl font-semibold text-muted-foreground">404</p>
      <p className="text-sm text-muted-foreground">页面不存在或未声明路由</p>
      <Button variant="outline" asChild>
        <Link to="/">返回工作台</Link>
      </Button>
    </div>
  )
}
