import { afterEach, describe, expect, it, vi } from "vitest"
import { createPingUi } from "../index"
import type { ComponentNode } from "../types"

const tree: ComponentNode = { type: "application", id: "app", children: [] }

afterEach(() => {
  vi.restoreAllMocks()
})

describe("生命周期与钩子", () => {
  it("按创建、初始化、运行顺序触发不乱序", () => {
    const events: string[] = []
    const sdk = createPingUi({
      hooks: {
        onCreate: () => events.push("onCreate"),
        onInit: () => events.push("onInit"),
        onRun: () => events.push("onRun"),
      },
    })
    // sdk:created 在实例返回前触发（外部无法订阅），钩子 onCreate 可捕获
    sdk.on("sdk:initialized", () => events.push("sdk:initialized"))
    sdk.on("sdk:run", () => events.push("sdk:run"))

    sdk.init()
    sdk.run(tree)

    expect(events).toEqual([
      "onCreate",
      "sdk:initialized",
      "onInit",
      "sdk:run",
      "onRun",
    ])
  })

  it("处理器抛错时错误钩子收到封装错误且生命周期不中断", () => {
    const onError = vi.fn()
    const sdk = createPingUi({ hooks: { onError } })
    sdk.init()
    sdk.register("boom", () => {
      throw new Error("render failed")
    })
    // 不抛未捕获异常
    expect(() => sdk.run({ type: "boom", id: "b1" })).not.toThrow()
    expect(onError).toHaveBeenCalledTimes(1)
    const err = onError.mock.calls[0][1]
    expect(err).toMatchObject({
      nodeId: "b1",
      type: "boom",
      phase: "run",
      message: "render failed",
    })
    // 生命周期不中断：仍处于 running 态
    expect(sdk.state).toBe("running")
  })

  it("销毁幂等（二次调用无副作用不抛错）", () => {
    const onDestroy = vi.fn()
    const sdk = createPingUi({ hooks: { onDestroy } })
    sdk.init()
    sdk.destroy()
    expect(onDestroy).toHaveBeenCalledTimes(1)
    expect(sdk.state).toBe("destroyed")
    // 二次销毁：不抛错、钩子不重复调用
    expect(() => sdk.destroy()).not.toThrow()
    expect(onDestroy).toHaveBeenCalledTimes(1)
  })

  it("销毁后 get 任意 type 均为 undefined", () => {
    const sdk = createPingUi()
    sdk.init()
    sdk.register("menu", vi.fn())
    sdk.destroy()
    expect(sdk.get("menu")).toBeUndefined()
    expect(sdk.has("menu")).toBe(false)
  })

  it("销毁后 emit 不回调", () => {
    const sdk = createPingUi()
    const callback = vi.fn()
    sdk.on("event:test", callback)
    sdk.destroy()
    sdk.emit("event:test")
    expect(callback).not.toHaveBeenCalled()
  })

  it("update 与 run 触发的事件名不同", () => {
    const events: string[] = []
    const sdk = createPingUi()
    sdk.init()
    sdk.on("sdk:run", () => events.push("run"))
    sdk.on("sdk:updated", () => events.push("updated"))
    sdk.run(tree)
    sdk.update(tree)
    expect(events).toEqual(["run", "updated"])
  })

  it("未初始化即运行自动补 init() 并告警，随后处于 running 态", () => {
    const warnSpy = vi.spyOn(console, "warn").mockImplementation(() => {})
    const sdk = createPingUi()
    sdk.register("menu", vi.fn())
    // 不抛未捕获异常
    expect(() => sdk.run(tree)).not.toThrow()
    expect(warnSpy).toHaveBeenCalled()
    expect(sdk.state).toBe("running")
  })

  it("create 触发 component:created 事件与钩子", () => {
    const events: string[] = []
    const onComponentCreate = vi.fn()
    const sdk = createPingUi({ hooks: { onComponentCreate } })
    sdk.init()
    sdk.on("component:created", () => events.push("component:created"))
    const handler = sdk.create(tree)
    expect(events).toEqual(["component:created"])
    expect(onComponentCreate).toHaveBeenCalledTimes(1)
    expect(typeof handler).toBe("function")
  })
})
