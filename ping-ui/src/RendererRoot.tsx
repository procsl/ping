import { useEffect, useMemo, useState } from "react"
import { createBrowserRouter, Navigate, RouterProvider } from "react-router"
import type { MountOptions, ManifestSource, ComponentNode } from "@/schema/types"
import { loadManifest } from "@/data/loader"
import { deriveRoutes } from "@/router/derive"
import { AdminLayout } from "@/layout/AdminLayout"
import { RoutePage, NotFound } from "@/router/RoutePage"
import { TooltipProvider } from "@/components/ui/tooltip"
import { Skeleton } from "@/components/ui/skeleton"

interface RendererState {
  tree: ComponentNode
  source: ManifestSource
}

export function RendererRoot({ options }: { options: MountOptions }) {
  const [state, setState] = useState<RendererState | null>(null)

  useEffect(() => {
    let active = true
    void loadManifest(options.manifest).then((loaded) => {
      if (active) {
        setState({ tree: loaded.tree, source: loaded.source })
      }
    })
    return () => {
      active = false
    }
  }, [options.manifest])

  const router = useMemo(() => {
    if (!state) {
      return null
    }
    const routes = deriveRoutes(state.tree)
    const first = routes[0]?.path
    const namespace = options.namespace ?? "app"

    return createBrowserRouter([
      {
        path: "/",
        element: (
          <AdminLayout
            tree={state.tree}
            namespace={namespace}
            title={state.tree.name ?? "Ping Admin"}
          />
        ),
        children: [
          ...(first
            ? [{ index: true, element: <Navigate to={first} replace /> }]
            : []),
          ...routes.map((route) => ({
            path: route.path.replace(/^\/+/, ""),
            element: (
              <RoutePage
                node={route.node}
                namespace={namespace}
                activePath={route.path}
              />
            ),
          })),
          { path: "*", element: <NotFound /> },
        ],
      },
    ])
  }, [state, options.namespace])

  if (!state || !router) {
    return (
      <div className="flex h-screen flex-col gap-3 p-6">
        <Skeleton className="h-12 w-full" />
        <div className="flex flex-1 gap-3">
          <Skeleton className="h-full w-56" />
          <Skeleton className="h-full flex-1" />
        </div>
      </div>
    )
  }

  return (
    <TooltipProvider delayDuration={300}>
      <RouterProvider router={router} />
    </TooltipProvider>
  )
}
