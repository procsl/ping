import { describe, expect, it } from "vitest"
import { ApiError } from "../error"
import { buildRequest } from "./bind"
import type { OperationDescriptor } from "../types"

describe("bind 环（参数自动绑定，对应 spec 5 场景）", () => {
  it("path 参数替换：模板段替换为参数值后发起请求", () => {
    const operation: OperationDescriptor = {
      operationId: "accountStatusSetting",
      method: "PUT",
      path: "/v1/system/account/status/{id}",
      parameters: [{ name: "id", in: "path" }],
    }

    const request = buildRequest({ params: { id: "42", state: "disable" } }, operation)

    expect(request.method).toBe("PUT")
    expect(request.url).toBe("/v1/system/account/status/42")
    expect(request.body).toBeUndefined()
  })

  it("query 参数平铺：多参数拼接，数组按重复键展开", () => {
    const operation: OperationDescriptor = {
      operationId: "findUsers",
      method: "GET",
      path: "/v1/system/users",
      parameters: [
        { name: "name", in: "query" },
        { name: "state", in: "query" },
        { name: "ids", in: "query" },
      ],
    }

    const request = buildRequest(
      { params: { name: "x", state: "enable", ids: ["1", "2"] } },
      operation,
    )

    expect(request.url).toBe("/v1/system/users?name=x&state=enable&ids=1&ids=2")
  })

  it("body 序列化：声明 requestBody 且提供 params.body 时 JSON 序列化 + Content-Type", () => {
    const operation: OperationDescriptor = {
      operationId: "saveUser",
      method: "POST",
      path: "/v1/system/users",
      requestBody: { content: { "application/json": {} } },
    }

    const request = buildRequest({ params: { body: { name: "x" } } }, operation)

    expect(request.body).toBe(JSON.stringify({ name: "x" }))
    expect(request.headers["Content-Type"]).toBe("application/json")
  })

  it("未声明 requestBody：即使提供 body 参数也不发送请求体", () => {
    const operation: OperationDescriptor = {
      operationId: "findUsers",
      method: "GET",
      path: "/v1/system/users",
    }

    const request = buildRequest({ params: { body: { sneaky: true } } }, operation)

    expect(request.body).toBeUndefined()
    expect(request.headers["Content-Type"]).toBeUndefined()
  })

  it("缺失 path 参数：ApiError 拒绝（消息含参数名，不发请求）", () => {
    const operation: OperationDescriptor = {
      operationId: "accountStatusSetting",
      method: "PUT",
      path: "/v1/system/account/status/{id}",
      parameters: [{ name: "id", in: "path" }],
    }

    let thrown: unknown
    try {
      buildRequest({ params: { state: "disable" } }, operation)
    } catch (e) {
      thrown = e
    }

    expect(thrown).toBeInstanceOf(ApiError)
    expect((thrown as ApiError).message).toContain("id")
  })

  it("未声明参数忽略：多余参数不进 URL 不进 body，请求照常", () => {
    const operation: OperationDescriptor = {
      operationId: "findUsers",
      method: "GET",
      path: "/v1/system/users",
      parameters: [{ name: "name", in: "query" }],
    }

    const request = buildRequest(
      { params: { name: "x", injected: "evil", page: 9 } },
      operation,
    )

    expect(request.url).toBe("/v1/system/users?name=x")
  })

  it("query 空值跳过；参数值 URL 编码", () => {
    const operation: OperationDescriptor = {
      operationId: "findUsers",
      method: "GET",
      path: "/v1/system/users",
      parameters: [
        { name: "name", in: "query" },
        { name: "state", in: "query" },
      ],
    }

    const request = buildRequest(
      { params: { name: "a b&c", state: undefined } },
      operation,
    )

    expect(request.url).toBe("/v1/system/users?name=a%20b%26c")
  })
})
