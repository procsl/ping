import { describe, expect, it } from "vitest"
import { ApiError } from "../error"
import { compose } from "../pipeline"
import type { Context } from "../types"
import { source } from "./source"

const tree = { type: "app", id: "ping.app", children: [] }

async function runWith(handler: ReturnType<typeof source>): Promise<Context> {
  const ctx: Context = {}
  await compose([handler, async () => undefined])(ctx)
  return ctx
}

describe("source 环（取树）", () => {
  it("注入树源：结果原样进 ctx.raw，零网络", async () => {
    const ctx = await runWith(source(() => Promise.resolve(tree)))
    expect(ctx.raw).toBe(tree)
  })

  it("默认树源：fetch 200 JSON 进 ctx.raw", async () => {
    const send = async () => new Response(JSON.stringify(tree), { status: 200 })
    const ctx = await runWith(source(undefined, send))
    expect(ctx.raw).toEqual(tree)
  })

  it("默认树源非 2xx：ApiError 携带 status 与响应体 message", async () => {
    const send = async () =>
      new Response(JSON.stringify({ message: "未登录" }), {
        status: 401,
        statusText: "Unauthorized",
      })

    const err = await runWith(source(undefined, send)).catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).status).toBe(401)
    expect((err as ApiError).message).toBe("未登录")
  })

  it("默认树源网络失败：ApiError status=0 且 cause 保留底层原因", async () => {
    const cause = new TypeError("Failed to fetch")
    const send = async () => {
      throw cause
    }

    const err = await runWith(source(undefined, send)).catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).status).toBe(0)
    expect((err as ApiError).cause).toBe(cause)
  })

  it("默认树源响应非法 JSON：ApiError 明确报解析失败", async () => {
    const send = async () => new Response("<html>", { status: 200 })

    const err = await runWith(source(undefined, send)).catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).message).toBe("树源响应不是合法 JSON")
  })

  it("注入树源自身失败：原因原样透传不包装", async () => {
    const boom = new Error("fixture missing")
    const failing = () => Promise.reject(boom)

    const err = await runWith(source(failing)).catch((e: unknown) => e)

    expect(err).toBe(boom)
  })
})
