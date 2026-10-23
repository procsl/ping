/**
 * 错误契约：ApiError（design D5）。
 *
 * 非 2xx / 网络失败 / 解析失败一律 reject ApiError；
 * message 自响应体 MessageVO 形态提取，缺失回落状态文本；
 * fieldErrors 为 ParameterErrorVO 形态的 errors 内容原样透出（不加工）。
 */

export interface ApiErrorInit {
  status: number
  operationId: string
  /** HTTP 状态文本（Response.statusText），message 的第一回落 */
  statusText?: string
  /** 响应体原文（MessageVO / ParameterErrorVO 形态） */
  body?: unknown
  /** 底层失败原因（网络失败/解析失败） */
  cause?: unknown
  /** 直接指定 message（如未知 operationId 场景），优先于响应体提取 */
  message?: string
}

export class ApiError extends Error {
  /** HTTP 状态码；网络失败 = 0 */
  readonly status: number
  readonly operationId: string
  /** 字段级错误原样内容（ParameterErrorVO.errors，形态由后端决定不加工）；缺失为空 */
  readonly fieldErrors?: unknown

  constructor(init: ApiErrorInit) {
    const { status, operationId, statusText, body, cause } = init
    const message =
      init.message ??
      extractMessage(body) ??
      statusText ??
      `HTTP ${status}`
    super(message, cause !== undefined ? { cause } : undefined)
    this.name = "ApiError"
    this.status = status
    this.operationId = operationId
    this.fieldErrors = extractFieldErrors(body)
  }
}

/** 从响应体提取 message（仅认 string，其他类型视为缺失） */
function extractMessage(body: unknown): string | undefined {
  if (isRecord(body) && typeof body.message === "string" && body.message !== "") {
    return body.message
  }
  return undefined
}

/** 从响应体提取字段级错误（ParameterErrorVO.errors 数组原样，不加工） */
function extractFieldErrors(body: unknown): unknown {
  if (isRecord(body) && Array.isArray(body.errors)) {
    return body.errors
  }
  return undefined
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value)
}
