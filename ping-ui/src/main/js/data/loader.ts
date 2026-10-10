import type {
  ComponentManifest,
  ComponentNode,
  ManifestSource,
} from "@/schema/types"
import { defaultTree } from "@/data/default-tree"

export interface LoadedManifest {
  tree: ComponentNode
  source: ManifestSource
  /** 接口下发的类型定义；降级本地时为空数组 */
  types: unknown[]
}

function isComponentTree(value: unknown): value is ComponentNode {
  return (
    typeof value === "object" &&
    value !== null &&
    typeof (value as ComponentNode).type === "string"
  )
}

/**
 * 从 GET /v1/ui/components 的响应中挑出要渲染的应用树。
 *
 * 正常形态是 `{types, pages}`，`pages[0]` 优先为 `application` 外壳树；
 * 同时保留裸树（直接是 `ComponentNode`）与旧数组形态的兼容分支。
 */
function pickTree(body: unknown): ComponentNode | undefined {
  if (isComponentTree(body)) {
    return body
  }

  if (Array.isArray(body)) {
    const candidates = body.filter(isComponentTree)
    return candidates.find((node) => node.type === "application") ?? candidates[0]
  }

  if (typeof body === "object" && body !== null) {
    const pages = (body as ComponentManifest).pages
    if (Array.isArray(pages)) {
      const candidates = pages.filter(isComponentTree)
      return candidates.find((node) => node.type === "application") ?? candidates[0]
    }
  }

  return undefined
}

/**
 * 加载组件树：manifest 指向后端接口时优先请求，接口不可达、非 200、
 * 或响应不符合描述结构时静默降级到本地声明，确保渲染器单文件即可运行。
 */
export async function loadManifest(manifest?: string): Promise<LoadedManifest> {
  if (manifest) {
    try {
      const res = await fetch(manifest, {
        headers: { Accept: "application/json" },
      })
      if (res.ok) {
        const body: unknown = await res.json()
        const tree = pickTree(body)
        if (tree) {
          const types =
            typeof body === "object" &&
            body !== null &&
            Array.isArray((body as ComponentManifest).types)
              ? (body as ComponentManifest).types
              : []
          return { tree, source: "remote", types }
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
  return { tree: defaultTree, source: "local", types: [] }
}
