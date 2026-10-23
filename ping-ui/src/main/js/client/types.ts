/**
 * 组装层协议：Context 接口与结构类型（design D4）。
 *
 * 运行时 Context 就是一个空袋子（Koa 惯例）——本文件的类型只在编译期生效，
 * 为业务 Handler 提供入参出参补全；框架执行器（pipeline.ts）不引用任何字段语义。
 */

// ---------------------------------------------------------------------------
// 功能树与内联 operation（镜像 GET /v1/ui/components 编译产物，产物即契约）
// ---------------------------------------------------------------------------

/** 功能树节点：app > page > function；function 无 children、page 无 properties（NON_EMPTY 序列化） */
export interface ComponentNode {
  /** "app" | "page" | "function" */
  type: string
  id: string
  /** function 为内联 openapi operation（见 OperationDescriptor），app/page 可无 */
  properties?: Record<string, unknown>
  children?: ComponentNode[]
}

/** 内联 parameter 声明（openapi parameter 镜像，$ref 已全展开） */
export interface ParameterDescriptor {
  name: string
  /** "path" | "query" | "header" | "cookie" */
  in: string
  required?: boolean
  schema?: Record<string, unknown>
  [key: string]: unknown
}

/** function.properties 的内联 operation 描述（operationId + INLINE_KEYS + responses-2xx + route） */
export interface OperationDescriptor {
  operationId: string
  method: string
  /** 含路径模板段，如 /v1/system/users/{id} */
  path: string
  /** UI 路由便利字段，如 user/list（渲染器消费） */
  route?: string
  parameters?: ParameterDescriptor[]
  requestBody?: Record<string, unknown>
  /** 仅 2xx 状态码（编译期已过滤） */
  responses?: Record<string, unknown>
  [key: string]: unknown
}

/** operationId → operation 索引（bootstrap 链 indexer 环产出） */
export type OperationIndex = ReadonlyMap<string, OperationDescriptor>

// ---------------------------------------------------------------------------
// 注入边界与调用入参
// ---------------------------------------------------------------------------

/** 树源：默认 fetch GET /v1/ui/components；测试/离线注入本地树 */
export type TreeSource = () => Promise<unknown>

export interface ComposeClientOptions {
  treeSource?: TreeSource
  /** HTTP 发送层，默认 globalThis.fetch；测试注入 mock */
  fetch?: typeof globalThis.fetch
}

export interface FetchInit {
  /** 按内联 parameters[].in 自动分拣；未声明参数忽略、缺 path 参数拒绝 */
  params?: Record<string, unknown>
  /** 取消/透传 AbortSignal（AbortError 原样透出，不包装） */
  signal?: AbortSignal
}

// ---------------------------------------------------------------------------
// 请求与响应结构
// ---------------------------------------------------------------------------

export interface ApiRequest {
  method: string
  /** 已完成 path 替换与 query 平铺的完整 URL */
  url: string
  headers: Record<string, string>
  /** 已 JSON 序列化（仅 operation 声明 requestBody 时存在） */
  body?: string
}

/** 响应元数据（http 环 2xx 时填入 ctx.response；数据本体走 return 冒泡通道） */
export interface ResponseMeta {
  status: number
  headers: Record<string, string>
}

// ---------------------------------------------------------------------------
// Context 协议：字段分组 = 谁填谁读（design D4）
// ---------------------------------------------------------------------------

export interface Context {
  // ── fetch 入口填（调用链 seed）──
  operationId?: string
  params?: Record<string, unknown>
  signal?: AbortSignal

  // ── bootstrap 链填（create：source → indexer）──
  raw?: unknown
  tree?: ComponentNode
  index?: OperationIndex

  // ── 调用链填 ──
  /** locate 环填（查表定位）；业务 hook 经 requireOperation 读 */
  operation?: OperationDescriptor
  /** bind 环填（path/query/body 分拣完成）；业务 hook 改写其 headers 等 */
  request?: ApiRequest
  /** http 环 2xx 填（元数据）；上行 hook 可读 */
  response?: ResponseMeta
}

// ---------------------------------------------------------------------------
// require* helper：字段缺失 = 环序被破坏，throw 明确错误（替代 undefined 断言炸）
// ---------------------------------------------------------------------------

export function requireOperation(ctx: Context): OperationDescriptor {
  if (ctx.operation === undefined) {
    throw new Error(
      "ctx.operation 不存在：locate 环未执行。业务 Handler 经 client.addHandler 注册时"
        + "自动位于 locate 之后；若自行 compose 链，请把 locate 放在该环之前",
    )
  }
  return ctx.operation
}

export function requireRequest(ctx: Context): ApiRequest {
  if (ctx.request === undefined) {
    throw new Error(
      "ctx.request 不存在：bind 环未执行。业务 Handler 经 client.addHandler 注册时"
        + "自动位于 bind 之后；若自行 compose 链，请把 bind 放在该环之前",
    )
  }
  return ctx.request
}
