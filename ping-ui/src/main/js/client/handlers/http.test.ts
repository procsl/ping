import { describe, expect, it } from "vitest"
import { ApiError } from "../error"
import { compose } from "../pipeline"
import type { Context } from "../types"
import { http } from "./http"

function run(send: typeof globalThis.fetch, seed?: Partial<Context>): Promise<unknown> {
  const ctx: Context = {
    operationId: "findUsers",
    request: { method: "GET", url: "/v1/system/users", headers: {} },
    ...seed,
  }
  return compose([http(send)])(ctx)
}

describe("http 环（终点，对应 spec 响应/错误场景）", () => {
  it("2xx JSON：return 解析数据 + ctx.response 填元数据", async () => {
    const ctx: Context = {
      operationId: "findUsers",
      request: { method: "GET", url: "/v1/system/users", headers: {} },
    }
    const send = async () =>
      new Response(JSON.stringify({ list: [{ id: 1 }], create_time: "2026-01-01" }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      })

    const data = await compose([http(send)])(ctx)

    expect(data).toEqual({ list: [{ id: 1 }], create_time: "2026-01-01" })
    expect(ctx.response?.status).toBe(200)
  })

  it("空响应体（204）：兑现 undefined，不算解析失败", async () => {
    const send = async () => new Response(null, { status: 204 })
    await expect(run(send)).resolves.toBeUndefined()
  })

  it("零长度 200 响应体：兑现 undefined", async () => {
    const send = async () => new Response("", { status: 200 })
    await expect(run(send)).resolves.toBeUndefined()
  })

  it("非 2xx 带业务消息：ApiError status/message/fieldErrors", async () => {
    const send = async () =>
      new Response(
        JSON.stringify({
          code: "METHOD_ARGUMENT_NOT_VALID_ERROR",
          message: "参数校验失败",
          errors: [{ field: "account", tips: "不可为空" }],
        }),
        { status: 400, statusText: "Bad Request" },
      )

    const err = await run(send).catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).status).toBe(400)
    expect((err as ApiError).message).toBe("参数校验失败")
    expect((err as ApiError).fieldErrors).toEqual([{ field: "account", tips: "不可为空" }])
    expect((err as ApiError).operationId).toBe("findUsers")
  })

  it("非 2xx 非 JSON 响应体：ApiError 回落状态文本", async () => {
    const send = async () =>
      new Response("<html>502</html>", { status: 502, statusText: "Bad Gateway" })

    const err = await run(send).catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).message).toBe("Bad Gateway")
  })

  it("网络失败：ApiError status=0 且 cause 保留底层原因", async () => {
    const cause = new TypeError("Failed to fetch")
    const send = async () => {
      throw cause
    }

    const err = await run(send).catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).status).toBe(0)
    expect((err as ApiError).cause).toBe(cause)
  })

  it("AbortError 原样透出不包装", async () => {
    const abort = new DOMException("The operation was aborted", "AbortError")
    const send = async () => {
      throw abort
    }

    const err = await run(send).catch((e: unknown) => e)

    expect(err).toBe(abort)
    expect(err).not.toBeInstanceOf(ApiError)
  })

  it("请求参数透传：method/url/headers/signal 送入 fetch", async () => {
    let captured: RequestInit | undefined
    const send = async (_url: string | URL | Request, init?: RequestInit) => {
      captured = init
      return new Response("{}", { status: 200 })
    }
    const controller = new AbortController()
    const ctx: Context = {
      operationId: "saveUser",
      request: {
        method: "POST",
        url: "/v1/system/users",
        headers: { "Content-Type": "application/json" },
        body: "{}",
      },
      signal: controller.signal,
    }

    await compose([http(send)])(ctx)

    expect(captured?.method).toBe("POST")
    expect(captured?.body).toBe("{}")
    expect(captured?.signal).toBe(controller.signal)
  })
})
