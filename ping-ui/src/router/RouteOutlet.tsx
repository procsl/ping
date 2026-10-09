import { Outlet } from "react-router"

/** main_container 内置组件：路由出口，页面内容在此渲染 */
export function RouteOutlet(): React.ReactNode {
  return <Outlet />
}
