import { describe, expect, it, vi } from "vitest"
import { createPingUi } from "../index"
import {
  PLACEHOLDER_RENDER_EVENT,
  placeholderHandler,
  resolvePlaceholderName,
  type PlaceholderRenderPayload,
} from "../handlers/placeholder"
import type { ComponentNode, Handler } from "../types"

describe("placeholder handler", () => {
  describe("resolvePlaceholderName", () => {
    it("优先取 properties.name", () => {
      const node: ComponentNode = {
        type: "placeholder",
        id: "p1",
        name: "节点名",
        properties: { name: "属性名" },
      }
      expect(resolvePlaceholderName(node)).toBe("属性名")
    })

    it("properties.name 缺失时回落节点 name", () => {
      const node: ComponentNode = { type: "placeholder", id: "p1", name: "用户管理" }
      expect(resolvePlaceholderName(node)).toBe("用户管理")
    })

    it("name 均缺失时回落 id", () => {
      const node: ComponentNode = { type: "placeholder", id: "p1" }
      expect(resolvePlaceholderName(node)).toBe("p1")
    })

    it("全部缺失时用「该页面」", () => {
      const node: ComponentNode = { type: "placeholder" }
      expect(resolvePlaceholderName(node)).toBe("该页面")
    })

    it("properties.name 为空白字符串时视为缺失", () => {
      const node: ComponentNode = {
        type: "placeholder",
        id: "p1",
        properties: { name: "   " },
      }
      expect(resolvePlaceholderName(node)).toBe("p1")
    })
  })

  describe("placeholderHandler", () => {
    it("发出渲染事件且载荷含解析后的页面名", () => {
      const sdk = createPingUi()
      sdk.init()
      const onRender = vi.fn()
      sdk.on(PLACEHOLDER_RENDER_EVENT, onRender)
      sdk.register("placeholder", placeholderHandler)

      const tree: ComponentNode = {
        type: "placeholder",
        id: "p1",
        name: "用户管理",
        properties: { name: "系统设置" },
      }
      sdk.run(tree)

      expect(onRender).toHaveBeenCalledTimes(1)
      const payload = onRender.mock.calls[0]?.[0] as PlaceholderRenderPayload
      expect(payload.name).toBe("系统设置")
      expect(payload.nodeId).toBe("p1")
      expect(payload.node).toBe(tree)
    })

    it("无子节点派发：children 不被处理", () => {
      const sdk = createPingUi()
      sdk.init()
      const processed: string[] = []
      const itemHandler: Handler = (_tree, node) => {
        processed.push(node.id ?? "")
      }
      sdk.register("item", itemHandler)
      sdk.register("placeholder", placeholderHandler)

      const tree: ComponentNode = {
        type: "placeholder",
        id: "p1",
        children: [{ type: "item", id: "hidden" }],
      }
      expect(() => sdk.run(tree)).not.toThrow()
      expect(processed).toEqual([])
    })

    it("未订阅渲染事件时静默执行（无副作用）", () => {
      const sdk = createPingUi()
      sdk.init()
      sdk.register("placeholder", placeholderHandler)
      expect(() =>
        sdk.run({ type: "placeholder", id: "p1", name: "用户管理" }),
      ).not.toThrow()
    })

    it("作为根节点运行：命中处理器且状态为 running", () => {
      const sdk = createPingUi()
      sdk.init()
      const onRender = vi.fn()
      sdk.on(PLACEHOLDER_RENDER_EVENT, onRender)
      sdk.register("placeholder", placeholderHandler)

      sdk.run({ type: "placeholder", id: "p1" })
      expect(onRender).toHaveBeenCalledTimes(1)
      expect(sdk.state).toBe("running")
    })

    it("处理器抛错时被 SDK 捕获并走降级（错误隔离）", () => {
      const onError = vi.fn()
      const sdk = createPingUi({ hooks: { onError } })
      sdk.init()

      // 注册一个会抛错的 placeholder 处理器
      const boomHandler: Handler = () => {
        throw new Error("placeholder boom")
      }
      sdk.register("placeholder", boomHandler)
      expect(() => sdk.run({ type: "placeholder", id: "p1" })).not.toThrow()
      expect(onError).toHaveBeenCalledTimes(1)
      expect(sdk.state).toBe("running")
    })
  })
})
