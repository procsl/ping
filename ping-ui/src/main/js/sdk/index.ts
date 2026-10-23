/**
 * Ping UI SDK entry（vite 打包入口，产物 renderer.js）。
 *
 * 当前导出两部分：
 * 1. 轻量 client（add-compose-client）：消费 compose 编译产物的 API 客户端——
 *    ComposeClient 门面 + Koa 式洋葱管道（Handler/compose）+ Context 协议 + ApiError
 * 2. createPingUi 占位：渲染器重建在后续提案（redesign-ping-ui-framework 4.3.1 约定）
 */

// ---- 轻量 client（业务组件/渲染器的唯一 import 口）----
export { ComposeClient } from "../client/client"
export { compose, type Handler, type Next } from "../client/pipeline"
export { ApiError, type ApiErrorInit } from "../client/error"
export {
  requireOperation,
  requireRequest,
  type ApiRequest,
  type ComponentNode,
  type ComposeClientOptions,
  type Context,
  type FetchInit,
  type OperationDescriptor,
  type OperationIndex,
  type ParameterDescriptor,
  type ResponseMeta,
  type TreeSource,
} from "../client/types"

// ---- 渲染器占位（后续提案重建）----

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
