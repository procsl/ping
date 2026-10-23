/**
 * 架子环⑤：HTTP 终点（调用链最内层，design D3/D5）。
 *
 * 不调 next（终点语义）：
 * - 2xx：ctx.response 填 {status, headers} 元数据，return 解析后的数据（数据走 return 冒泡通道）
 * - 空体（204/零长度）：兑现 undefined，不算解析失败
 * - 非 2xx：throw ApiError（message 自响应体提取回落状态文本，fieldErrors 透出）
 * - 网络失败：throw ApiError{status:0, cause}；AbortError 原样透出不包装
 */

import { ApiError } from "../error"
import type { Handler } from "../pipeline"
import { requireRequest } from "../types"

/** 组装 http 环：send 为 HTTP 发送层（默认 globalThis.fetch） */
export function http(send?: typeof globalThis.fetch): Handler {
  return async (ctx) => {
    const request = requireRequest(ctx)
    const fetcher = send ?? globalThis.fetch
    const operationId = ctx.operationId ?? ""

    let res: Response
    try {
      res = await fetcher(request.url, {
        method: request.method,
        headers: request.headers,
        body: request.body,
        signal: ctx.signal,
      })
    } catch (cause) {
      if (isAbortError(cause)) {
        // 取消原样透出（D5）：调用方可与网络失败区分，重试逻辑不误发
        throw cause
      }
      throw new ApiError({
        status: 0,
        operationId,
        statusText: "Network Error",
        cause,
      })
    }

    if (!res.ok) {
      const body = await res.json().catch(() => undefined)
      throw new ApiError({
        status: res.status,
        operationId,
        statusText: res.statusText,
        body,
      })
    }

    ctx.response = {
      status: res.status,
      headers: Object.fromEntries(res.headers.entries()),
    }

    const text = await res.text()
    if (text === "") {
      // 空响应体（204/零长度）兑现 undefined
      return undefined
    }
    try {
      return JSON.parse(text)
    } catch (cause) {
      throw new ApiError({
        status: res.status,
        operationId,
        message: "响应不是合法 JSON",
        cause,
      })
    }
  }
}

function isAbortError(cause: unknown): boolean {
  return (
    typeof cause === "object" &&
    cause !== null &&
    "name" in cause &&
    (cause as { name?: unknown }).name === "AbortError"
  )
}
