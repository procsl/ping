/**
 * demo 入口 —— 原生 DOM 渲染，无任何 UI 框架依赖。
 * 经 vite dev server 运行（`npm run demo`），不进 build 产物。
 */

import { createPingUi } from "../index"
import { demoTree, registerDemoHandlers } from "./demo-tree"
import type { Handler } from "../types"

const outputEl = document.querySelector<HTMLElement>("#demo-output")
const errorsEl = document.querySelector<HTMLElement>("#demo-errors")

if (!outputEl || !errorsEl) {
  throw new Error("demo 页面缺少挂载节点")
}

function el(tag: string, className: string, text: string): HTMLElement {
  const node = document.createElement(tag)
  node.className = className
  node.textContent = text
  return node
}

// 注册「真实渲染」的处理器：用原生 DOM 构建可见输出
const sdk = createPingUi({ namespace: "demo", debug: true })
sdk.init()

const output = registerDemoHandlers(sdk)

// 覆盖 text 处理器为真实 DOM 渲染（demo-tree 里的 text 只记录日志）
const textHandler: Handler = (_tree, node) => {
  const value = (node.properties?.value as string | undefined) ?? node.name ?? node.id
  outputEl.appendChild(el("span", "demo-chip demo-chip-ok", `[${node.id}] ${value}`))
}
sdk.register("text", textHandler, { override: true })

const cardHandler: Handler = (_tree, node, _mb, sdkInstance) => {
  const card = el("div", "demo-card", "")
  const title = el("h3", "demo-card-title", (node.name as string) ?? "Card")
  card.appendChild(title)
  outputEl.appendChild(card)
  const created = sdkInstance.create(_tree)
  for (const child of node.children ?? []) {
    created(child)
  }
}
sdk.register("card", cardHandler, { override: true })

const rowHandler: Handler = (_tree, node, _mb, sdkInstance) => {
  const row = el("div", "demo-row", "")
  outputEl.appendChild(row)
  const created = sdkInstance.create(_tree)
  for (const child of node.children ?? []) {
    created(child)
  }
}
sdk.register("row", rowHandler, { override: true })

const brokenHandler: Handler = (_tree, node) => {
  outputEl.appendChild(
    el("span", "demo-chip demo-chip-broken", `[${node.id}] 即将抛错`),
  )
  throw new Error(`broken handler for ${node.id}`)
}
sdk.register("broken", brokenHandler, { override: true })

// 自定义 error_handler：页面上可见降级形态
sdk.register(
  "error_handler",
  (error: { type: string; message: string }) => {
    errorsEl.textContent = ""
    errorsEl.appendChild(
      el("div", "demo-error-line", `[${error.type}] ${error.message}`),
    )
    return { type: "placeholder", name: `降级：${error.type}` }
  },
  { override: true },
)

// 运行组件树
sdk.run(demoTree)

// 展示收集到的日志输出（含未注册降级路径）
outputEl.appendChild(el("div", "demo-log", "—— 处理器日志 ——"))
for (const line of output.rendered) {
  outputEl.appendChild(el("div", "demo-log-line", line))
}
