import { describe, expect, it } from "vitest"
import { ApiError } from "./error"

describe("ApiError", () => {
  it("从 MessageVO 形态提取 message", () => {
    const e = new ApiError({
      status: 400,
      operationId: "saveUser",
      body: { message: "参数校验失败" },
    })

    expect(e).toBeInstanceOf(Error)
    expect(e.name).toBe("ApiError")
    expect(e.message).toBe("参数校验失败")
    expect(e.status).toBe(400)
    expect(e.operationId).toBe("saveUser")
    expect(e.fieldErrors).toBeUndefined()
  })

  it("从 ParameterErrorVO 形态透出 fieldErrors（errors 数组原样不加工）", () => {
    const errors = [
      { field: "account", tips: "不可为空" },
      { field: "email", tips: "格式不正确" },
    ]
    const e = new ApiError({
      status: 400,
      operationId: "saveUser",
      body: {
        code: "METHOD_ARGUMENT_NOT_VALID_ERROR",
        message: "参数校验失败",
        errors,
      },
    })

    expect(e.message).toBe("参数校验失败")
    expect(e.fieldErrors).toEqual(errors)
    expect(e.fieldErrors).toBe(errors)   // 原样引用，零加工
  })

  it("缺 message 回落 statusText，statusText 也缺回落状态文本", () => {
    const a = new ApiError({
      status: 502,
      operationId: "findUsers",
      statusText: "Bad Gateway",
      body: { code: "X" },
    })
    expect(a.message).toBe("Bad Gateway")

    const b = new ApiError({ status: 502, operationId: "findUsers" })
    expect(b.message).toBe("HTTP 502")
  })

  it("message 显式指定时优先于响应体（未知 operationId 等场景）", () => {
    const e = new ApiError({
      status: 0,
      operationId: "nope",
      message: "未知的 operationId: nope",
      body: { message: "被忽略" },
    })
    expect(e.message).toBe("未知的 operationId: nope")
    expect(e.status).toBe(0)
  })

  it("网络失败：status=0 且 cause 保留底层原因", () => {
    const cause = new TypeError("Failed to fetch")
    const e = new ApiError({
      status: 0,
      operationId: "findUsers",
      statusText: "Network Error",
      cause,
    })

    expect(e.status).toBe(0)
    expect(e.message).toBe("Network Error")
    expect(e.cause).toBe(cause)
  })

  it("响应体 message 非字符串（如数字/对象）视为缺失", () => {
    const e = new ApiError({
      status: 500,
      operationId: "x",
      statusText: "Internal Server Error",
      body: { message: 42 },
    })
    expect(e.message).toBe("Internal Server Error")
  })
})
