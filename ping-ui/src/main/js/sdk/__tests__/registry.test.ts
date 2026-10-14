import { afterEach, describe, expect, it, vi } from "vitest"
import { Registry, ERROR_HANDLER_TYPE } from "../registry"
import type { ComponentNode } from "../types"

const node: ComponentNode = { type: "menu", id: "m1" }

afterEach(() => {
  vi.restoreAllMocks()
})

describe("Registry", () => {
  it("首次注册成功", () => {
    const registry = new Registry()
    const handler = vi.fn()
    const ok = registry.register("menu", handler)
    expect(ok).toBe(true)
    expect(registry.has("menu", "default")).toBe(true)
    const got = registry.get("menu", "default")
    expect(got).toBeDefined()
    got?.(node, node, {} as never, {} as never)
    expect(handler).toHaveBeenCalledTimes(1)
  })

  it("重复注册被拒绝、原处理器不变、console.warn 被调用", () => {
    const registry = new Registry()
    const warnSpy = vi.spyOn(console, "warn").mockImplementation(() => {})
    const first = vi.fn()
    const second = vi.fn()
    registry.register("menu", first)
    const ok = registry.register("menu", second)
    expect(ok).toBe(false)
    expect(warnSpy).toHaveBeenCalled()
    // 原处理器不变：查询到的仍是 first
    const got = registry.get("menu", "default")
    got?.(node, node, {} as never, {} as never)
    expect(first).toHaveBeenCalledTimes(1)
    expect(second).not.toHaveBeenCalled()
  })

  it("显式覆盖生效", () => {
    const registry = new Registry()
    const first = vi.fn()
    const second = vi.fn()
    registry.register("menu", first)
    const ok = registry.register("menu", second, { override: true })
    expect(ok).toBe(true)
    const got = registry.get("menu", "default")
    got?.(node, node, {} as never, {} as never)
    expect(second).toHaveBeenCalledTimes(1)
    expect(first).not.toHaveBeenCalled()
  })

  it("当前命名空间未命中时回退 default 命中", () => {
    const registry = new Registry()
    const handler = vi.fn()
    registry.register("menu", handler, { namespace: "default" })
    expect(registry.has("menu", "app")).toBe(true)
    const got = registry.get("menu", "app")
    expect(got).toBeDefined()
    got?.(node, node, {} as never, {} as never)
    expect(handler).toHaveBeenCalledTimes(1)
  })

  it("同名不同命名空间互不覆盖", () => {
    const registry = new Registry()
    const a = vi.fn()
    const b = vi.fn()
    registry.register("menu", a, { namespace: "app" })
    registry.register("menu", b, { namespace: "other" })
    const gotA = registry.get("menu", "app")
    gotA?.(node, node, {} as never, {} as never)
    expect(a).toHaveBeenCalledTimes(1)
    expect(b).not.toHaveBeenCalled()
  })

  it("unregister 精确到命名空间下的一条", () => {
    const registry = new Registry()
    const a = vi.fn()
    const b = vi.fn()
    registry.register("menu", a, { namespace: "app" })
    registry.register("menu", b, { namespace: "default" })
    registry.unregister("menu", { namespace: "app" })
    // app 命名空间下已无该 type（listTypes 不跨命名空间回退）
    expect(registry.listTypes("app")).toEqual([])
    // 查询回退到 default 命名空间的处理器
    const got = registry.get("menu", "app")
    got?.(node, node, {} as never, {} as never)
    expect(a).not.toHaveBeenCalled()
    expect(b).toHaveBeenCalledTimes(1)
  })

  it("error_handler 保留名不可被当作可渲染 type 命中", () => {
    const registry = new Registry()
    registry.register(ERROR_HANDLER_TYPE, vi.fn(), { override: true })
    expect(registry.has(ERROR_HANDLER_TYPE, "default")).toBe(false)
    expect(registry.get(ERROR_HANDLER_TYPE, "default")).toBeUndefined()
    // 但错误处理器可经 getErrorHandler 取到
    expect(registry.getErrorHandler("default")).toBeDefined()
  })
})
