/**
 * Ping UI 渲染器占位 entry（redesign-ping-ui-framework 4.3.1）。
 *
 * 旧 SDK 机制（registry / message-bus / sdk-error / handlers）已随本变更移除；
 * 新渲染器与轻量 client 不在本次范围。保留 `createPingUi` 占位以维持
 * vite 打包入口（vite.config entry）与薄壳页 `META-INF/ping/entry/index.html`
 * 的调用形态：`createPingUi(options).init()` / `.destroy()`。
 */

export interface PingUiOptions {
  namespace?: string
  debug?: boolean
}

export interface PingUi {
  /** 占位：渲染器落地前为空实现 */
  init(): void
  /** 占位：渲染器落地前为空实现 */
  destroy(): void
}

export function createPingUi(options: PingUiOptions = {}): PingUi {
  console.debug("[ping-ui] createPingUi 占位实现（渲染器不在本变更范围）", options)
  return {
    init() {
      // 占位：渲染器重建在后续变更
    },
    destroy() {
      // 占位：渲染器重建在后续变更
    },
  }
}
