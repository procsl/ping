/**
 * 错误封装 —— 对应 `ping-ui/doc/sdk-design.md` §6.2。
 *
 * SDK 只做两件事：捕获处理器抛错 → 包装为 `SdkError` 交给 `error_handler`。
 * 告警不抛错，与仓库「降级 + 告警，启动永不失败」的不变量一致。
 */

/** 错误发生的生命周期阶段 */
export type SdkPhase = "run" | "update" | "destroy"

/** 封装后的错误对象：含节点标识、type 与阶段，供 error_handler 与错误钩子消费 */
export interface SdkError {
    /** 出错节点的 id（节点无 id 时缺省） */
    nodeId?: string
    /** 出错节点的 type */
    type: string
    /** 出错阶段：run | update | destroy */
    phase: SdkPhase
    /** 错误消息 */
    message: string
    /** 堆栈（捕获自原始异常） */
    stack?: string
    /** 原始异常 */
    cause?: unknown
}

/**
 * 把任意抛错包装为 `SdkError`。
 * 已封装过的错误原样返回，避免二次包装丢失字段。
 */
export function toSdkError(
    error: unknown,
    node: { type: string; id?: string },
    phase: SdkPhase,
): SdkError {
    if (isSdkError(error)) {
        return error
    }
    const message =
        error instanceof Error ? error.message : String(error ?? "unknown error")
    return {
        nodeId: node.id,
        type: node.type,
        phase,
        message,
        stack: error instanceof Error ? error.stack : undefined,
        cause: error,
    }
}

function isSdkError(value: unknown): value is SdkError {
    return (
        typeof value === "object" &&
        value !== null &&
        typeof (value as SdkError).type === "string" &&
        typeof (value as SdkError).message === "string"
    )
}
