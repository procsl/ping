/**
 * 框架层：Koa 式洋葱管道（design D3/D7，参照 koa-compose）。
 *
 * 框架认识的全部：一串 Handler、一个袋子（Context）、洋葱语义。
 * 本文件不引用任何业务字段（operation/request 等词不出现）。
 *
 * 语义：
 * - 数组顺序 = 执行顺序（chain[0] 最外层）
 * - 不调 next() 即短路
 * - return 值与异常沿 await next() 逐层冒泡（数据走 return，错误走 throw）
 * - next() 重复调用 throw
 * - 链尽头无人 return 兑现 undefined
 */

import type { Context } from "./types"

/** 下一环入口：await next() 下探，其返回值即下一环的结果 */
export type Next = () => Promise<unknown>

/** 业务环/架子环的统一形态：函数即 Handler，无接口无泛型 */
export type Handler = (ctx: Context, next: Next) => Promise<unknown>

/** 把链串成洋葱，返回对某袋子执行整条链的 run 函数 */
export function compose(chain: Handler[]): (ctx: Context) => Promise<unknown> {
  if (!Array.isArray(chain)) {
    throw new TypeError("compose 需要数组")
  }
  for (const fn of chain) {
    if (typeof fn !== "function") {
      throw new TypeError("compose 的每一环必须是函数")
    }
  }

  return function run(ctx: Context): Promise<unknown> {
    let index = -1

    function dispatch(i: number): Promise<unknown> {
      if (i <= index) {
        return Promise.reject(new Error("next() 被重复调用"))
      }
      index = i
      const fn = chain[i]
      if (fn === undefined) {
        // 链尽头：无人 return 则兑现 undefined
        return Promise.resolve(undefined)
      }
      try {
        return Promise.resolve(fn(ctx, () => dispatch(i + 1)))
      } catch (err) {
        return Promise.reject(err)
      }
    }

    return dispatch(0)
  }
}
