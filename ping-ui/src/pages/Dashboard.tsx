import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

const STATS = [
  { title: "今日访问", value: "1,280" },
  { title: "接口调用", value: "8,642" },
  { title: "在线用户", value: "36" },
  { title: "异常告警", value: "0" },
]

/** 框架默认工作台：菜单节点未声明页面内容时展示 */
export function Dashboard() {
  return (
    <div className="space-y-4">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {STATS.map((stat) => (
          <Card key={stat.title}>
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.title}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-2xl font-semibold">
              {stat.value}
            </CardContent>
          </Card>
        ))}
      </div>
      <Card>
        <CardHeader>
          <CardTitle>Ping Admin</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          抽象组件渲染器已就绪：菜单与路由由后端组件树推导，本页为框架默认工作台。
        </CardContent>
      </Card>
    </div>
  )
}
