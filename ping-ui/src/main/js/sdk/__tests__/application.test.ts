import { describe, expect, it, vi } from "vitest"
import { createPingUi } from "../index"
import {
  APPLICATION_RENDER_EVENT,
  applicationHandler,
  DEFAULT_APPLICATION_NAME,
  resolveApplicationName,
  type ApplicationRenderPayload,
} from "../handlers/application"
import type { ComponentNode, Handler } from "../types"

describe("application handler", () => {
  describe("resolveApplicationName", () => {
    it("优先取 properties.name", () => {
      const node: ComponentNode = {
        type: "application",
        id: "app",
        name: "节点名",
        properties: { name: "Ping Admin" },
      }
      expect(resolveApplicationName(node)).toBe("Ping Admin")
    })

    it("properties.name 缺失时回落节点 name", () => {
      const node: ComponentNode = { type: "application", id: "app", name: "Ping 后台" }
      expect(resolveApplicationName(node)).toBe("Ping 后台")
    })

    it("name 均缺失时用缺省应用名", () => {
      const node: ComponentNode = { type: "application", id: "app" }
      expect(resolveApplicationName(node)).toBe(DEFAULT_APPLICATION_NAME)
    })

    it("properties.name 为空白字符串时视为缺失", () => {
      const node: ComponentNode = {
        type: "application",
        id: "app",
        properties: { name: "   " },
      }
      expect(resolveApplicationName(node)).toBe(DEFAULT_APPLICATION_NAME)
    })
  })

  describe("applicationHandler", () => {
    it("发出渲染事件且载荷含解析后的应用名", () => {
      const sdk = createPingUi()
      sdk.init()
      const onRender = vi.fn()
      sdk.on(APPLICATION_RENDER_EVENT, onRender)
      sdk.register("application", applicationHandler)

      const tree: ComponentNode = {
        type: "application",
        id: "ping.ui.app",
        name: "Ping Admin",
        properties: { name: "Ping 管理后台" },
      }
      sdk.run(tree)

      expect(onRender).toHaveBeenCalledTimes(1)
      const payload = onRender.mock.calls[0]?.[0] as ApplicationRenderPayload
      expect(payload.name).toBe("Ping 管理后台")
      expect(payload.nodeId).toBe("ping.ui.app")
      expect(payload.node).toBe(tree)
    })

    it("派发全部子节点（处理器主动驱动）", () => {
      const sdk = createPingUi()
      sdk.init()
      const processed: string[] = []
      const childHandler: Handler = (_tree, node) => {
        processed.push(node.id ?? "")
      }
      // 注册子节点类型处理器
      sdk.register("layout", childHandler)
      sdk.register("menu", childHandler)
      sdk.register("application", applicationHandler)

      const tree: ComponentNode = {
        type: "application",
        id: "app",
        children: [
          { type: "layout", id: "layout-left" },
          { type: "menu", id: "menu-1" },
        ],
      }
      sdk.run(tree)

      expect(processed).toEqual(["layout-left", "menu-1"])
    })

    it("无子节点时照常运行（空应用树）", () => {
      const sdk = createPingUi()
      sdk.init()
      const onRender = vi.fn()
      sdk.on(APPLICATION_RENDER_EVENT, onRender)
      sdk.register("application", applicationHandler)

      expect(() => sdk.run({ type: "application", id: "app" })).not.toThrow()
      expect(onRender).toHaveBeenCalledTimes(1)
      expect(sdk.state).toBe("running")
    })

    it("子节点抛错被隔离，不影响其它子节点", () => {
      const sdk = createPingUi()
      sdk.init()
      const processed: string[] = []
      const okHandler: Handler = (_tree, node) => {
        processed.push(node.id ?? "")
      }
      const boomHandler: Handler = () => {
        throw new Error("boom")
      }
      sdk.register("layout", okHandler)
      sdk.register("menu", boomHandler)
      sdk.register("application", applicationHandler)

      const tree: ComponentNode = {
        type: "application",
        id: "app",
        children: [
          { type: "layout", id: "layout-left" },
          { type: "menu", id: "menu-boom" },
          { type: "layout", id: "layout-right" },
        ],
      }
      expect(() => sdk.run(tree)).not.toThrow()
      expect(processed).toEqual(["layout-left", "layout-right"])
    })

    it("作为根节点运行：命中处理器且状态为 running", () => {
      const sdk = createPingUi()
      sdk.init()
      const onRender = vi.fn()
      sdk.on(APPLICATION_RENDER_EVENT, onRender)
      sdk.register("application", applicationHandler)

      sdk.run({ type: "application", id: "app", name: "Ping Admin" })
      expect(onRender).toHaveBeenCalledTimes(1)
      expect(sdk.state).toBe("running")
    })
  })
})
