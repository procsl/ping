# Ping UI Web SDK 设计

> 模块：`ping-ui`（基础层）｜ 状态：**设计稿（待评审）**
> 本文是 `ping-ui` 文档的**主文档**：定义 Web 端抽象组件 SDK 的架构。
> SDK **不负责渲染 UI**（UI 由 shadcn 等具体框架承担），只提供：**组件 type 处理器注册**、
> **JSON 描述 → 渲染**、**消息通知机制**。错误处理、调试等能力以 buildin 无 UI 组件注入。
>
> 配套文档：`ping-ui/doc/abstract-component-schema.md` —— 节点模型、18 个组件类型定义、
> `compose.json` 编写规范。SDK 只约定节点的 5 个通用字段，具体类型语义全部归该规范。

---

## 1. 设计目标

| 目标 | 说明 |
|---|---|
| 不渲染 UI | SDK 输入抽象 UI 的 JSON 描述，交给对应 type 的处理器渲染 |
| 注册处理器 | 注册组件 type 的处理器（handler），渲染由处理器完成 |
| 运行入口 | 注册完成后返回一个对象，传入 json 描述 + 消息对象即开始运行 |
| 消息通知 | 提供消息注册与通知机制（消息组件暂未定义，SDK 只提供机制） |
| 提供机制 | 错误处理、调试等以 buildin 无 UI 组件注入 |

**非目标：**

- 不做 DOM 操作、不绑定任何 UI 框架。
- 不做组件通信总线协议（消息通知只做 `on/off/emit` 机制）。
- 不做动态远程加载（留扩展点）。

---

## 2. 核心概念

### 2.1 组件节点（ComponentNode）

输入是抽象 UI 的 JSON 描述，与 `compose.json` 对齐，节点模型见
`abstract-component-schema.md` §2。SDK 只约定 5 个通用字段，其余字段透传：

```ts
interface ComponentNode {
  id?: string
  type: string
  name?: string
  children?: ComponentNode[]
  properties?: Record<string, unknown>
  [key: string]: unknown
}
```

节点上**不出现**这 5 个字段以外的字段 —— `router`、`order`、`api`、`widget` 等
组件特有属性一律在 `properties` 里。SDK 对 `properties` 的内容**不做校验**，
语义校验归各类型处理器与规范文档。

### 2.2 处理器（Handler）

一个处理器 = 一个「处理指定 type 节点」的函数。**内部的渲染由对应的处理器完成**，
SDK 不关心处理器如何渲染（可以是 shadcn 组件、DOM 操作、任意实现）。

处理器接收 4 个参数，**无返回值**：

```ts
type Handler = (
  tree: ComponentNode,      // 总的 json 描述（整棵树）
  node: ComponentNode,      // 当前对象 json 描述
  messageBus: MessageBus,   // 负责消息通知的对象（注册后自动注入）
  sdk: PingUi,              // sdk 实例本身（注册时可以自动注入）
) => void
```

**自动注入**：`messageBus` 与 `sdk` 由 SDK 在调用时自动注入，处理器定义时无需
显式接收 —— 调用者只需提供 `tree` 与 `node`。

**`create` 返回的 Handler**：由 `create` 创建时注入 `tree` 与 `messageBus`，
调用时**只需传入当前节点的 json 描述**：

```ts
type CreatedHandler = (node: ComponentNode) => void
```

**处理器自己注册**：处理器模块在加载时调用 `sdk.register(type, handler)`。

### 2.3 注册表（Registry）

注册主要是注册组件 type 的处理器：

```ts
interface Registry {
  register(type: string, handler: Handler, options?: RegisterOptions): void
  unregister(type: string): void
  has(type: string): boolean
  get(type: string): Handler | undefined
}

interface RegisterOptions {
  /** 命名空间，默认 "default"；同名不同命名空间互不覆盖 */
  namespace?: string
  /** 是否允许覆盖已注册的处理器，默认 false */
  override?: boolean
}
```

**注册规则：**

- 同一命名空间内，`type` 重复注册且未声明 `override` → 拒绝并告警。
- 查询按「当前命名空间 → default」回退。
- 注销后再次渲染该类型 → 走未注册降级（见 §5）。

---

## 3. 运行流程（run）

注册完成后，SDK 返回一个对象。**当调用 `run` 时，开始运行**：

```ts
const sdk = createPingUi({ namespace: "app" })

// 注册处理器（组件自己注册）
sdk.register("menu", (tree, node, messageBus, sdk) => renderMenu(tree, node, messageBus, sdk))

// 传入 json 描述 + 消息通知对象 → 开始运行
sdk.run(tree, messageBus)
```

**`run(tree, messageBus)` 传两个参数：**

- `tree`：总的 json 描述（整棵树）。
- `messageBus`：负责消息通知的对象。

**运行流程：**

1. **初始化所有已注册的处理器**。
2. 通过 `tree.type` 查询注册表，找到对应的 handler。
3. 命中 → 执行该 handler，自动注入 `(tree, node, messageBus, sdk)`，处理器完成渲染，无返回值。
4. 未命中 → 走未注册降级（`error_handler` 机制，见 §6）。

**`create(tree, messageBus)` 返回 Handler：**

```ts
// create 创建时注入 tree 与 messageBus，返回 Handler
const handler = sdk.create(tree, messageBus)

// 调用时只需传入当前节点的 json 描述，tree / messageBus / sdk 均已注入
handler({ type: "menu", id: "m1", name: "用户管理" })
```

**渲染结果**：处理器无返回值，渲染效果由处理器自行产生（shadcn 配置、DOM 操作、
任意实现），SDK 不约束。

---

## 4. 消息通知

SDK 提供消息注册与通知机制，**消息组件暂未定义** —— 目前只保留一个空定义，
具体消息语义由未来的消息组件定义。

```ts
interface MessageBus {
  // 待定义
}
```

---

## 5. 生命周期

SDK 实例生命周期：

```
create ──▶ init ──▶ running ──▶ destroy
              │         │
              │         ├──▶ run（每次运行，由处理器完成）
              │         └──▶ create（传入 node，返回 Handler）
```

| 阶段 | 方法 | 说明 | 事件 |
|---|---|---|---|
| 创建 | `createPingUi(options)` | 创建实例，返回 SDK 对象 | `sdk:created` |
| 初始化 | `init()` | 初始化：注册默认 buildin 组件、准备内部状态 | `sdk:initialized` |
| 运行 | `run(tree, messageBus)` | 初始化所有处理器，执行 tree.type 对应的 handler | `sdk:run` |
| 运行 | `create(tree, messageBus)` | 注入 tree 与消息对象，返回 Handler | `component:created` |
| 运行 | `update(tree)` | 整树替换（可选） | `sdk:updated` |
| 销毁 | `destroy()` | 注销并销毁：清理注册表、移除订阅、触发销毁钩子（幂等） | `sdk:destroyed` |

**钩子：**

```ts
interface SdkHooks {
  onCreate?(sdk: PingUi): void
  onInit?(sdk: PingUi): void
  onRun?(sdk: PingUi, tree: ComponentNode): void
  onComponentCreate?(sdk: PingUi, tree: ComponentNode, messageBus: MessageBus, handler: CreatedHandler): void
  onError?(sdk: PingUi, error: SdkError): void
  onDestroy?(sdk: PingUi): void
}
```

---

## 6. 机制（buildin 组件注入）

错误处理、调试等能力**不内置实现**，而是以**无 UI 组件**（buildin）注入 ——
SDK 只提供注册与调用机制，具体处理逻辑由 buildin 组件承担。

### 6.1 默认全局组件：`error_handler`

SDK 内置一个**默认的全局组件** `error_handler`，职责：

- **打印错误**：捕获渲染过程中的错误并输出到控制台。
- **防止崩溃**：单组件渲染失败不拖垮整棵树，降级为占位。

`error_handler` 是**默认注册**的，**可以被覆盖**：

```ts
// 覆盖默认 error_handler
sdk.register("error_handler", myErrorHandler, { override: true })
```

### 6.2 错误处理机制

SDK 只做两件事，其余交给 `error_handler`：

1. **捕获**：渲染过程中处理器抛错 → SDK 捕获，包装为 `SdkError`
   （含 `nodeId`、`type`、`phase`、堆栈），交给 `error_handler` 处理。
2. **降级**：未注册类型 / 渲染失败 → 由 `error_handler` 决定如何降级。

```ts
interface SdkError {
  nodeId?: string
  type: string
  phase: "run" | "update" | "destroy"
  message: string
  stack?: string
  cause?: unknown
}
```

> **关键**：单组件渲染失败**不影响其他组件** —— SDK 逐个渲染，错误只影响
> 当前节点，`error_handler` 决定该节点降级为何物。

### 6.3 调试机制

SDK 提供 `debug: boolean` 开关与 `sdk.inspect()` 快照接口，具体日志输出
由 `logger` buildin 组件实现（后续定义）。

---

## 7. 公共 API 一览

```ts
import { createPingUi } from "@ping-ui/sdk"

// 创建 SDK 实例，返回一个对象
const sdk = createPingUi({
  namespace: "app",
  debug: true,
})

// 初始化（注册默认 buildin 组件，如 error_handler）
sdk.init()

// 注册组件 type 的处理器（组件自己注册，4 参数自动注入）
sdk.register("menu", (tree, node, messageBus, sdk) => renderMenu(tree, node, messageBus, sdk))
sdk.register("nav_group", navGroupHandler)

// 覆盖默认 error_handler
sdk.register("error_handler", myErrorHandler, { override: true })

// 传入 json 描述 + 消息通知对象 → 初始化所有处理器并执行 tree.type 对应的 handler
sdk.run(tree, messageBus)

// create 创建时注入 tree 与 messageBus → 返回 Handler
const handler = sdk.create(tree, messageBus)
// 调用时只需传入当前节点的 json 描述
handler({ type: "menu", id: "m1", name: "用户管理" })

// 销毁（注销并销毁，幂等）
sdk.destroy()
```

---

## 8. 与现有代码的关系

现有 `registry.tsx` / `RouterOutlet.tsx` 是 React 绑定实现。重构方向：

1. SDK 核心（注册表 + 渲染 + 消息 + 生命周期 + 错误机制）框架无关，独立成包。
2. 现有 React 渲染器**暂不动**，SDK 先行落地，后续再决定是否迁移。
3. 默认 `error_handler` 组件作为无 UI 处理器注入，可被覆盖。

**迁移状态**：前端代码当前仍是**扁平节点模型**（类型专有字段平铺在节点上，
子节点键为 `containers`）。SDK 落地时按本文 §2.1 的 `children` + `properties`
模型实现，代码侧的迁移清单见 `abstract-component-schema.md` §11。
两份文档描述的都是**目标态**，与当前代码存在已知差异。

---

## 9. 待协商问题

| # | 问题 | 倾向方案 |
|---|---|---|
| A | Handler 参数个数 | 4 个：`(tree, node, messageBus, sdk)`，后两个自动注入 |
| B | 命名空间粒度 | 按「命名空间 + type」两级 |
| C | `run(tree, messageBus)` | 初始化所有处理器，执行 tree.type 对应的 handler |
| D | `create(tree, messageBus)` 返回的 Handler | 注入 tree 与消息对象，调用时只需传入当前节点 json |
| E | `error_handler` 的降级形态 | 默认返回 null / 占位对象，可覆盖 |
| F | 包形态 | 单包 `@ping-ui/sdk` |
| G | 现有 React 渲染器去留 | 暂保留，SDK 先行，后续再定 |

---

## 10. 里程碑

| 阶段 | 内容 | 产出 |
|---|---|---|
| M1 | SDK 核心（注册表/run/create/消息/生命周期/错误机制） | `@ping-ui/sdk` |
| M2 | 默认 `error_handler` 组件 | 无 UI 组件 |
| M3 | 文档 + 测试 | 使用手册、单测 |
