import type { ComponentNode, ManifestSource } from "@/schema/types"
import { defaultTree } from "@/data/default-tree"

export interface LoadedManifest {
  tree: ComponentNode
  source: ManifestSource
}

function isComponentTree(value: unknown): value is ComponentNode {
  return (
    typeof value === "object" &&
    value !== null &&
    typeof (value as ComponentNode).type === "string"
  )
}

/**
 * 加载组件树：manifest 指向后端接口时优先请求（预留 GET /v1/ui/menus），
 * 接口缺失或失败时静默降级到本地声明，确保渲染器单文件即可运行。
 */
export async function loadManifest(manifest?: string): Promise<LoadedManifest> {
  if (manifest) {
    try {
      const res = await fetch(manifest, {
        headers: { Accept: "application/json" },
      })
      if (res.ok) {
        const body: unknown = await res.json()
        const tree = Array.isArray(body)
          ? (body[0] as ComponentNode | undefined)
          : body
        if (isComponentTree(tree)) {
          return { tree, source: "remote" }
        }
        console.warn("[ping-ui] 组件树接口响应不符合 schema，降级本地声明")
      } else {
        console.warn(
          `[ping-ui] 组件树接口返回 ${res.status}，降级本地声明`,
        )
      }
    } catch (e) {
      console.warn("[ping-ui] 组件树接口不可用，降级本地声明", e)
    }
  }
  return { tree: defaultTree, source: "local" }
}
