import { describe, expect, it, vi } from "vitest"
import { ApiError } from "./error"
import { ComposeClient } from "./client"
import type { Context } from "./types"

const tree = {
  type: "app",
  id: "ping.app",
  children: [
    {
      type: "page",
      id: "user",
      children: [
        {
          type: "function",
          id: "f1",
          properties: {
            operationId: "findUsers",
            method: "GET",
            path: "/v1/system/users",
            parameters: [{ name: "name", in: "query" }],
          },
        },
        {
          type: "function",
          id: "f2",
          properties: {
            operationId: "saveUser",
            method: "POST",
            path: "/v1/system/users",
            requestBody: {},
          },
        },
      ],
    },
  ],
}

/** mock HTTP 层：每次调用新建独立 mock（记录不跨测试） */
function mockSend() {
  return vi.fn(async (_url: unknown, _init?: RequestInit) =>
    new Response(JSON.stringify({ list: [] }), { status: 200 }),
  )
}

const okSend = async () => new Response(JSON.stringify({ list: [] }), { status: 200 })

describe("ComposeClient", () => {
  it("注入树源创建：零网络，fetch 兑现链冒泡值", async () => {
    const send = mockSend()
    const client = await ComposeClient.create({
      treeSource: () => Promise.resolve(tree),
      fetch: send,
    })

    const data = await client.fetch<{ list: unknown[] }>("findUsers", {
      params: { name: "x" },
    })

    expect(data).toEqual({ list: [] })
    expect(send).toHaveBeenCalledOnce()
    expect(send.mock.calls[0][0]).toBe("/v1/system/users?name=x")
  })

  it("创建失败即 reject：错误携带失败原因，无半初始化客户端", async () => {
    const boom = new Error("fixture missing")

    await expect(
      ComposeClient.create({ treeSource: () => Promise.reject(boom) }),
    ).rejects.toBe(boom)
  })

  it("业务环插槽位置：locate 之后 bind 之后 http 之前（operation 与 request 均可读）", async () => {
    const seen: Context = {}
    const client = await ComposeClient.create({
      treeSource: () => Promise.resolve(tree),
      fetch: okSend,
    })
    client.addHandler(async (ctx, next) => {
      Object.assign(seen, {
        operationId: ctx.operationId,
        operation: ctx.operation,
        request: ctx.request,
      })
      return next()
    })

    await client.fetch("findUsers")

    expect(seen.operation?.operationId).toBe("findUsers")
    expect(seen.request?.url).toBe("/v1/system/users")
  })

  it("注册顺序 = 执行顺序：越晚注册越贴近 HTTP（下行 A→B，上行 B→A）", async () => {
    const order: string[] = []
    const client = await ComposeClient.create({
      treeSource: () => Promise.resolve(tree),
      fetch: async () => {
        order.push("http")
        return new Response("{}", { status: 200 })
      },
    })
    client.addHandler(async (_ctx, next) => {
      order.push("A-down")
      const r = await next()
      order.push("A-up")
      return r
    })
    client.addHandler(async (_ctx, next) => {
      order.push("B-down")
      const r = await next()
      order.push("B-up")
      return r
    })

    await client.fetch("findUsers")

    expect(order).toEqual(["A-down", "B-down", "http", "B-up", "A-up"])
  })

  it("请求改写：业务环向 ctx.request.headers 注入字段，最终请求携带", async () => {
    const send = mockSend()
    const client = await ComposeClient.create({
      treeSource: () => Promise.resolve(tree),
      fetch: send,
    })
    client.addHandler(async (ctx, next) => {
      ctx.request!.headers["X-Token"] = "t-123"
      return next()
    })

    await client.fetch("findUsers")

    const init = send.mock.calls[0][1]
    expect((init?.headers as Record<string, string>)["X-Token"]).toBe("t-123")
  })

  it("短路：不调 next 不发 HTTP，短路值即兑现值", async () => {
    const send = mockSend()
    const client = await ComposeClient.create({
      treeSource: () => Promise.resolve(tree),
      fetch: send,
    })
    client.addHandler(async () => ({ from: "cache" }))

    const data = await client.fetch("findUsers")

    expect(data).toEqual({ from: "cache" })
    expect(send).not.toHaveBeenCalled()
  })

  it("tree 只读：与索引同源，修改不影响内部状态", async () => {
    const client = await ComposeClient.create({
      treeSource: () => Promise.resolve(tree),
      fetch: okSend,
    })

    expect(client.tree?.id).toBe("ping.app")
    expect(Object.isFrozen(client.tree)).toBe(true)
    expect(Object.isFrozen(client.tree?.children?.[0])).toBe(true)
  })

  it("未知 operationId 调用：ApiError 拒绝且消息含键名", async () => {
    const client = await ComposeClient.create({
      treeSource: () => Promise.resolve(tree),
      fetch: okSend,
    })

    const err = await client.fetch("nope").catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).message).toContain("nope")
  })
})
