import { describe, expect, it } from "vitest"
import { compose } from "../pipeline"
import type { Context } from "../types"
import { indexer } from "./indexer"

function run(raw: unknown): Promise<Context> {
  const ctx: Context = { raw }
  return compose([indexer(), async (c) => c])(ctx).then(() => ctx)
}

describe("indexer 环（嗅探 + 建索引）", () => {
  it("树输入：app > page > function 抽取，索引键完整", async () => {
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
                route: "user/list",
              },
            },
            {
              type: "function",
              id: "f2",
              properties: { operationId: "saveUser", method: "POST", path: "/v1/system/users" },
            },
          ],
        },
      ],
    }

    const ctx = await run(tree)

    expect(ctx.tree).toBe(tree)
    expect([...(ctx.index?.keys() ?? [])]).toEqual(["findUsers", "saveUser"])
    expect(ctx.index?.get("findUsers")).toMatchObject({
      method: "GET",
      path: "/v1/system/users",
      route: "user/list",
    })
  })

  it("树特征优先：同时含 type 与 paths 时按树处理", async () => {
    const ambiguous = {
      type: "app",
      paths: { "/x": {} },
      children: [
        { type: "function", id: "f1", properties: { operationId: "only", method: "GET", path: "/x" } },
      ],
    }

    const ctx = await run(ambiguous)

    expect(ctx.tree).toBe(ambiguous)
    expect([...(ctx.index?.keys() ?? [])]).toEqual(["only"])
  })

  it("裸 openapi 文档输入：paths 抽取索引并补 method/path，不填 tree", async () => {
    const doc = {
      openapi: "3.0.0",
      paths: {
        "/v1/system/users": {
          get: { operationId: "findUsers", responses: { "200": {} } },
          post: { operationId: "saveUser" },
          parameters: [{ name: "global" }],
        },
        "/v1/bad": {
          get: { responses: {} },          // 无 operationId：跳过
          post: { operationId: "" },        // 空 operationId：跳过
        },
      },
    }

    const ctx = await run(doc)

    expect(ctx.tree).toBeUndefined()
    expect(ctx.index?.get("findUsers")).toMatchObject({
      operationId: "findUsers",
      method: "GET",
      path: "/v1/system/users",
    })
    expect(ctx.index?.get("saveUser")).toMatchObject({ method: "POST", path: "/v1/system/users" })
    expect(ctx.index?.size).toBe(2)
  })

  it("非法输入：对象但既非树也非文档 → 明确报错", async () => {
    await expect(run({ foo: 1 })).rejects.toThrow("无法识别的树源数据")
  })

  it("非对象输入（null / 数组 / 字符串）→ 明确报错", async () => {
    await expect(run(null)).rejects.toThrow("必须是 JSON 对象")
    await expect(run([1, 2])).rejects.toThrow("必须是 JSON 对象")
    await expect(run("tree")).rejects.toThrow("必须是 JSON 对象")
  })
})
