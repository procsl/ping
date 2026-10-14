/**
 * demo 组件树与处理器定义 —— 可被 vitest 导入，验证三条路径各自的输出。
 *
 * 三条路径：
 * 1. 正常命中：`card` / `row` / `text` 处理器被注册并渲染；
 * 2. 未注册降级：`unknown_widget` 未注册处理器 → 走 error_handler 降级；
 * 3. 抛错隔离：`broken` 处理器抛错 → 同级后续节点照常执行。
 */

import type { ComponentNode, Handler, PingUi } from "../types"

/** 记录渲染输出的收集器（demo 与测试共用） */
export interface DemoOutput {
  rendered: string[]
  errors: string[]
}

export function createDemoOutput(): DemoOutput {
  return { rendered: [], errors: [] }
}

/** 注册 demo 用处理器；返回输出收集器 */
export function registerDemoHandlers(sdk: PingUi): DemoOutput {
  const output = createDemoOutput()

  const textHandler: Handler = (_tree, node) => {
    output.rendered.push(`text:${node.id ?? node.type}`)
  }
  sdk.register("text", textHandler)

  const rowHandler: Handler = (_tree, node, _mb, sdkInstance) => {
    output.rendered.push(`row:${node.id ?? node.type}`)
    // 处理器主动派发子节点
    const created = sdkInstance.create(_tree)
    for (const child of node.children ?? []) {
      created(child)
    }
  }
  sdk.register("row", rowHandler)

  const cardHandler: Handler = (_tree, node, _mb, sdkInstance) => {
    output.rendered.push(`card:${node.id ?? node.type}`)
    const created = sdkInstance.create(_tree)
    for (const child of node.children ?? []) {
      created(child)
    }
  }
  sdk.register("card", cardHandler)

  const brokenHandler: Handler = (_tree, node) => {
    output.rendered.push(`broken:${node.id ?? node.type}`)
    throw new Error(`broken handler for ${node.id ?? node.type}`)
  }
  sdk.register("broken", brokenHandler)

  // 覆盖默认 error_handler：记录错误并返回占位
  sdk.register(
    "error_handler",
    (error: { type: string; message: string }) => {
      output.errors.push(`${error.type}:${error.message}`)
      return { type: "placeholder", name: `降级：${error.type}` }
    },
    { override: true },
  )

  return output
}

/** demo 组件树：覆盖正常命中、未注册降级、抛错隔离三条路径 */
export const demoTree: ComponentNode = {
  type: "card",
  id: "demo-root",
  name: "SDK Demo",
  properties: { title: "Ping UI Web SDK 演示" },
  children: [
    {
      type: "row",
      id: "row-1",
      children: [
        { type: "text", id: "text-1", name: "正常命中", properties: { value: "hello" } },
        { type: "text", id: "text-2", name: "正常命中 2", properties: { value: "world" } },
      ],
    },
    {
      type: "unknown_widget",
      id: "unknown-1",
      name: "未注册类型",
      children: [
        { type: "text", id: "text-3", name: "降级后子节点保留" },
      ],
    },
    {
      type: "row",
      id: "row-2",
      children: [
        { type: "broken", id: "broken-1", name: "抛错节点" },
        { type: "text", id: "text-4", name: "同级继续" },
      ],
    },
  ],
}
