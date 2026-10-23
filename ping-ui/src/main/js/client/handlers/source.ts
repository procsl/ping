/**
 * 架子环①：取树（bootstrap 链首环，design D1）。
 *
 * 默认树源 fetch GET /v1/ui/components；treeSource 注入（测试/离线）优先。
 * 默认树源的 HTTP 层跟随 ComposeClientOptions.fetch 注入（mock HTTP 时树源同被 mock）。
 * 失败语义：非 2xx → ApiError（携带响应体）；网络失败 → ApiError{status:0,cause}；
 * 注入树源自身失败 → 原因原样透传（不包装）。
 */

import { ApiError } from "../error"
import type { Handler } from "../pipeline"
import type { TreeSource } from "../types"

export const DEFAULT_TREE_PATH = "/v1/ui/components"

/** 组装 source 环：treeSource 注入优先，send 为默认树源的 HTTP 发送层 */
export function source(treeSource?: TreeSource, send?: typeof globalThis.fetch): Handler {
  return async (ctx, next) => {
    ctx.raw = treeSource !== undefined ? await treeSource() : await fetchTree(send)
    return next()
  }
}

async function fetchTree(send?: typeof globalThis.fetch): Promise<unknown> {
  const fetcher = send ?? globalThis.fetch
  let res: Response
  try {
    res = await fetcher(DEFAULT_TREE_PATH)
  } catch (cause) {
    throw new ApiError({
      status: 0,
      operationId: "",
      statusText: "Network Error",
      cause,
    })
  }
  if (!res.ok) {
    const body = await res.json().catch(() => undefined)
    throw new ApiError({
      status: res.status,
      operationId: "",
      statusText: res.statusText,
      body,
    })
  }
  try {
    return await res.json()
  } catch (cause) {
    throw new ApiError({
      status: res.status,
      operationId: "",
      message: "树源响应不是合法 JSON",
      cause,
    })
  }
}
