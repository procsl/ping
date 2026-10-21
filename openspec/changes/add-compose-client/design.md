# Design

## Context

`redesign-ping-ui-framework` 落地的编译产物是本设计的核心原料：`GET /v1/ui/components` 返回自包含功能树，`function.properties` 已是 `$ref` 全展开、仅 2xx 的 openapi operation（method/path/parameters/requestBody/responses + route）。swagger-client 类通用客户端需在运行时承担的文档下载、$ref 解析、索引构建，ping 已在启动期完成——本客户端的运行时工作只剩「索引 + 绑定 + 管道」。

前端现状为白纸：14 个 shadcn/ui 组件、`createPingUi` 占位 entry、vite lib 模式产物 `renderer.js`；旧 SDK（处理器注册表/message-bus）已随 redesign 删除，本设计不欠其兼容债。前端测试框架已随 SDK 删除一并消失（package.json 仅剩 build/preview）。动机参见 proposal.md - Why。

## Goals / Non-Goals

**Goals:**

- 客户端核心极度薄：拿到树 → 建索引 → 绑定参数 → 过管道 → fetch，无其他职责
- 所有「聪明行为」（鉴权、加解密、转换、校验、剪枝、缓存）均为注册产物，核心零魔法
- 数据层与视图层单向依赖：UI 组件引入客户端，客户端永不 import React
- 可测性优先：网络（树源 + HTTP 层）是仅有的两个可注入边界，其余为纯逻辑

**Non-Goals:**

- 渲染器（侧栏/路由/function 渲染器/渲染扩展键）——后续渲染器提案
- 登录页与 401 重放登录流程的完整实现——依赖登录 UI，后续提案
- 任何官方 Handler 实现（校验/缓存/加密/转换/剪枝）——后续按需逐个落地
- 对外发布 npm 包——客户端与渲染器同 bundle（renderer.js）交付

## Decisions

### D1 创建形态：静态工厂 + 树源/HTTP 层注入

`await ComposeClient.create(options?)`，内部经 `treeSource`（默认 `fetch('/v1/ui/components')`）取树建索引，失败即 reject。备选：`new` + `init()`（存在「忘了 init 就调用」窗口）；首次调用惰性拉树（错误时机模糊，需 memo in-flight promise）——均否决。`options.treeSource` 与 `options.fetch`（HTTP 层）是仅有的两个注入边界：测试/Node 场景注入纯对象树与 mock HTTP，其余全为纯逻辑。**输入信任**：树结构与 operationId 唯一性由后端编译期保证，客户端不设坏树容错分支（用户裁决——无需考虑坏树情况）。

### D2 索引键：operationId（route 为产物便利字段）

operationId 是 openapi 原生标识，后端已保证全局唯一（断链即启动失败）；route（`page.id/function.id`）仅为 UI 路由便利字段，留待渲染器消费。构造器同时嗅探裸 openapi 文档形态（含 `paths`/`openapi` 键）直接建索引——树抽取是主路径，裸文档是零成本副产品（工具/测试场景）。

### D3 管道：洋葱模型，纯数据流

`Handler.handle(ctx, next)`，注册顺序即执行顺序，短路 = 不调 `next()`。备选钩子模型（before/after/error）：无法表达响应改写等上行逻辑，必然返工；备选事件订阅：只能旁观不能改写，均否决。`ctx` 只携带 `operation` 与请求描述——**副作用事件机制（emit 出口 + 订阅端）整体推后**（用户裁决，聚焦 client 本体），loading/toast 等 UI 通知随渲染器提案设计。**「Handler 直接返回 React 对象」已否决**（拷问结论）：数据管道泄漏视图会迫使核心依赖 React 并污染调用点类型；「特殊渲染」需求归渲染器的注册表扩展点（后续提案），依赖方向恒为 UI → client。

### D4 错误：reject + ApiError 对齐后端错误体

非 2xx/网络失败/解析失败一律 reject `ApiError extends Error`：`status`（网络失败 0）、`operationId`、`message`（响应体 `MessageVO.message` 提取，回落状态文本）、`fieldErrors`（`ParameterErrorVO` 字段级错误原样透出——渲染器表单高亮的对接点）。备选 Result 对象模式（`{ok, data, error}`）：每个调用点须判 ok，成功路径被污染，违背「异常精炼」。

判错与边界（用户裁决，2026-10-21）：

- **2xx 即业务成功**：RESTful 状态码判错，不检查响应体业务错误码；空响应体（204/零长度）兑现 `undefined`，不算解析失败
- **取消原样透出**：`AbortSignal` 触发的 `AbortError` 不包装成 ApiError——调用方可与网络失败（status=0）区分，重试逻辑不误发
- **Handler 内部异常原样传播**：管道抛出的非 ApiError（如 TypeError）不伪装为 HTTP 失败，调用方拿到的是真实 bug

### D5 零魔法三条（已确认）

- **401 不特殊**：核心抛 `ApiError{status:401}`，登录流程归鉴权 Handler（可插拔纯粹性；「不要登录流程的场景」自然成立）。**用户已确认**（2026-10-21）。
- **snake_case 不转换**：兑现数据保持后端字段名原文，转换是注册的 Handler（显式可见，调试时与后端原文对得上）。**用户已确认**（2026-10-21）。
- **未声明参数忽略**：调用多传的参数静默丢弃（前向兼容）；缺失 path 参数则拒绝且不发请求（无法构造 URL）。

### D6 核心零自带 Handler

只交付骨架：Handler 接口、ctx 类型、链执行器、ApiError。加密/缓存等强业务向实现放 core 是负担；字段校验虽吃内联 schema（编译期红利，ajv 原生 JSON Schema 零转换），但 ajv ~200KB 违背瘦核心，推后独立落地。零自带同时是接口纪律：第一个官方 Handler 落地时管道接口才被真实使用方检验。

### D7 产物形态：住 renderer.js bundle

`src/main/js/client/`（纯 TS），与渲染器同 bundle 交付——「UI 组件引入 client」是 bundle 内部 import，无 npm 发布/CDN/externals 复杂度。vite entry（`sdk/index.ts`）与产物文件名不变。

### D8 测试：vitest 恢复 + maven 接入

恢复 `vitest devDependency` + `npm test` script（独立 `vitest.config.ts`，`environment: node`——用户裁决，测试为纯逻辑、Node 环境即真实运行环境），`ping-ui/pom.xml` 的 exec-maven-plugin 增加 test 阶段 `npm run test`（复用 `ping.npm.skip` 开关，与 build 接入同构）。client 是纯逻辑模块，是前端测试密度最高处；无测试的洋葱链等于盲写。

### D9 命名：ComposeClient

「swagger-client」撞官方 npm 包名。`ComposeClient` 与后端词汇族（ComposeCompiler/ComposeRepository/ComposeException）同源——消费 compose 编译产物的客户端，模型概念一眼可归属。

### D10 传输策略细节归 Handler，core 不立契约

超时、baseURL/跨域、请求头定制、非 JSON 内容处理、query 空值与编码策略等传输层细节，**全部不进 core 契约**（用户裁决「B 相关的问题全部由 handler 去解决」）：核心只完成「按内联声明绑定 + 发出」，需要特殊策略时经 `ctx.request` 改写或短路实现。同源绝对路径（`/v1/...`）直发为默认形态，不提供 `baseUrl` 选项（避免与 Handler 改写机制重复开洞）。

## Architecture

```
ping-ui/src/main/js/
  client/                     纯 TS，零 React、零框架依赖
    index.ts                  公共 API 面：汇总导出（渲染器/业务组件只从此 import）
    types.ts                  TreeSource / ComposeClientOptions / OperationDescriptor
                              / ApiRequest / ApiResult / FetchInit 等结构类型
    error.ts                  ApiError（status/operationId/message/fieldErrors）
    source.ts                 默认树源（fetch /v1/ui/components）+ 输入嗅探
                              （树特征优先 → 裸 openapi 文档 → 非法输入拒绝）
    bind.ts                   params 按 parameters[].in 分拣：path 替换 /
                              query 平铺（数组重复键）/ body JSON 序列化
    pipeline.ts               洋葱链执行器：Handler 串接、next 逐层下探、
                              Ctx 构造、最终 HTTP 终点
    client.ts                 ComposeClient：create 静态工厂、operationId 索引、
                              fetch 组装（绑定 → 管道 → HTTP → 兑现/拒绝）、
                              register / readonly tree
  sdk/index.ts                vite entry（不变）：导出衔接 ComposeClient
```

职责边界：`source` 只碰树网络，`pipeline` 只管链，`bind` 是纯函数，`client` 只做组装——每个模块单一职责（DDD 职责清晰），模块间以 `types.ts` 的结构类型为契约。

## Key Interfaces

```ts
// ---- 注入边界 ----
type TreeSource = () => Promise<unknown>          // 默认：fetch('/v1/ui/components')
type HttpSend = typeof globalThis.fetch           // 默认：globalThis.fetch

interface ComposeClientOptions {
  treeSource?: TreeSource
  fetch?: HttpSend                                // 测试/Node 注入 mock HTTP 层
}

// ---- 客户端公共 API 面 ----
interface ComposeClient {
  /** 按 operationId 调用：绑定参数 → 洋葱管道 → HTTP → 兑现数据/拒绝 ApiError */
  fetch<T = unknown>(operationId: string, init?: FetchInit): Promise<T>
  /** 注册洋葱 Handler，注册顺序即执行顺序（越早注册越外层） */
  register(handler: Handler): void
  /** 已加载功能树（只读，与索引同源；渲染器据此画侧栏/路由） */
  readonly tree: ComponentNode
}

interface FetchInit {
  params?: Record<string, unknown>                // 按内联 parameters 自动分拣
  signal?: AbortSignal                            // 取消/超时透传
}

// ---- 洋葱管道 ----
interface Handler {
  handle(ctx: Ctx, next: () => Promise<ApiResult>): Promise<ApiResult>
}

interface Ctx {
  readonly operation: OperationDescriptor          // 定位到的内联 operation
  request: ApiRequest                              // 可改写（下行：header 注入等）
  response?: ApiResult                             // 上行时可用（可改写）
}

interface ApiRequest {
  method: string
  url: string                                      // 已完成 path/query 绑定的完整 URL
  headers: Record<string, string>
  body?: string                                    // 已 JSON 序列化（如声明 requestBody）
}

interface ApiResult {                              // 管道内原始响应（2xx 之外也在）
  status: number
  headers: Record<string, string>
  body: unknown                                    // 已尝试 JSON 解析
}

// ---- 错误契约 ----
class ApiError extends Error {
  status: number                                   // HTTP 状态；网络失败 = 0
  operationId: string
  message: string                                  // body.message 提取，回落状态文本
  fieldErrors?: Record<string, unknown>            // ParameterErrorVO 字段级错误原样
}
```

`OperationDescriptor` 与树节点类型（`ComponentNode`）直接镜像编译产物形态（`function.properties` 的内联 operation），不做中间模型转换——产物即契约。

## Call Flow

```
调用方                ComposeClient          链执行器           HandlerA/B          fetch
  │                       │                     │                  │                │
  │ fetch(id,{params})    │                     │                  │                │
  │──────────────────────►│                     │                  │                │
  │                       │ ① 索引定位 OperationDescriptor        │                │
  │                       │ ② bind: params→URL/query/body         │                │
  │                       │ ③ 构造 Ctx{request}                   │                │
  │                       │────────────────────►│                  │                │
  │                       │                     │ ④ A.handle(ctx,next)               │
  │                       │                     │─────────────────►│                │
  │                       │                     │    B.handle(ctx,next)              │
  │                       │                     │──────────────────────────────────►│
  │                       │                     │                  │ ⑤ HTTP 发出    │
  │                       │                     │                  │◄───────────────┤ ApiResult
  │                       │                     │ ⑥ B 上行（转换/剪枝/缓存写入）      │
  │                       │                     │◄─────────────────┤                │
  │                       │ ⑦ 2xx→兑现 data     │ ⑥' A 上行（解密/错误映射）          │
  │                       │   非2xx/失败→reject ApiError           │                │
  │◄──────────────────────┤                     │                  │                │
```

下行（④→⑤）Handler 可改写 `ctx.request`、可短路；上行（⑥）可改写 `ctx.response`。管道全程纯数据流，兑现值恒为数据（无副作用旁路，见 Open Questions）。

## Usage Examples

```ts
// ---- 1. 创建（默认：内部拉一次树；此后无第二次树请求）----
const client = await ComposeClient.create()

// 测试/离线：注入树源与 mock HTTP，零真实网络
const client = await ComposeClient.create({
  treeSource: () => Promise.resolve(localTree),
  fetch: mockHttp,
})

// ---- 2. 调用（params 按内联声明自动分拣，调用方不关心传输细节）----
const users = await client.fetch('findUsers', {
  params: { name: 'x', state: 'enable', offset: 1, size: 20 },
})
await client.fetch('accountStatusSetting', { params: { id: '42', state: 'disable' } })

// ---- 3. 错误处理（reject 语义：成功即纯数据，失败即 ApiError）----
try {
  await client.fetch('accountStatusSetting', { params: { id: '42', state: 'bad' } })
} catch (e) {
  if (e instanceof ApiError) {
    e.status                    // 400
    e.message                   // 响应体 message（后端中文文案原文）
    e.fieldErrors               // ParameterErrorVO 字段级错误 → 表单高亮数据源
  }
}

// ---- 4. 官方 Handler 均由注册实现（core 零自带，以下为形态示意）----

// 鉴权：token 注入（401 与普通失败一致 reject；重放/登录流程随登录页提案）
class AuthHandler implements Handler {
  async handle(ctx: Ctx, next: () => Promise<ApiResult>): Promise<ApiResult> {
    ctx.request.headers['X-Session-Token'] = currentToken()
    return next()
  }
}

// 转换：snake_case → camelCase（显式注册，核心永不自动转换）
class CamelCaseHandler implements Handler { /* 上行改写 res.body 键名 */ }

// 缓存：短路 = 不调 next，直接以缓存兑现
class CacheHandler implements Handler {
  async handle(ctx: Ctx, next: () => Promise<ApiResult>): Promise<ApiResult> {
    const key = `${ctx.operation.operationId}:${JSON.stringify(ctx.request.url)}`
    const hit = cache.get(key)
    if (hit) return hit                          // 短路
    const res = await next()
    cache.set(key, res)
    return res
  }
}

// 注册顺序 = 执行顺序（越早注册越外层；顺序敏感项见 Risks）
client.register(new AuthHandler())
client.register(new CacheHandler())
```

## Risks / Trade-offs

- [树形状硬编码] 树抽取逻辑耦合 app > page > function 形状，后端结构演进会静默破坏索引 → 树抽取集中在 `source.ts` 单一模块 + vitest 用真实编译产物形态的 fixture 锁定
- [管道顺序敏感] 加密/缓存/鉴权的相对顺序错误会产生隐蔽 bug（如缓存存了密文）→ 链执行器测试覆盖顺序语义；官方 Handler 落地时文档化推荐顺序
- [openapi 嗅探误判] 裸文档嗅探可能误判非 openapi JSON → 树特征（type/children）优先匹配，嗅探规则收窄（`paths` 存在且为对象），不确定时按树路径失败
- [next 重入与重放] 401 重放需要链执行器支持语义明确的重放（如 `ctx.retry()`），当前设计未预支 → 登录流程提案一并设计，本变更链执行器只保证单次下探语义

## Open Questions

- 副作用事件机制（Handler → 渲染层的 loading/toast 通知通道，emit 出口与订阅端）——本变更裁决推后，随渲染器提案一并设计
- 渲染扩展键（operationId / route / 复合键）与注册机制载体——渲染器提案的核心议题
- 官方 Handler 清单与落地顺序（ajv 校验、缓存键策略、snake→camel 转换约定）——按真实需求逐个提案
- 登录流程：401 重放（`ctx.retry()` 语义）、会话刷新与 `GET /v1/system/authentications/current` 的启动预检——登录页提案一并解决
