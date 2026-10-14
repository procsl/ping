/**
 * Ping UI Web SDK 核心 —— 对应 `ping-ui/doc/sdk-design.md`。
 *
 * 框架无关的抽象组件运行时：处理器注册表、组件树运行入口（`run`/`create`）、
 * 处理器主动驱动的子节点派发、实例生命周期与钩子、消息通知机制，
 * 以及基于可覆盖 `error_handler` 的单节点错误隔离与降级。
 *
 * 关键设计（design D3）：
 * - 子节点派发由处理器主动驱动：SDK 不自动遍历树，处理器通过 `create` 返回的
 *   预绑定处理器逐个派发子节点，可自行跳过某些子节点（支撑 `tabs` 不渲染未激活页签）。
 * - 错误包装实现于 `create` 返回的处理器内部：任何经它派发的节点，其异常都被捕获为
 *   `SdkError` 并交给 `error_handler`，失败只影响该节点，处理器的遍历循环继续执行下一个。
 */

import { MessageBus } from "./message-bus"
import { Registry, ERROR_HANDLER_TYPE } from "./registry"
import { toSdkError, type SdkError, type SdkPhase } from "./sdk-error"
import type {
  ComponentNode,
  CreatedHandler,
  ErrorHandler,
  Handler,
  PingUi,
  PingUiOptions,
  RegisterOptions,
  SdkHooks,
  SdkSnapshot,
  SdkState,
} from "./types"

export * from "./types"
export { MessageBus } from "./message-bus"
export { Registry, ERROR_HANDLER_TYPE } from "./registry"
export { toSdkError, type SdkError, type SdkPhase } from "./sdk-error"
export {
  APPLICATION_RENDER_EVENT,
  applicationHandler,
  DEFAULT_APPLICATION_NAME,
  resolveApplicationName,
  type ApplicationRenderPayload,
} from "./handlers/application"
export {
  PLACEHOLDER_RENDER_EVENT,
  placeholderHandler,
  resolvePlaceholderName,
  type PlaceholderRenderPayload,
} from "./handlers/placeholder"

const DEFAULT_NAMESPACE = "default"

/** 默认 error_handler：输出错误 + 返回占位降级形态（design D6，init() 时注册，可被覆盖） */
function defaultErrorHandler(error: SdkError, node: ComponentNode): unknown {
  console.error(`[ping-ui] 组件 "${node.type}" 渲染失败`, error)
  return { type: "placeholder", id: node.id, name: `未知组件：${node.type}` }
}

/**
 * 创建 SDK 实例。
 * 生命周期：created → initialized → running → destroyed（design D8 显式状态机）。
 */
export function createPingUi(options: PingUiOptions = {}): PingUi {
  const namespace = options.namespace ?? DEFAULT_NAMESPACE
  const debug = options.debug ?? false
  const hooks: SdkHooks = options.hooks ?? {}
  const registry = new Registry()
  const messageBus = new MessageBus()

  let state: SdkState = "created"
  let currentMessageBus: MessageBus = messageBus
  let currentPhase: SdkPhase = "run"

  function log(...args: unknown[]): void {
    if (debug) {
      console.log("[ping-ui]", ...args)
    }
  }

  function emit(event: string, payload?: unknown): void {
    messageBus.emit(event, payload)
  }

  /**
   * 单节点派发（错误包装路径，design D3）：
   * 命中处理器 → 执行；未命中 → 降级；抛错 → 包装为 SdkError 交 error_handler。
   * 任何失败只影响当前节点，不冒泡进处理器的遍历循环。
   */
  function dispatch(tree: ComponentNode, node: ComponentNode, mb: MessageBus): void {
    try {
      const handler = registry.get(node.type, namespace)
      if (handler) {
        handler(tree, node, mb, sdk)
      } else {
        // 未注册类型 → 降级（子节点保留）
        const error = toSdkError(
          new Error(`未注册的组件类型 "${node.type}"`),
          node,
          currentPhase,
        )
        dispatchError(tree, node, mb, error)
      }
    } catch (e) {
      const error = toSdkError(e, node, currentPhase)
      dispatchError(tree, node, mb, error)
    }
  }

  /** 错误处理：错误钩子 → error_handler → 子节点保留（继续派发） */
  function dispatchError(
    tree: ComponentNode,
    node: ComponentNode,
    mb: MessageBus,
    error: SdkError,
  ): void {
    hooks.onError?.(sdk, error)
    try {
      const errorHandler = registry.getErrorHandler(namespace)
      if (errorHandler) {
        errorHandler(error, node, mb, sdk)
      } else {
        console.error(`[ping-ui] 组件 "${node.type}" 渲染失败`, error)
      }
    } catch (e) {
      // error_handler 自身抛错也不能中断整棵树
      console.error(`[ping-ui] error_handler 自身抛错`, e)
    }
    // 子节点保留：被降级节点的子节点仍被继续处理
    for (const child of node.children ?? []) {
      dispatch(tree, child, mb)
    }
  }

  const sdk: PingUi = {
    get namespace() {
      return namespace
    },
    get debug() {
      return debug
    },
    get state() {
      return state
    },

    register(
      type: string,
      handler: Handler | ErrorHandler,
      registerOptions?: RegisterOptions,
    ): boolean {
      const ok = registry.register(type, handler, registerOptions)
      log(`register "${type}" → ${ok ? "ok" : "rejected"}`)
      return ok
    },

    unregister(type: string, registerOptions?: RegisterOptions): void {
      registry.unregister(type, registerOptions)
      log(`unregister "${type}"`)
    },

    has(type: string): boolean {
      return registry.has(type, namespace)
    },

    get(type: string): Handler | undefined {
      return registry.get(type, namespace)
    },

    init(): void {
      if (state === "destroyed") {
        console.warn("[ping-ui] 实例已销毁，init() 被忽略")
        return
      }
      if (state === "created") {
        state = "initialized"
        // 注册默认 error_handler（可被覆盖）
        registry.register(ERROR_HANDLER_TYPE, defaultErrorHandler, {
          override: true,
        })
        log("init: registered default error_handler")
        emit("sdk:initialized")
        hooks.onInit?.(sdk)
      }
    },

    run(tree: ComponentNode, bus?: MessageBus): void {
      if (state === "destroyed") {
        console.warn("[ping-ui] 实例已销毁，run() 被忽略")
        return
      }
      if (state === "created") {
        // design D8：未初始化即运行，自动补 init() 并告警
        console.warn("[ping-ui] 实例未初始化即运行，自动补 init()")
        sdk.init()
      }
      state = "running"
      currentMessageBus = bus ?? messageBus
      currentPhase = "run"
      log("run", tree.type)
      emit("sdk:run", tree)
      hooks.onRun?.(sdk, tree)
      // 执行根节点：与 create 共用同一派发路径，错误隔离一致
      dispatch(tree, tree, currentMessageBus)
    },

    create(tree: ComponentNode, bus?: MessageBus): CreatedHandler {
      const mb = bus ?? messageBus
      const handler: CreatedHandler = (node) => {
        dispatch(tree, node, mb)
      }
      log("create", tree.type)
      emit("component:created", { tree, messageBus: mb, handler })
      hooks.onComponentCreate?.(sdk, tree, mb, handler)
      return handler
    },

    update(tree: ComponentNode): void {
      if (state === "destroyed") {
        console.warn("[ping-ui] 实例已销毁，update() 被忽略")
        return
      }
      if (state === "created") {
        console.warn("[ping-ui] 实例未初始化即更新，自动补 init()")
        sdk.init()
      }
      state = "running"
      currentMessageBus = messageBus
      currentPhase = "update"
      log("update", tree.type)
      emit("sdk:updated", tree)
      hooks.onRun?.(sdk, tree)
      dispatch(tree, tree, currentMessageBus)
    },

    destroy(): void {
      if (state === "destroyed") {
        return // 幂等
      }
      state = "destroyed"
      log("destroy")
      try {
        emit("sdk:destroyed")
        hooks.onDestroy?.(sdk)
      } catch (e) {
        const error = toSdkError(e, { type: "sdk" }, "destroy")
        hooks.onError?.(sdk, error)
      }
      // 清理注册表与消息订阅
      registry.clear()
      messageBus.clear()
    },

    on(event: string, callback: (payload: unknown) => void): void {
      messageBus.on(event, callback)
    },
    off(event: string, callback?: (payload: unknown) => void): void {
      messageBus.off(event, callback)
    },
    emit(event: string, payload?: unknown): void {
      messageBus.emit(event, payload)
    },

    inspect(): SdkSnapshot {
      return {
        state,
        namespace,
        debug,
        registeredTypes: registry.listTypes(namespace),
      }
    },
  }

  // 创建事件
  emit("sdk:created")
  hooks.onCreate?.(sdk)

  return sdk
}
