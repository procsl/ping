import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { mockStats } from "@/mock"

/** 框架默认工作台：菜单节点未声明页面内容时展示，统计数字为 mock 数据 */
export function Dashboard() {
  return (
    <div className="space-y-4">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {mockStats.map((stat) => (
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
          后台管理框架已就绪：侧栏折叠、多标签导航、明暗主题、面包屑与用户面板。
          菜单与路由由本地组件树声明推导，数据为模拟数据，与后端接口暂时解耦。
        </CardContent>
      </Card>
    </div>
  )
}
