import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import type { MountOptions } from "@/schema/types"
import { RendererRoot } from "@/RendererRoot"
import "@/styles/globals.css"

export type { MountOptions, ComponentNode } from "@/schema/types"

/**
 * 渲染器挂载入口（ESM，主壳与薄壳共用）：
 *   import { mount } from '../assets/renderer/renderer.js'
 *   mount({ namespace: 'system', manifest: '/v1/ui/menus?ns=system' })
 */
export function mount(options: MountOptions = {}): void {
  const selector = options.selector ?? "#ping-root"
  let container = document.querySelector<HTMLElement>(selector)
  if (!container) {
    container = document.createElement("div")
    container.id = "ping-root"
    document.body.appendChild(container)
  }

  createRoot(container).render(
    <StrictMode>
      <RendererRoot options={options} />
    </StrictMode>,
  )
}

export default mount
