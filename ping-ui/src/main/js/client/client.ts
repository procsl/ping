/**
 * 组装层门面：ComposeClient（design D1/D7）。
 *
 * - create：跑 bootstrap 链 [source, indexer] 取树建索引，失败即 reject（无半初始化）
 * - fetch：seed ctx → 组链 [locate, bind, ...业务hooks, http] → 兑现链冒泡值 / reject
 * - addHandler：业务环插槽固定 bind 之后 http 之前（贴近终点：可读 operation/request、
 *   可改写 request、可短路），多次调用顺序 = 执行顺序
 * - tree：只读暴露（deep freeze），与索引同源（同一次 bootstrap 加载结果）
 */

import { bind } from "./handlers/bind"
import { http } from "./handlers/http"
import { indexer } from "./handlers/indexer"
import { locate } from "./handlers/locate"
import { source } from "./handlers/source"
import { compose, type Handler } from "./pipeline"
import type {
  ComponentNode,
  ComposeClientOptions,
  Context,
  FetchInit,
  OperationIndex,
} from "./types"

export class ComposeClient {
  private readonly handlers: Handler[] = []

  private constructor(
    private readonly index: OperationIndex,
    private readonly send?: ComposeClientOptions["fetch"],
    /** 已加载功能树（只读，与索引同源）；裸文档输入时无树语义，为 undefined */
    readonly tree?: ComponentNode,
  ) {}

  /** 创建：bootstrap 链取树建索引，失败即 reject（错误携带失败原因） */
  static async create(options: ComposeClientOptions = {}): Promise<ComposeClient> {
    const ctx: Context = {}
    await compose([source(options.treeSource, options.fetch), indexer()])(ctx)
    // 成功到达此处：ctx.index 必已建立（indexer 缺索引即 throw）
    const tree = ctx.tree === undefined ? undefined : deepFreeze(ctx.tree)
    return new ComposeClient(ctx.index as OperationIndex, options.fetch, tree)
  }

  /** 注册业务环（插槽：bind 之后 http 之前；顺序 = 注册顺序） */
  addHandler(handler: Handler): void {
    this.handlers.push(handler)
  }

  /** 按 operationId 调用：定位 → 绑定 → 业务环 → HTTP，兑现数据 / reject ApiError */
  async fetch<T = unknown>(operationId: string, init?: FetchInit): Promise<T> {
    const ctx: Context = {
      operationId,
      params: init?.params,
      signal: init?.signal,
    }
    const chain: Handler[] = [
      locate(this.index),
      bind(),
      ...this.handlers,
      http(this.send),
    ]
    return (await compose(chain)(ctx)) as T
  }
}

/** 深冻结：消费方修改暴露的树不影响内部状态（spec 功能树只读） */
function deepFreeze<T>(value: T): T {
  if (typeof value !== "object" || value === null || Object.isFrozen(value)) {
    return value
  }
  Object.freeze(value)
  for (const child of Object.values(value as Record<string, unknown>)) {
    deepFreeze(child)
  }
  return value
}
