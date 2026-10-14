/**
 * 抽象组件节点模型 —— 对应 `ping-ui/doc/abstract-component-schema.md` §2.1 与 §2.2。
 *
 * §2.1：每个组件节点只保留 5 个通用字段，组件特有字段一律放入 `properties` 对象。
 * §2.2：`properties` 在「类型定义」与「组件实例」中都出现但含义不同——
 *   - 类型定义（`types/<type>.json`）里的 `properties` 是**属性模式声明**；
 *   - 组件实例节点上的 `properties` 是**实际取值**（键值对象，值是任意 JSON 值）。
 * SDK 对 `properties` 的内容不做校验，语义校验归各类型处理器与规范文档。
 */

import type { MessageBus } from "./message-bus"
import type { SdkError } from "./sdk-error"

/** 抽象组件节点：5 通用字段 + 索引签名（其余字段透传，SDK 不消费） */
export interface ComponentNode {
  /** 组件类型，前端按此查渲染注册表；缺失时该节点及子树不进入注册表 */
  type: string
  /** 全树唯一 Key，用于 React key、去重、元素定位 */
  id?: string
  /** 展示文案 / 标题；缺失时前端回落 id，id 也缺失时回落 type */
  name?: string
  /** 子节点数组；子节点顺序不作为语义，同级顺序由 properties.order 决定 */
  children?: ComponentNode[]
  /** 组件特有字段集合（实际取值，见文件头 §2.2 说明） */
  properties?: Record<string, unknown>
  [key: string]: unknown
}

/**
 * 处理器 —— 对应 `ping-ui/doc/sdk-design.md` §2.2。
 *
 * 接收 4 个参数，无返回值；`messageBus` 与 `sdk` 由 SDK 调用时自动注入，
 * 处理器定义时只需写前两个参数。
 */
export type Handler = (
  tree: ComponentNode,
  node: ComponentNode,
  messageBus: MessageBus,
  sdk: PingUi,
) => void

/**
 * 预绑定处理器 —— 由 `create(tree, messageBus)` 创建时注入 tree 与 messageBus，
 * 调用时只需传入当前节点的 json 描述。
 */
export type CreatedHandler = (node: ComponentNode) => void

/**
 * 错误处理器 —— 对应 `ping-ui/doc/sdk-design.md` §6.1。
 *
 * 与普通 Handler 不同：接收封装后的 `SdkError`，返回降级形态（占位对象等）。
 * 以 `register("error_handler", handler, { override: true })` 注册，可被覆盖。
 */
export type ErrorHandler = (
  error: SdkError,
  node: ComponentNode,
  messageBus: MessageBus,
  sdk: PingUi,
) => unknown

/** 实例生命周期状态（design D8：显式状态机） */
export type SdkState = "created" | "initialized" | "running" | "destroyed"

/** 生命周期钩子 —— 对应 `sdk-design.md` §5 */
export interface SdkHooks {
  /** 创建实例后触发 */
  onCreate?(sdk: PingUi): void
  /** 初始化后触发 */
  onInit?(sdk: PingUi): void
  /** 每次运行前触发 */
  onRun?(sdk: PingUi, tree: ComponentNode): void
  /** 预绑定处理器创建后触发 */
  onComponentCreate?(
    sdk: PingUi,
    tree: ComponentNode,
    messageBus: MessageBus,
    handler: CreatedHandler,
  ): void
  /** 产生封装后的错误时触发 */
  onError?(sdk: PingUi, error: SdkError): void
  /** 销毁后触发 */
  onDestroy?(sdk: PingUi): void
}

/** `createPingUi` 的选项 */
export interface PingUiOptions {
  /** 命名空间，默认 "default"；查询按「当前命名空间 → default」回退 */
  namespace?: string
  /** 调试开关：控制 SDK 自身控制台输出 */
  debug?: boolean
  /** 生命周期钩子 */
  hooks?: SdkHooks
}

/** 注册表快照（`inspect()` 返回） */
export interface SdkSnapshot {
  state: SdkState
  namespace: string
  debug: boolean
  /** 当前命名空间下已注册的 type 列表 */
  registeredTypes: string[]
}

/**
 * SDK 实例 —— 对应 `sdk-design.md` §7 公共 API。
 * 由 `createPingUi(options)` 创建。
 */
export interface PingUi {
  /** 命名空间 */
  readonly namespace: string
  /** 调试开关 */
  readonly debug: boolean
  /** 当前生命周期状态 */
  readonly state: SdkState

  /** 注册组件 type 的处理器；返回是否注册成功 */
  register(
    type: string,
    handler: Handler | ErrorHandler,
    options?: RegisterOptions,
  ): boolean
  /** 注销处理器；可精确到命名空间下的一条 */
  unregister(type: string, options?: RegisterOptions): void
  /** 当前命名空间未命中时回退 default 命名空间 */
  has(type: string): boolean
  /** 查询处理器；`error_handler` 保留名不可被当作可渲染 type 命中 */
  get(type: string): Handler | undefined

  /** 初始化：注册默认 buildin 组件（error_handler）、准备内部状态 */
  init(): void
  /** 运行：按 tree.type 命中处理器执行；created 态自动补 init() 并告警 */
  run(tree: ComponentNode, messageBus?: MessageBus): void
  /** 创建预绑定处理器（注入 tree 与 messageBus），子节点派发由处理器驱动 */
  create(tree: ComponentNode, messageBus?: MessageBus): CreatedHandler
  /** 整树替换 + 重新运行，触发 sdk:updated */
  update(tree: ComponentNode): void
  /** 销毁：清理注册表与消息订阅，幂等 */
  destroy(): void

  /** 消息通知机制（代理内部 MessageBus） */
  on(event: string, callback: (payload: unknown) => void): void
  off(event: string, callback?: (payload: unknown) => void): void
  emit(event: string, payload?: unknown): void

  /** 注册表与状态快照（debug 用） */
  inspect(): SdkSnapshot
}

/** 注册选项 —— 对应 `sdk-design.md` §2.3 */
export interface RegisterOptions {
  /** 命名空间，默认 "default"；同名不同命名空间互不覆盖 */
  namespace?: string
  /** 是否允许覆盖已注册的处理器，默认 false */
  override?: boolean
}
