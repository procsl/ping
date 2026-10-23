/**
 * 架子环②：建索引（bootstrap 链次环，design D2）。
 *
 * 嗅探：树特征（type / children）优先 → 裸 openapi 文档（paths 对象）→ 非法输入拒绝。
 * 树输入：走 app > page > function 抽取 function.properties 内联 operation；
 * 裸文档输入：从 paths 提取各 method 的 operationId（补 method/path），不填 ctx.tree
 * （裸文档无树语义，仅为工具/测试副产品）。
 * 输入信任：operationId 唯一性由后端编译期保证，不设重复/坏树容错。
 */

import type { Handler } from "../pipeline"
import type { ComponentNode, OperationDescriptor, OperationIndex } from "../types"

const HTTP_METHODS = new Set([
  "get", "put", "post", "delete", "options", "head", "patch", "trace",
])

/** 组装 indexer 环 */
export function indexer(): Handler {
  return async (ctx, next) => {
    const raw = ctx.raw
    if (!isRecord(raw)) {
      throw new Error("树源数据必须是 JSON 对象")
    }

    if (isTreeShape(raw)) {
      const node = raw as unknown as ComponentNode
      const index = new Map<string, OperationDescriptor>()
      collectTree(node, index)
      ctx.tree = node
      ctx.index = index
    } else if (isBareOpenApi(raw)) {
      ctx.index = collectBare(raw)
    } else {
      throw new Error(
        "无法识别的树源数据：既不是功能树（含 type/children），也不是裸 openapi 文档（含 paths 对象）",
      )
    }
    return next()
  }
}

/** 树特征：有字符串 type 或 children 数组 */
function isTreeShape(raw: Record<string, unknown>): boolean {
  return typeof raw.type === "string" || Array.isArray(raw.children)
}

/** 裸 openapi：paths 存在且为（非数组）对象 */
function isBareOpenApi(raw: Record<string, unknown>): boolean {
  return isRecord(raw.paths)
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value)
}

/** 树抽取：function 节点的 properties 内联 operation 进索引 */
function collectTree(
  node: ComponentNode,
  index: Map<string, OperationDescriptor>,
): void {
  if (node.type === "function" && isRecord(node.properties)) {
    const operation = node.properties as unknown as OperationDescriptor
    if (typeof operation.operationId === "string" && operation.operationId !== "") {
      index.set(operation.operationId, operation)
    }
  }
  node.children?.forEach((child) => collectTree(child, index))
}

/** 裸文档抽取：paths 项内各 HTTP 方法的 operation 进索引（补 method/path） */
function collectBare(doc: Record<string, unknown>): OperationIndex {
  const index = new Map<string, OperationDescriptor>()
  const paths = doc.paths as Record<string, unknown>
  for (const [path, item] of Object.entries(paths)) {
    if (!isRecord(item)) continue
    for (const [method, operation] of Object.entries(item)) {
      if (!HTTP_METHODS.has(method.toLowerCase()) || !isRecord(operation)) continue
      const operationId = operation.operationId
      if (typeof operationId !== "string" || operationId === "") continue
      index.set(operationId, {
        ...operation,
        operationId,
        method: method.toUpperCase(),
        path,
      } as OperationDescriptor)
    }
  }
  return index
}
