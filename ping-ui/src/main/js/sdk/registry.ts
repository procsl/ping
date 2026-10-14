/**
 * 处理器注册表 —— 对应 `ping-ui/doc/sdk-design.md` §2.3 与 design D5。
 *
 * 二维 Map：`Map<namespace, Map<type, handler>>`，查找顺序「当前命名空间 → default」。
 * 同一命名空间内重复注册同一 type 且未声明 override → 拒绝并 `console.warn`，**不抛异常**——
 * 与仓库「降级 + 告警，启动永不失败」的不变量一致，注册冲突属配置错误而非异常状态。
 *
 * `error_handler` 是保留名（design D6）：注册进同一张表，但按节点 type 查找时排除，
 * 不可被当作可渲染 type 命中；错误处理路径单独经 `getErrorHandler` 取用。
 */

import type { ComponentNode, Handler, ErrorHandler, RegisterOptions } from "./types"
import type { MessageBus } from "./message-bus"
import type { SdkError } from "./sdk-error"
import type { PingUi } from "./types"

/** 保留名：默认全局错误处理器，不可被当作可渲染 type 命中 */
export const ERROR_HANDLER_TYPE = "error_handler"

/** 内部普通处理器签名：`(tree, node, messageBus, sdk) => unknown`，包装后返回 undefined */
type InternalHandler = (
  tree: ComponentNode,
  node: ComponentNode,
  messageBus: MessageBus,
  sdk: PingUi,
) => unknown

/** 内部错误处理器签名：第一个参数传 SdkError，返回降级形态 */
type InternalErrorHandler = (
  error: SdkError,
  node: ComponentNode,
  messageBus: MessageBus,
  sdk: PingUi,
) => unknown

/** 注册表存储的条目：普通处理器或错误处理器 */
type Entry = InternalHandler | InternalErrorHandler

export class Registry {
  private table = new Map<string, Map<string, Entry>>()

  /**
   * 注册处理器。
   * @returns 是否注册成功；重复注册且未声明 override 时返回 false 并告警
   */
  register(
    type: string,
    handler: Handler | ErrorHandler,
    options?: RegisterOptions,
  ): boolean {
    const ns = options?.namespace ?? "default"
    const override = options?.override ?? false

    let nsMap = this.table.get(ns)
    if (!nsMap) {
      nsMap = new Map()
      this.table.set(ns, nsMap)
    }

    if (nsMap.has(type) && !override) {
      console.warn(
        `[ping-ui] 处理器 "${type}" 已在命名空间 "${ns}" 注册，本次注册被拒绝（如需覆盖请声明 override: true）`,
      )
      return false
    }

    nsMap.set(type, this.wrap(type, handler))
    return true
  }

  /** 注销处理器；可精确到命名空间下的一条 */
  unregister(type: string, options?: RegisterOptions): void {
    const ns = options?.namespace ?? "default"
    const nsMap = this.table.get(ns)
    if (!nsMap) {
      return
    }
    nsMap.delete(type)
    if (nsMap.size === 0) {
      this.table.delete(ns)
    }
  }

  /** 当前命名空间未命中时回退 default 命名空间；error_handler 保留名恒为 false */
  has(type: string, namespace: string): boolean {
    return this.get(type, namespace) !== undefined
  }

  /**
   * 查询处理器（按「当前命名空间 → default」回退）。
   * 返回的 Handler 已包装为 4 参数签名（调用时补 messageBus 与 sdk）。
   * error_handler 保留名不可被当作可渲染 type 命中，恒返回 undefined。
   */
  get(type: string, namespace: string): Handler | undefined {
    if (type === ERROR_HANDLER_TYPE) {
      return undefined
    }
    const entry = this.lookup(type, namespace)
    if (!entry) {
      return undefined
    }
    // error_handler 条目在 get 路径不可达（上面已排除），此处只可能是普通处理器
    const internal = entry as InternalHandler
    return (tree, node, messageBus, sdk) => {
      internal(tree, node, messageBus, sdk)
    }
  }

  /**
   * 取错误处理器（内部签名，第一个参数传 SdkError）。
   * 按「当前命名空间 → default」回退；未注册自定义实现时返回 undefined，
   * 由调用方决定是否使用内置默认降级。
   */
  getErrorHandler(namespace: string): InternalErrorHandler | undefined {
    const entry = this.lookup(ERROR_HANDLER_TYPE, namespace)
    return entry as InternalErrorHandler | undefined
  }

  /** 当前命名空间下已注册的 type 列表（inspect 用） */
  listTypes(namespace: string): string[] {
    const nsMap = this.table.get(namespace)
    return nsMap ? [...nsMap.keys()] : []
  }

  /** 清空全部注册（销毁时调用） */
  clear(): void {
    this.table.clear()
  }

  /** 按「当前命名空间 → default」查找内部处理器 */
  private lookup(type: string, namespace: string): Entry | undefined {
    const current = this.table.get(namespace)?.get(type)
    if (current) {
      return current
    }
    if (namespace === "default") {
      return undefined
    }
    return this.table.get("default")?.get(type)
  }

  /** 统一包装为内部签名：普通 Handler 丢弃返回值，error_handler 透传降级形态 */
  private wrap(type: string, handler: Handler | ErrorHandler): Entry {
    if (type === ERROR_HANDLER_TYPE) {
      const errorHandler = handler as ErrorHandler
      return (
        error: SdkError,
        node: ComponentNode,
        messageBus: MessageBus,
        sdk: PingUi,
      ) => errorHandler(error, node, messageBus, sdk)
    }
    const normalHandler = handler as Handler
    return (
      tree: ComponentNode,
      node: ComponentNode,
      messageBus: MessageBus,
      sdk: PingUi,
    ) => {
      normalHandler(tree, node, messageBus, sdk)
    }
  }
}
