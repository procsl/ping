/**
 * fixture 契约验证（tasks 4.2）：真实编译产物 fixture 全链路。
 *
 * 纯 Node 单元测试（无网络、不起服务），仅文件名中的 fixtures 表明数据来源特殊。
 *
 * fixture 来自 GET /v1/ui/components 真实导出（ping-system 编译产物），
 * 后端结构演进时该 fixture 先红（fixture 纪律，design Test Requirements）。
 */

import { describe, expect, it, vi } from "vitest"
import { ComposeClient } from "./client"
import { ApiError } from "./error"
import tree from "./__fixtures__/ui-components.json"

function mockSend(handler?: (url: string, init?: RequestInit) => Response) {
  return vi.fn(async (url: unknown, init?: RequestInit) => {
    const send = handler ?? (() => new Response(JSON.stringify({ list: [] }), { status: 200 }))
    return send(String(url), init)
  })
}

function createClient(send: typeof globalThis.fetch) {
  return ComposeClient.create({
    treeSource: () => Promise.resolve(tree),
    fetch: send,
  })
}

describe("ComposeClient 集成语义（真实 fixture）", () => {
  it("全链路：findUsers 定位/绑定/发送/兑现纯数据", async () => {
    const send = mockSend(() =>
      new Response(JSON.stringify({ records: [{ id: 1, create_time: "2026-01-01" }], total: 1 }), {
        status: 200,
      }),
    )
    const client = await createClient(send)

    const data = await client.fetch<{ total: number }>("findUsers", {
      params: { name: "x", account: "a", offset: 1, size: 20 },
    })

    expect(data.total).toBe(1)
    expect(send.mock.calls[0][0]).toBe(
      "/v1/system/users?name=x&account=a&offset=1&size=20",
    )
  })

  it("400 带业务消息：ApiError 携带 message 与 fieldErrors", async () => {
    const send = mockSend(() =>
      new Response(
        JSON.stringify({
          code: "METHOD_ARGUMENT_NOT_VALID_ERROR",
          message: "参数校验失败",
          errors: [{ field: "nickname", tips: "不可为空" }],
        }),
        { status: 400, statusText: "Bad Request" },
      ),
    )
    const client = await createClient(send)

    const err = await client
      .fetch("accountStatusSetting", { params: { id: "1", body: { state: "bad" } } })
      .catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).status).toBe(400)
    expect((err as ApiError).message).toBe("参数校验失败")
    expect((err as ApiError).fieldErrors).toEqual([{ field: "nickname", tips: "不可为空" }])
    // path 参数已绑定进 URL
    expect(send.mock.calls[0][0]).toBe("/v1/system/users/1/account")
  })

  it("401 未注册鉴权环：与普通失败无差别（status=401 的 ApiError）", async () => {
    const send = mockSend(() =>
      new Response(JSON.stringify({ message: "未登录" }), {
        status: 401,
        statusText: "Unauthorized",
      }),
    )
    const client = await createClient(send)

    const err = await client.fetch("findUsers").catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).status).toBe(401)
    expect((err as ApiError).message).toBe("未登录")
  })

  it("snake_case 字段名原文兑现（核心零转换）", async () => {
    const send = mockSend(() =>
      new Response(JSON.stringify({ create_time: "2026-01-01", user_name: "u" }), {
        status: 200,
      }),
    )
    const client = await createClient(send)

    const data = await client.fetch<Record<string, string>>("findUsers")

    expect(data).toEqual({ create_time: "2026-01-01", user_name: "u" })
  })

  it("网络失败：ApiError status=0", async () => {
    const send = vi.fn(async () => {
      throw new TypeError("Failed to fetch")
    })
    const client = await createClient(send)

    const err = await client.fetch("findUsers").catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).status).toBe(0)
  })

  it("AbortSignal 取消：AbortError 原样透出，与网络失败可区分", async () => {
    const controller = new AbortController()
    const send = vi.fn(async (_url: unknown, init?: RequestInit) => {
      if (init?.signal?.aborted) {
        throw new DOMException("The operation was aborted", "AbortError")
      }
      return new Response("{}", { status: 200 })
    })
    const client = await createClient(send)
    controller.abort()

    const err = await client
      .fetch("findUsers", { signal: controller.signal })
      .catch((e: unknown) => e)

    expect(err).toBeInstanceOf(DOMException)
    expect((err as { name: string }).name).toBe("AbortError")
    expect(err).not.toBeInstanceOf(ApiError)
  })

  it("业务环请求改写抵达最终请求；短路不发 HTTP（真实 fixture）", async () => {
    const send = mockSend()
    const client = await createClient(send)
    client.addHandler(async (ctx, next) => {
      ctx.request!.headers["X-Token"] = "t-1"
      return next()
    })
    client.addHandler(async (ctx, next) => {
      if (ctx.operationId === "accountStatusSetting") return { from: "cache" }
      return next()
    })

    await client.fetch("findUsers")
    await client.fetch("accountStatusSetting", { params: { id: "9" } })

    expect(send.mock.calls[0][1]?.headers).toMatchObject({ "X-Token": "t-1" })
    expect(send).toHaveBeenCalledTimes(1)   // 第二次短路，未发 HTTP
  })
})
