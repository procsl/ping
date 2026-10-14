import { afterEach, describe, expect, it, vi } from "vitest"
import { createPingUi } from "../index"
import { MessageBus } from "../message-bus"
import type { ComponentNode, Handler } from "../types"

afterEach(() => {
  vi.restoreAllMocks()
})

describe("run / create 派发", () => {
  it("命中处理器：执行且能拿到整棵树、当前节点、消息对象与 SDK 实例", () => {
    const sdk = createPingUi()
    sdk.init()
    const bus = new MessageBus()
    const received: unknown[] = []
    const handler: Handler = (tree, node, messageBus, sdkInstance) => {
      received.push(tree, node, messageBus, sdkInstance)
    }
    sdk.register("menu", handler)
    const tree: ComponentNode = { type: "menu", id: "m1" }
    sdk.run(tree, bus)
    expect(received[0]).toBe(tree)
    expect(received[1]).toBe(tree)
    expect(received[2]).toBe(bus)
    expect(received[3]).toBe(sdk)
  })

  it("处理器无返回值", () => {
    const sdk = createPingUi()
    sdk.init()
    sdk.register("menu", () => {
      return "some-value" // 处理器返回值被忽略
    })
    const result = sdk.run({ type: "menu", id: "m1" })
    expect(result).toBeUndefined()
  })

  it("预绑定后只传 node 即可执行，且拿到的是创建时绑定的那份 tree 与 messageBus", () => {
    const sdk = createPingUi()
    sdk.init()
    const bus = new MessageBus()
    const received: unknown[] = []
    const handler: Handler = (tree, node, messageBus) => {
      received.push(tree, node, messageBus)
    }
    sdk.register("menu", handler)
    const tree: ComponentNode = { type: "menu", id: "root" }
    const created = sdk.create(tree, bus)
    const child: ComponentNode = { type: "menu", id: "child" }
    created(child)
    expect(received[0]).toBe(tree)
    expect(received[1]).toBe(child)
    expect(received[2]).toBe(bus)
  })

  it("派发的某子节点抛错后，同一处理器后续要派发的子节点照常执行", () => {
    const sdk = createPingUi()
    sdk.init()
    const order: string[] = []
    const itemHandler: Handler = (_tree, node) => {
      order.push(node.id ?? "")
      if (node.id === "bad") {
        throw new Error("item failed")
      }
    }
    sdk.register("item", itemHandler)
    const containerHandler: Handler = (_tree, node, _mb, sdkInstance) => {
      const created = sdkInstance.create(_tree)
      for (const child of node.children ?? []) {
        created(child)
      }
    }
    sdk.register("container", containerHandler)
    const tree: ComponentNode = {
      type: "container",
      id: "root",
      children: [
        { type: "item", id: "a" },
        { type: "item", id: "bad" },
        { type: "item", id: "c" },
      ],
    }
    expect(() => sdk.run(tree)).not.toThrow()
    expect(order).toEqual(["a", "bad", "c"])
  })

  it("处理器只派发部分子节点时被跳过者不被处理且不报错", () => {
    const sdk = createPingUi()
    sdk.init()
    const processed: string[] = []
    const itemHandler: Handler = (_tree, node) => {
      processed.push(node.id ?? "")
    }
    sdk.register("item", itemHandler)
    const tabsHandler: Handler = (_tree, node, _mb, sdkInstance) => {
      const created = sdkInstance.create(_tree)
      // 只派发第一个子节点（模拟 tabs 只渲染激活页签）
      const first = node.children?.[0]
      if (first) {
        created(first)
      }
    }
    sdk.register("tabs", tabsHandler)
    const tree: ComponentNode = {
      type: "tabs",
      id: "tabs1",
      children: [
        { type: "item", id: "tab-a" },
        { type: "item", id: "tab-b" },
      ],
    }
    expect(() => sdk.run(tree)).not.toThrow()
    expect(processed).toEqual(["tab-a"])
  })

  it("处理器不派发任何子节点时运行照常继续", () => {
    const sdk = createPingUi()
    sdk.init()
    const processed: string[] = []
    const itemHandler: Handler = (_tree, node) => {
      processed.push(node.id ?? "")
    }
    sdk.register("item", itemHandler)
    const leafHandler: Handler = () => {
      // 不派发任何子节点
    }
    sdk.register("leaf", leafHandler)
    const tree: ComponentNode = {
      type: "leaf",
      id: "leaf1",
      children: [{ type: "item", id: "hidden" }],
    }
    expect(() => sdk.run(tree)).not.toThrow()
    expect(processed).toEqual([])
  })

  it("整棵树不因单点失败中断", () => {
    const sdk = createPingUi()
    sdk.init()
    const order: string[] = []
    const boomHandler: Handler = () => {
      throw new Error("boom")
    }
    sdk.register("boom", boomHandler)
    const okHandler: Handler = (_tree, node) => {
      order.push(node.id ?? "")
    }
    sdk.register("ok", okHandler)
    const rootHandler: Handler = (_tree, node, _mb, sdkInstance) => {
      const created = sdkInstance.create(_tree)
      for (const child of node.children ?? []) {
        created(child)
      }
    }
    sdk.register("root", rootHandler)
    const tree: ComponentNode = {
      type: "root",
      id: "root",
      children: [
        { type: "boom", id: "b1" },
        { type: "ok", id: "o1" },
      ],
    }
    expect(() => sdk.run(tree)).not.toThrow()
    expect(order).toEqual(["o1"])
  })
})
