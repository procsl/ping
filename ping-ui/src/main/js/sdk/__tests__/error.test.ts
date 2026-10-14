import { afterEach, describe, expect, it, vi } from "vitest"
import { createPingUi, ERROR_HANDLER_TYPE } from "../index"
import type { SdkError } from "../sdk-error"
import type { ComponentNode, ErrorHandler, Handler } from "../types"

afterEach(() => {
  vi.restoreAllMocks()
})

describe("错误封装与降级", () => {
  it("处理器抛错被封装且字段齐全", () => {
    const onError = vi.fn()
    const sdk = createPingUi({ hooks: { onError } })
    sdk.init()
    const boomHandler: Handler = () => {
      throw new Error("kaboom")
    }
    sdk.register("boom", boomHandler)
    sdk.run({ type: "boom", id: "b1" })
    const err: SdkError = onError.mock.calls[0][1]
    expect(err.nodeId).toBe("b1")
    expect(err.type).toBe("boom")
    expect(err.phase).toBe("run")
    expect(err.message).toBe("kaboom")
    expect(typeof err.stack).toBe("string")
    expect(err.cause).toBeInstanceOf(Error)
  })

  it("错误钩子被调用并收到该错误", () => {
    const onError = vi.fn()
    const sdk = createPingUi({ hooks: { onError } })
    sdk.init()
    const boomHandler: Handler = () => {
      throw new Error("kaboom")
    }
    sdk.register("boom", boomHandler)
    sdk.run({ type: "boom", id: "b1" })
    expect(onError).toHaveBeenCalledTimes(1)
    expect(onError.mock.calls[0][0]).toBe(sdk)
  })

  it("未注册 type 走降级且不抛未捕获异常", () => {
    const errorSpy = vi.spyOn(console, "error").mockImplementation(() => {})
    const sdk = createPingUi()
    sdk.init()
    expect(() => sdk.run({ type: "unknown_type", id: "u1" })).not.toThrow()
    // 默认 error_handler 输出错误
    expect(errorSpy).toHaveBeenCalled()
  })

  it("被降级节点的子节点仍被继续处理", () => {
    const sdk = createPingUi()
    sdk.init()
    const processed: string[] = []
    const itemHandler: Handler = (_tree, node) => {
      processed.push(node.id ?? "")
    }
    sdk.register("item", itemHandler)
    const tree: ComponentNode = {
      type: "unknown_parent",
      id: "parent",
      children: [{ type: "item", id: "child1" }],
    }
    expect(() => sdk.run(tree)).not.toThrow()
    expect(processed).toEqual(["child1"])
  })

  it("register error_handler 覆盖后由自定义实现接管", () => {
    const custom = vi.fn<ErrorHandler>(
      () => ({ type: "placeholder", name: "custom-fallback" }),
    )
    const sdk = createPingUi()
    sdk.init()
    sdk.register(ERROR_HANDLER_TYPE, custom, { override: true })
    sdk.run({ type: "unknown_type", id: "u1" })
    expect(custom).toHaveBeenCalledTimes(1)
    const err = custom.mock.calls[0]?.[0]
    expect(err?.type).toBe("unknown_type")
  })

  it("run 不会把 error_handler 当作节点处理器执行", () => {
    const custom = vi.fn<ErrorHandler>()
    const sdk = createPingUi()
    sdk.init()
    sdk.register(ERROR_HANDLER_TYPE, custom, { override: true })
    // 树根 type 是 error_handler 时：不可被当作可渲染 type 命中 → 走降级
    sdk.run({ type: ERROR_HANDLER_TYPE, id: "eh" })
    // 降级路径会调用 error_handler（作为错误处理器），但不会作为节点处理器被直接执行
    expect(custom).toHaveBeenCalledTimes(1)
    // 且不抛未捕获异常
    expect(sdk.state).toBe("running")
  })

  it("error_handler 自身抛错也不中断整棵树", () => {
    const sdk = createPingUi()
    sdk.init()
    const throwingHandler: ErrorHandler = () => {
      throw new Error("error_handler failed")
    }
    sdk.register(ERROR_HANDLER_TYPE, throwingHandler, { override: true })
    const processed: string[] = []
    const itemHandler: Handler = (_tree, node) => {
      processed.push(node.id ?? "")
    }
    sdk.register("item", itemHandler)
    const tree: ComponentNode = {
      type: "unknown_parent",
      id: "parent",
      children: [{ type: "item", id: "child1" }],
    }
    expect(() => sdk.run(tree)).not.toThrow()
    expect(processed).toEqual(["child1"])
  })
})
