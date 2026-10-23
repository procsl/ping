/**
 * 架子环③：查表定位（调用链首环，design D2）。
 *
 * ctx.operationId → 索引查表 → ctx.operation；未知键 throw ApiError（消息含键名）。
 */

import { ApiError } from "../error"
import type { Handler } from "../pipeline"
import type { OperationIndex } from "../types"

/** 组装 locate 环：绑定 bootstrap 链产出的 operationId 索引 */
export function locate(index: OperationIndex): Handler {
  return async (ctx, next) => {
    const operationId = ctx.operationId
    if (operationId === undefined || operationId === "") {
      throw new Error("ctx.operationId 不存在：locate 环必须由 client.fetch 组链执行")
    }
    const operation = index.get(operationId)
    if (operation === undefined) {
      throw new ApiError({
        status: 0,
        operationId,
        message: `未知的 operationId: ${operationId}`,
      })
    }
    ctx.operation = operation
    return next()
  }
}
