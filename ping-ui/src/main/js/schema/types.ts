/**
 * 抽象组件树 schema（与 openspec 变更 abstract-component-rendering 的节点模型对齐）
 * 定稿来源：ping-ui/doc/design.md + components/ui.json
 */

export interface ApiRef {
  method: "GET" | "POST" | "PUT" | "DELETE" | "PATCH"
  api: string
}

export interface ComponentNode {
  /** 组件类型，渲染器按 type 查内置注册表，未命中走动态加载 */
  type: string
  /** 唯一 Key */
  id?: string
  /** 组件名称 / 展示文案 */
  name?: string
  /** 布局方位，layout 节点专用：left | right | top | bottom */
  layout?: string
  /** 菜单路由，相对模块前缀，形如 system/user → 前端路径 /system/user */
  router?: string
  /** 同级排序 */
  order?: number
  /** 描述 */
  description?: string
  /** 后端 API 引用（构建期绑定完整定义，运行时按此加载数据） */
  apis?: ApiRef[]
  /** 子节点 */
  containers?: ComponentNode[]
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
