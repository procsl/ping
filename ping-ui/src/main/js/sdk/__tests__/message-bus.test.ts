import { describe, expect, it, vi } from "vitest"
import { MessageBus } from "../message-bus"

describe("MessageBus", () => {
  it("注册后收到通知并拿到数据", () => {
    const bus = new MessageBus()
    const callback = vi.fn()
    bus.on("event:test", callback)
    bus.emit("event:test", { value: 42 })
    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledWith({ value: 42 })
  })

  it("注销后不再收到且其它回调不受影响", () => {
    const bus = new MessageBus()
    const removed = vi.fn()
    const kept = vi.fn()
    bus.on("event:test", removed)
    bus.on("event:test", kept)
    bus.off("event:test", removed)
    bus.emit("event:test", "data")
    expect(removed).not.toHaveBeenCalled()
    expect(kept).toHaveBeenCalledTimes(1)
  })

  it("同事件多回调全部被调用", () => {
    const bus = new MessageBus()
    const a = vi.fn()
    const b = vi.fn()
    bus.on("event:multi", a)
    bus.on("event:multi", b)
    bus.emit("event:multi")
    expect(a).toHaveBeenCalledTimes(1)
    expect(b).toHaveBeenCalledTimes(1)
  })

  it("销毁后已移除订阅不再回调", () => {
    const bus = new MessageBus()
    const callback = vi.fn()
    bus.on("event:test", callback)
    bus.clear()
    bus.emit("event:test")
    expect(callback).not.toHaveBeenCalled()
  })

  it("off 不传回调时移除该事件全部回调", () => {
    const bus = new MessageBus()
    const a = vi.fn()
    const b = vi.fn()
    bus.on("event:test", a)
    bus.on("event:test", b)
    bus.off("event:test")
    bus.emit("event:test")
    expect(a).not.toHaveBeenCalled()
    expect(b).not.toHaveBeenCalled()
  })
})
