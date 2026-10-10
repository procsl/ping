/**
 * 抽象组件树 schema，与 spec（openspec/specs/ui-component-schema/spec.md）
 * 及规范手册（ping-ui/doc/abstract-component-schema.md）的节点模型对齐。
 * 服务端事实来源是构建期聚合索引 META-INF/ping/ui/components.json。
 */

/** 后端 HTTP 方法 */
export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH"

/** 入参 / 出参的结构描述（来自 OpenAPI operation 展平后的 schema） */
export interface ApiSchema {
  type?: string
  properties?: Record<string, unknown>
  required?: string[]
  [key: string]: unknown
}

/** 组件的输入参数定义 —— 由 request.schema 描述 */
export interface ApiRequest {
  content_type?: string
  schema?: ApiSchema
}

/** 组件的渲染数据契约 —— 由 response.schema 描述 */
export interface ApiResponse {
  status?: string
  content_type?: string
  schema?: ApiSchema
}

/**
 * 装配描述中的接口引用 `@ResourceReference:<name>` 经后端启动期解析后的完整描述。
 * 解析依赖打包期导出的 OpenAPI 文档；文档缺失或引用未命中时保持原样字符串下发。
 */
export interface ApiRef {
  method: HttpMethod
  path: string
  request?: ApiRequest
  response?: ApiResponse
}

/** 取出已解析的接口描述；未解析（仍是引用字符串）时返回 undefined */
export function resolvedApi(node: ComponentNode): ApiRef | undefined {
  const api = node.api
  return api && typeof api === "object" ? api : undefined
}

export interface ComponentNode {
  /** 组件类型，渲染器按 type 查内置注册表，未命中降级为占位节点 */
  type: string
  /** 唯一 Key */
  id?: string
  /** 组件名称 / 展示文案 */
  name?: string
  /** 布局方位，layout 节点专用：left | right | top | bottom | center */
  layout?: string
  /** 菜单路由，相对模块前缀，形如 system/user → 前端路径 /system/user */
  router?: string
  /** 同级排序 */
  order?: number
  /** 描述 */
  description?: string
  /**
   * 后端 API：装配期由 `@ResourceReference:<name>` 就地替换为完整描述；
   * 未解析时为引用原样字符串（见 resolvedApi）。
   */
  api?: ApiRef | string
  /** 子节点 */
  containers?: ComponentNode[]

  /* ---- 各类型专有属性，见 ping-ui/doc/abstract-component-schema.md ---- */
  /** dataset：行唯一键，默认 id */
  row_key?: string
  /** dataset：是否允许多选行 */
  selectable?: boolean
  /** dataset / form：展示标题，缺省回落 name */
  title?: string
  /** query：是否可折叠 */
  collapse?: boolean
  /** query：初始折叠 */
  collapsed?: boolean
  /** column：取值字段，对应出参 schema 中的键 */
  field?: string
  /** column：是否可排序 */
  sortable?: boolean
  /** column / field：宽度、栅格列数 */
  width?: string | number
  /** column：对齐 left | center | right */
  align?: string
  /** column：格式化提示 date | money | enum */
  format?: string
  /** column：字段值到展示文案的映射 */
  enum?: Record<string, string>
  /** column：冻结列 left | right */
  fixed?: string
  /** row_action / action：二次确认文案 */
  confirm?: string
  /** row_action / action / form_action：危险动作标红 */
  danger?: boolean
  /** action：样式 primary | outline | ghost */
  variant?: string
  /** action：位置 toolbar | header */
  placement?: string
  /** form：create | edit */
  mode?: string
  /** form：字段栅格列数 1-3 */
  cols?: number
  /** form：标签宽度 */
  label_width?: string
  /** field：控件类型 */
  widget?: string
  /** field：标签，缺省回落 name */
  label?: string
  /** field：必填 */
  required?: boolean
  /** field：只读或禁用 */
  disabled?: boolean
  /** field：占位提示 */
  placeholder?: string
  /** field：默认值 */
  default?: unknown
  /** field：选项 */
  options?: { value: string | number; label: string }[]
  /** field：多选 */
  multiple?: boolean
  /** field：正则校验 */
  pattern?: string
  /** field：长度与数值约束 */
  min_length?: number
  max_length?: number
  min?: number
  max?: number
  /** field：跨越的栅格列数 1-3 */
  col_span?: number
  /** form_action：submit | reset | cancel */
  kind?: string
  /** form_action：提交中态文案 */
  loading?: string
  /** breadcrumb：导航项 */
  items?: { name: string; router?: string }[]
  /** tabs：激活页签的 id，缺省取第一个子节点 */
  active?: string
  /** empty：引导按钮文案 */
  action?: string
  /** menu / nav_group：图标 */
  icon?: string
  /** menu：强制外链，新开窗口 */
  external?: boolean
}

export interface MountOptions {
  /** 模块命名空间，薄壳传入，用于命名空间隔离与组件文件寻址 */
  namespace?: string
  /** 组件树接口地址；缺省或加载失败时降级为本地声明 */
  manifest?: string
  /** 挂载容器选择器 */
  selector?: string
}

/** 渲染器加载组件树的数据源：后端接口优先，失败降级本地声明 */
export type ManifestSource = "remote" | "local"

/** GET /v1/ui/components 的响应体 */
export interface ComponentManifest {
  types: unknown[]
  pages: ComponentNode[]
}
