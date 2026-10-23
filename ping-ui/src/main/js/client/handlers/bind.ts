/**
 * 架子环④：参数绑定（调用链，design D5/D11，纯函数）。
 *
 * params 按内联 parameters[].in 自动分拣：
 * - path：替换 URL 模板段（缺失即 ApiError 拒绝，不发请求）
 * - query：平铺序列化，数组按重复键展开，undefined/null 跳过
 * - body：operation 声明 requestBody 且调用提供 params.body 时 JSON 序列化（Content-Type: application/json）
 * - header/cookie 等其他 in 的声明参数：不处理（传输策略归 Handler，D11）
 * - 未声明参数忽略（前向兼容，D6）
 */

import { ApiError } from "../error"
import type { Handler } from "../pipeline"
import { requireOperation, type ApiRequest, type Context, type OperationDescriptor } from "../types"

/** 组装 bind 环 */
export function bind(): Handler {
  return async (ctx, next) => {
    ctx.request = buildRequest(ctx, requireOperation(ctx))
    return next()
  }
}

/** 纯函数：内联声明 + params → ApiRequest（可独立单测） */
export function buildRequest(ctx: Context, operation: OperationDescriptor): ApiRequest {
  const params = ctx.params ?? {}
  const operationId = operation.operationId
  let url = operation.path
  const query: Array<[string, string]> = []

  for (const parameter of operation.parameters ?? []) {
    const value = params[parameter.name]
    if (parameter.in === "path") {
      if (value === undefined || value === null) {
        throw new ApiError({
          status: 0,
          operationId,
          message: `缺少 path 参数: ${parameter.name}（${operation.method} ${operation.path}）`,
        })
      }
      url = url.replace(`{${parameter.name}}`, encodeURIComponent(String(value)))
    } else if (parameter.in === "query") {
      if (value === undefined || value === null) continue
      if (Array.isArray(value)) {
        for (const item of value) query.push([parameter.name, String(item)])
      } else {
        query.push([parameter.name, String(value)])
      }
    }
  }

  if (query.length > 0) {
    const search = query
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      .join("&")
    url += `?${search}`
  }

  const headers: Record<string, string> = {}
  let body: string | undefined
  if (operation.requestBody !== undefined && params.body !== undefined && params.body !== null) {
    body = JSON.stringify(params.body)
    headers["Content-Type"] = "application/json"
  }

  return { method: operation.method, url, headers, body }
}
