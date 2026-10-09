import { useEffect, useMemo, useState } from "react"
import { useLocation, useNavigate } from "react-router"
import { X } from "lucide-react"
import { cn } from "@/lib/utils"
import type { DerivedRoute } from "@/router/derive"

export interface TagsViewProps {
  routes: DerivedRoute[]
}

/**
 * 多标签导航：记录访问过的路由，首个标签（工作台）固定不可关闭，
 * 关闭激活标签时回退到前一个。
 */
export function TagsView({ routes }: TagsViewProps): React.ReactNode {
  const location = useLocation()
  const navigate = useNavigate()

  const nameByPath = useMemo(
    () => new Map(routes.map((route) => [route.path, route.name])),
    [routes],
  )
  const homePath = routes[0]?.path

  const [tags, setTags] = useState<string[]>(() =>
    homePath ? [homePath] : [],
  )

  useEffect(() => {
    const current = location.pathname
    if (nameByPath.has(current)) {
      setTags((prev) => (prev.includes(current) ? prev : [...prev, current]))
    }
  }, [location.pathname, nameByPath])

  if (tags.length === 0) {
    return null
  }

  const closeTag = (path: string) => {
    if (path === homePath) {
      return
    }
    const index = tags.indexOf(path)
    const next = tags.filter((tag) => tag !== path)
    setTags(next)
    if (path === location.pathname) {
      navigate(next[Math.max(0, index - 1)] ?? "/")
    }
  }

  return (
    <div className="flex h-9 shrink-0 items-center gap-1 border-b bg-muted/40 px-2">
      {tags.map((path) => {
        const active = path === location.pathname
        const closable = path !== homePath
        return (
          <div
            key={path}
            className={cn(
              "group flex h-7 items-center gap-1 rounded-md px-2.5 text-xs",
              active
                ? "bg-background font-medium text-foreground shadow-sm ring-1 ring-border"
                : "text-muted-foreground hover:bg-background/60",
            )}
          >
            <button
              type="button"
              className="max-w-32 truncate"
              onClick={() => navigate(path)}
            >
              {nameByPath.get(path) ?? path}
            </button>
            {closable && (
              <button
                type="button"
                className="rounded p-0.5 opacity-60 hover:bg-muted hover:opacity-100"
                onClick={() => closeTag(path)}
                title="关闭标签"
              >
                <X className="h-3 w-3" />
              </button>
            )}
          </div>
        )
      })}
    </div>
  )
}
