import type { ReactNode } from "react"
import type { ComponentNode } from "@/schema/types"
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb"

export interface BreadcrumbBarProps {
  node: ComponentNode
}

/**
 * breadcrumb 组件类型：导航项由 items 属性声明，末项高亮、其余可点击；
 * items 缺失或为空时不渲染（规范手册 §5.2）。
 */
export function BreadcrumbBar({ node }: BreadcrumbBarProps): ReactNode {
  const items = Array.isArray(node.items) ? node.items : []
  if (items.length === 0) {
    console.warn(`[ping-ui] breadcrumb 缺少 items，已跳过：${node.id ?? "未命名"}`)
    return null
  }

  return (
    <Breadcrumb>
      <BreadcrumbList>
        {items.map((item, index) => {
          const isLast = index === items.length - 1
          return (
            <span key={`${item.name}-${index}`} className="flex items-center gap-1.5">
              <BreadcrumbItem>
                {isLast || !item.router ? (
                  <BreadcrumbPage>{item.name}</BreadcrumbPage>
                ) : (
                  <BreadcrumbLink href={`/${item.router.replace(/^\/+/, "")}`}>
                    {item.name}
                  </BreadcrumbLink>
                )}
              </BreadcrumbItem>
              {!isLast && <BreadcrumbSeparator />}
            </span>
          )
        })}
      </BreadcrumbList>
    </Breadcrumb>
  )
}
