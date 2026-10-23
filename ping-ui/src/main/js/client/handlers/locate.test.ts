import { describe, expect, it } from "vitest"
import { ApiError } from "../error"
import { compose } from "../pipeline"
import type { Context, OperationDescriptor, OperationIndex } from "../types"
import { locate } from "./locate"

const operation: OperationDescriptor = {
  operationId: "findUsers",
  method: "GET",
  path: "/v1/system/users",
}

function run(index: OperationIndex, seed: Context): Promise<Context> {
  return compose([locate(index), async (c) => c])(seed).then(() => seed)
}

describe("locate 环（查表定位）", () => {
  it("已知键定位：ctx.operation 填入索引条目", async () => {
    const index: OperationIndex = new Map([["findUsers", operation]])
    const ctx = await run(index, { operationId: "findUsers" })
    expect(ctx.operation).toBe(operation)
  })

  it("未知键：ApiError 拒绝且消息含键名", async () => {
    const index: OperationIndex = new Map()

    const err = await run(index, { operationId: "nope" }).catch((e: unknown) => e)

    expect(err).toBeInstanceOf(ApiError)
    expect((err as ApiError).message).toContain("nope")
    expect((err as ApiError).operationId).toBe("nope")
  })

  it("缺 operationId（seed 被破坏）：明确报错", async () => {
    const index: OperationIndex = new Map()
    await expect(run(index, {})).rejects.toThrow("ctx.operationId 不存在")
  })
})
