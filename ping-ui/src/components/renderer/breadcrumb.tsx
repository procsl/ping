import { Link } from "react-router"
import {
  Breadcrumb as BreadcrumbRoot,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb"

const TITLES: Record<string, string> = {
  home: "首页",
  system: "系统管理",
  monitor: "监控中心",
}

/** 面包屑：按 /xxx/xxx 两段式路径逐级展开 */
export function Breadcrumb({ activePath }: { activePath: string }) {
  const segments = activePath.split("/").filter(Boolean)
  if (segments.length === 0) {
    return null
  }

  return (
    <BreadcrumbRoot>
      <BreadcrumbList>
        {segments.map((segment, index) => {
          const isLast = index === segments.length - 1
          const to = `/${segments.slice(0, index + 1).join("/")}`
          const label =
            index === 0 ? (TITLES[segment] ?? segment) : segment
          return (
            <div key={to} className="contents">
              {index > 0 && <BreadcrumbSeparator />}
              <BreadcrumbItem>
                {isLast ? (
                  <BreadcrumbPage>{label}</BreadcrumbPage>
                ) : (
                  <BreadcrumbLink asChild>
                    <Link to={to}>{label}</Link>
                  </BreadcrumbLink>
                )}
              </BreadcrumbItem>
            </div>
          )
        })}
      </BreadcrumbList>
    </BreadcrumbRoot>
  )
}
