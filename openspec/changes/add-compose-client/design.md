# Design

## Context

`redesign-ping-ui-framework` 落地的编译产物是本设计的核心原料：`GET /v1/ui/components` 返回自包含功能树，`function.properties` 已是 `$ref` 全展开、仅 2xx 的 openapi operation（method/path/parameters/requestBody/responses + route）。swagger-client 类通用客户端需在运行时承担的文档下载、$ref 解析、索引构建，ping 已在启动期完成——本客户端的运行时工作只剩「索引 + 绑定 + 管道」。

前端现状为白纸：14 个 shadcn/ui 组件、`createPingUi` 占位 entry、vite lib 模式产物 `renderer.js`；旧 SDK（处理器注册表/message-bus）已随 redesign 删除，本设计不欠其兼容债。前端测试框架已随 SDK 删除一并消失（package.json 仅剩 build/preview）。动机参见 proposal.md - Why。

本设计经 2026-10-22 重设计收敛：管线形态定为 **Koa 式洋葱**（参照 koa-compose 十几年不变的模型），砍掉一切类型体操，框架认知成本一句话——**「传个袋子进链，最里面 return 什么，外面就拿到什么」**。

## Goals / Non-Goals

**Goals:**

- 客户端核心极度薄：拿到树 → 建索引 → 绑定参数 → 过管道 → fetch，无其他职责
- 框架层（管道执行器）零业务概念、零泛型体操；业务行为（鉴权、加解密、转换、校验、剪枝、缓存）全部经 addHandler 注册，核心零业务魔法
- 数据层与视图层单向依赖：UI 组件引入客户端，客户端永不 import React
- 可测性优先：网络（树源 + HTTP 层）是仅有的两个可注入边界，其余为纯逻辑

**Non-Goals:**

- 渲染器（侧栏/路由/function 渲染器/渲染扩展键）——后续渲染器提案
- 登录页与 401 重放登录流程的完整实现——依赖登录 UI，后续提案
- 任何官方业务 Handler 实现（校验/缓存/加密/转换/剪枝）——后续按需逐个落地
- 对外发布 npm 包——客户端与渲染器同 bundle（renderer.js）交付

## Decisions

### D1 创建形态：静态工厂 + 树源/HTTP 层注入

`await ComposeClient.create(options?)`，内部跑 bootstrap 链（source → indexer，见 D7）取树建索引，失败即 reject。备选：`new` + `init()`（存在「忘了 init 就调用」窗口）；首次调用惰性拉树（错误时机模糊，需 memo in-flight promise）——均否决。`options.treeSource`（默认 `fetch('/v1/ui/components')`）与 `options.fetch`（HTTP 层，默认 `globalThis.fetch`）是仅有的两个注入边界：测试/Node 场景注入纯对象树与 mock HTTP，其余全为纯逻辑。**输入信任**：树结构与 operationId 唯一性由后端编译期保证，客户端不设坏树容错分支（用户裁决——无需考虑坏树情况）。

### D2 索引键：operationId（route 为产物便利字段）

operationId 是 openapi 原生标识，后端已保证全局唯一（断链即启动失败）；route（`page.id/function.id`）仅为 UI 路由便利字段，留待渲染器消费。索引同时嗅探裸 openapi 文档形态（含 `paths`/`openapi` 键）直接建索引——树抽取是主路径，裸文档是零成本副产品（工具/测试场景）。

### D3 管道：Koa 式洋葱，函数即 Handler，返回值即数据

2026-10-22 重设计（用户裁决），参照 Koa/koa-compose：

- **Handler 是函数不是接口**：`type Handler = (ctx, next) => Promise<unknown>`。写一个 hook 就是一个箭头函数，无 class、无实现接口样板。曾设计 `Handler<In, Out>` 双泛型（嵌套时检查进出类型衔接），实测收益恒假（一条链上所有环节签名必须一致，衔接检查永远通过）且认知负担大——砍掉
- **ctx 运行时是空袋子**：普通对象，谁要什么谁自己放（Koa ctx 惯例）。框架代码不引用任何字段语义，只透传
- **返回值即数据**：最内层环节 return 的值沿 `await next()` 逐层冒泡，`fetch()` 兑现该值。数据走 return 通道、错误走 throw/reject 通道，两通道干净分离——缓存短路直接 `return 缓存数据`，与正常路径类型一致
- **短路** = 不调 `next()` 直接 return；洋葱上行（`await next()` 之后）可读 `ctx.response`、可加工数据
- 备选钩子模型（before/after/error 三段）：无法表达「重试时先刷新 token 再重发」这类横切逻辑（被迫拆两半用外部变量粘连），且响应上行改写表达贫乏——否决。备选事件订阅：只能旁观不能改写——否决
- **副作用事件机制（emit 出口 + 订阅端）整体推后**（用户裁决，聚焦 client 本体），loading/toast 等 UI 通知随渲染器提案设计
- **「Handler 直接返回 React 对象」已否决**（拷问结论）：数据管道泄漏视图会迫使核心依赖 React 并污染调用点类型；「特殊渲染」需求归渲染器的注册表扩展点（后续提案），依赖方向恒为 UI → client

### D4 ctx：单一 Context 协议接口（组装层导出）

业务写 hook 时的入参出参判断靠**唯一一个 `Context` 接口**（组装层定义、随包导出）：字段分组注释标明「谁填、什么时候有」——fetch 入口填（operationId/params/signal）、bootstrap 链填（raw/tree/index）、调用链填（operation/request/response）。编译期 `ctx.` 即有补全，依赖显式化（hook 之间互不认识，都依赖同一份契约，如同插头与插座共按同一标准制造）。

运行时无任何校验与魔法——还是那个空袋子。字段可选（两链共用一个袋子类型所致），「保证有」由环序约定 + helper 兜底：`requireOperation(ctx)` / `requireRequest(ctx)` 缺字段时 throw 明确错误，替代 undefined 断言炸。

### D5 错误：reject + ApiError 对齐后端错误体

非 2xx/网络失败/解析失败一律 reject `ApiError extends Error`：`status`（网络失败 0）、`operationId`、`message`（响应体 `MessageVO.message` 提取，回落状态文本）、`fieldErrors`（`ParameterErrorVO` 字段级错误原样透出——渲染器表单高亮的对接点）。备选 Result 对象模式（`{ok, data, error}`）：每个调用点须判 ok，成功路径被污染，违背「异常精炼」。

判错与边界（用户裁决，2026-10-21）：

- **2xx 即业务成功**：RESTful 状态码判错，不检查响应体业务错误码；空响应体（204/零长度）兑现 `undefined`，不算解析失败
- **取消原样透出**：`AbortSignal` 触发的 `AbortError` 不包装成 ApiError——调用方可与网络失败（status=0）区分，重试逻辑不误发
- **Handler 内部异常原样传播**：管道抛出的非 ApiError（如 TypeError）不伪装为 HTTP 失败，调用方拿到的是真实 bug

### D6 零魔法三条（已确认）

- **401 不特殊**：核心抛 `ApiError{status:401}`，登录流程归鉴权 Handler（可插拔纯粹性；「不要登录流程的场景」自然成立）。**用户已确认**（2026-10-21）。
- **snake_case 不转换**：兑现数据保持后端字段名原文，转换是注册的 Handler（显式可见，调试时与后端原文对得上）。**用户已确认**（2026-10-21）。
- **未声明参数忽略**：调用多传的参数静默丢弃（前向兼容）；缺失 path 参数则拒绝且不发请求（无法构造 URL）。

### D7 三层架构：框架 / 组装 / 业务

2026-10-22 定稿（用户裁决「所有具体细节——解析组件、请求——都可注册为 Handler」的落地形态）：

```
① 框架层（pipeline.ts）—— 不认识任何业务字段
   Handler/Next 类型 + compose 执行器（洋葱循环）
   + Context 类型 + ApiError —— 框架认识的全部：一串环、一个袋子、洋葱语义

② 组装层（handlers/ + client.ts）—— 交付物，不是特权
   五个架子环：source / indexer / locate / bind / http
   默认链组装 + addHandler 插槽 + Context 协议字段
   用门面 = 选了这套组装约定；不用门面可直接 compose 自组链

③ 业务层 —— 用户的地盘
   写 Handler（函数最轻；要嵌套子环节用普通组合或 compose 串数组）
   addHandler 挂入，插槽固定在 locate 与 bind 之间
```

- 顺序问题的解法：**框架执行器零排序知识**；「locate 最先、http 最后」是组装层（门面）的约定，不是框架特权——替换 http 环即零网络 mock，替换 source 环即离线静态树。**业务环插槽固定在 bind 之后、http 之前**（贴近终点：可读 operation 与 request、可改写 request、可短路；实现期修正——插在 bind 之前时 hook 拿不到 request，无法满足 spec「请求改写」场景）
- 「core 零自带」哲学精确化：**业务环零内置**（鉴权/缓存/转换一个都不给）；架子环是「消费编译产物」的基础设施（没有它们 client 不成立），以普通模块交付、可独立取用可替换
- 行业参照：Koa（ctx 空袋子 + 洋葱 + compose）、tRPC/Apollo Link（终点即普通环节）、swagger-client（operationId 入口）、Ky（工厂选项注入）——无任何自创概念

### D8 产物形态：住 renderer.js bundle

`src/main/js/client/`（纯 TS），与渲染器同 bundle 交付——「UI 组件引入 client」是 bundle 内部 import，无 npm 发布/CDN/externals 复杂度。vite entry（`sdk/index.ts`）与产物文件名不变。

### D9 测试：vitest 恢复 + maven 接入

恢复 `vitest devDependency` + `npm test` script（独立 `vitest.config.ts`，`environment: node`——用户裁决，测试为纯逻辑、Node 环境即真实运行环境），`ping-ui/pom.xml` 的 exec-maven-plugin 增加 test 阶段 `npm run test`（复用 `ping.npm.skip` 开关，与 build 接入同构）。分层覆盖要求见 Test Requirements 节——无测试的洋葱链等于盲写。

### D10 命名：ComposeClient

「swagger-client」撞官方 npm 包名。`ComposeClient` 与后端词汇族（ComposeCompiler/ComposeRepository/ComposeException）同源——消费 compose 编译产物的客户端，模型概念一眼可归属。

### D11 传输策略细节归 Handler，core 不立契约

超时、baseURL/跨域、请求头定制、非 JSON 内容处理、query 空值与编码策略等传输层细节，**全部不进 core 契约**（用户裁决「B 相关的问题全部由 handler 去解决」）：核心只完成「按内联声明绑定 + 发出」，需要特殊策略时经 `ctx.request` 改写或短路实现。同源绝对路径（`/v1/...`）直发为默认形态，不提供 `baseUrl` 选项（避免与 Handler 改写机制重复开洞）。

## Architecture

### 组件清单

```
ping-ui/src/main/js/
  client/                     纯 TS，零 React、零框架依赖
    index.ts                  公共导出（渲染器/业务组件唯一 import 口）
    types.ts                  Context 协议接口（分组字段：谁填谁读）
                              + OperationDescriptor/ComponentNode/ApiRequest
                              /ResponseMeta/TreeSource/ComposeClientOptions/FetchInit
                              + requireOperation/requireRequest helper
    error.ts                  ApiError（status/operationId/message/fieldErrors）
    pipeline.ts               框架：Handler/Next 函数类型 + compose 洋葱执行器
    handlers/                 组装层：五个架子环（全部普通 Handler，可独立取用/替换）
      source.ts               环① 取树：treeSource() → ctx.raw
      indexer.ts              环② 建索引：嗅探（树特征优先 → 裸 openapi paths）
                              → ctx.tree + ctx.index（operationId → operation 表）
      locate.ts               环③ 查表：ctx.operation = index.get(operationId)
                              查不到 throw ApiError（消息含键名）
      bind.ts                 环④ 绑定（纯函数）：params 按 parameters[].in 分拣
                              → ctx.request（path 替换/query 平铺/body JSON）
      http.ts                 环⑤ 终点：发出 ctx.request；2xx → ctx.response
                              = {status, headers} + return 解析后数据；
                              非 2xx/网络失败 → throw ApiError；不调 next
    client.ts                 门面：ComposeClient（create 跑 bootstrap 链、
                              fetch 组调用链 [locate, ...hooks, bind, http]、
                              addHandler 插槽、tree 只读暴露）
  sdk/index.ts                vite entry（不变）：导出衔接 ComposeClient
```

### 三层依赖方向

```
③ 业务 hook ──import──► ② 组装层（Context 类型 / ComposeClient / require* helper）
② 组装层   ──import──► ① 框架层（compose / Handler 类型）
① 框架层   ──import──► types.ts / error.ts（仅类型与错误契约，零行为依赖）
```

职责边界（DDD 职责清晰）：`pipeline` 只管跑链，`handlers/*` 每环单一职责（十行级），`client` 只做组装，`bind` 是纯函数——模块间以 `types.ts` 的结构类型为契约。

## Key Interfaces

```ts
// ===== ① 框架层（pipeline.ts）：不认识任何业务字段 =====
export type Next = () => Promise<unknown>
export type Handler = (ctx: Context, next: Next) => Promise<unknown>

/** 把链串成洋葱：chain[0] 最外层；返回 run(ctx)。语义：
 *  · 每环 await next() 下探，return 值/异常逐层冒泡
 *  · 不调 next 即短路；next() 重复调用 throw；链尽头无人 return 兑现 undefined */
export function compose(chain: Handler[]): (ctx: Context) => Promise<unknown>

// ===== ② 协议（types.ts）：组装层定义，业务 import =====
export interface Context {
  // ── fetch 入口填 ──
  operationId?: string
  params?: Record<string, unknown>
  signal?: AbortSignal
  // ── bootstrap 链填（create）──
  raw?: unknown
  tree?: ComponentNode
  index?: OperationIndex
  // ── 调用链填 ──
  operation?: OperationDescriptor
  request?: ApiRequest
  response?: ResponseMeta          // http 环 2xx 填；响应数据走 return 冒泡
}

export type OperationIndex = ReadonlyMap<string, OperationDescriptor>

export interface ResponseMeta {                       // 响应元数据（数据本体走 return）
  status: number
  headers: Record<string, string>
}

export interface ApiRequest {
  method: string
  url: string                                          // 已完成 path/query 绑定
  headers: Record<string, string>
  body?: string                                        // 已 JSON 序列化
}

export type TreeSource = () => Promise<unknown>

export interface ComposeClientOptions {
  treeSource?: TreeSource                              // 默认：fetch /v1/ui/components
  fetch?: typeof globalThis.fetch                      // 默认：globalThis.fetch
}

export interface FetchInit {
  params?: Record<string, unknown>                     // 按内联 parameters 自动分拣
  signal?: AbortSignal                                 // 取消/超时透传
}

// require* helper：字段缺失 throw 明确错误，替代 undefined 断言炸
export function requireOperation(ctx: Context): OperationDescriptor
export function requireRequest(ctx: Context): ApiRequest

// ===== 错误契约（error.ts）=====
export class ApiError extends Error {
  status: number                                       // HTTP 状态；网络失败 = 0
  operationId: string
  message: string                                      // body.message 提取，回落状态文本
  fieldErrors?: Record<string, unknown>                // ParameterErrorVO 字段级错误原样
}

// ===== ② 门面（client.ts）=====
export class ComposeClient {
  static create(options?: ComposeClientOptions): Promise<ComposeClient>
  /** 组链 [locate, bind, ...业务hooks, http] → run(seed ctx) → 兑现冒泡值 */
  fetch<T = unknown>(operationId: string, init?: FetchInit): Promise<T>
  /** 业务环插槽：bind 之后、http 之前；多次调用顺序 = 执行顺序 */
  addHandler(handler: Handler): void
  readonly tree?: ComponentNode                       // 只读（deep freeze），与索引同源；裸文档输入时无树语义
}
```

`OperationDescriptor` 与树节点类型（`ComponentNode`）直接镜像编译产物形态（`function.properties` 的内联 operation），不做中间模型转换——产物即契约。

## Call Flow

### create：bootstrap 链

```
调用方                      ComposeClient              compose              source / indexer
  │                             │                        │                        │
  │ await ComposeClient.create()│                        │                        │
  │────────────────────────────►│  ctx = {}（空袋子）     │                        │
  │                             │  run = compose([source, indexer])               │
  │                             │───────────────────────►│                        │
  │                             │                        │ ① source: ctx.raw = await treeSource()
  │                             │                        │───────────────────────►│ 取树（注入或默认 fetch）
  │                             │                        │ ② indexer: 嗅探解析     │
  │                             │                        │◄───────────────────────┤ ctx.tree / ctx.index
  │                             │◄───────────────────────┤ await run(ctx) 结束     │
  │◄────────────────────────────┤ new ComposeClient(ctx.tree, ctx.index, fetch注入)
```

### fetch：调用链（洋葱）

```
调用方        ComposeClient         compose          locate      业务hooks      bind        http        注入 fetch
  │               │                   │                │            │            │            │             │
  │ fetch(id,     │                   │                │            │            │            │             │
  │  {params})    │                   │                │            │            │            │             │
  │──────────────►│ ctx = {operationId, params, signal}│            │            │            │             │
  │               │ run = compose([locate, bind, ...hooks, http])  │            │            │             │
  │               │──────────────────►│                │            │            │            │             │
  │               │                   │ 下行 ─────────►│ ① 查表     │            │            │             │
  │               │                   │                │ ctx.operation = index.get(id)      │             │
  │               │                   │                │ （查不到 throw ApiError）           │             │
  │               │                   │─────────────────────────────────────────────────────►│ ② 拼 URL  │
  │               │                   │                │            │            │ ctx.request = 按内联声明分拣│
  │               │                   │────────────────────────────►│ ③ 业务hooks│            │             │
  │               │                   │                │            │ 可改写 ctx.request、可短路（不调 next 直接 return）│
  │               │                   │──────────────────────────────────────────────────────────────►│ ④ 发出 ──────►│
  │               │                   │                │            │            │            │             │ ⑤ 请求
  │               │                   │                │            │            │            │◄─────────────┤ 响应
  │               │                   │                │            │            │            │ 2xx:        │
  │               │                   │                │            │            │            │  ctx.response = {status, headers}
  │               │                   │                │            │            │            │  return 数据 │
  │               │                   │                │            │            │◄───────────┤ 非2xx/网络失败: throw ApiError
  │               │                   │                │            │◄───────────┤ 上行：await next() 拿到数据│
  │               │                   │                │◄───────────┤ （上行可读 ctx.response、可加工数据）│
  │               │                   │◄───────────────┤ return 值沿 await next() 逐层冒泡    │
  │◄──────────────┤ fetch 兑现冒泡值（成功）或 reject（ApiError / 原样异常）                    │
```

数据走 return 通道、错误走 throw 通道：任何环节 throw（ApiError 或原样异常）即整链 reject，无包装。

## Usage Examples

```ts
// ===== 1. 创建 =====
const client = await ComposeClient.create()          // 内部拉一次树，此后无第二次树请求

// 测试/离线：注入树源与 mock HTTP，零真实网络
const client = await ComposeClient.create({
  treeSource: () => Promise.resolve(localTree),
  fetch: mockHttp,
})

// ===== 2. 调用（params 按内联声明自动分拣，调用方不关心传输细节）=====
const users = await client.fetch('findUsers', {
  params: { name: 'x', state: 'enable', offset: 1, limit: 20 },
})
await client.fetch('accountStatusSetting', { params: { id: '42', state: 'disable' } })

// ===== 3. 错误处理（成功即纯数据，失败即 ApiError）=====
try {
  await client.fetch('accountStatusSetting', { params: { id: '42', state: 'bad' } })
} catch (e) {
  if (e instanceof ApiError) {
    e.status                    // 400
    e.message                   // 响应体 message（后端中文文案原文）
    e.fieldErrors               // ParameterErrorVO 字段级错误 → 表单高亮数据源
  }
}

// ===== 4. 业务 hook：函数即 Handler，一个箭头函数就是全部 =====
const logHook: Handler = async (ctx, next) => {
  const t = Date.now()
  try {
    return await next()
  } finally {
    console.log(ctx.operationId, Date.now() - t, 'ms')
  }
}
client.addHandler(logHook)      // 插槽：bind 之后、http 之前

// ===== 5. 要嵌套子环节时：普通组合，或 compose 串数组 =====
const auth: Handler = async (ctx, next) => {
  const op = requireOperation(ctx)          // 缺字段 throw 明确错误
  if (op.secure) ctx.request.headers['X-Token'] = currentToken()
  return next()
}
const cache: Handler = async (ctx, next) => {
  const key = cacheKey(ctx)
  const hit = cache.get(key)
  if (hit) return hit                       // 短路：不调 next，直接以缓存数据兑现
  const data = await next()
  cache.set(key, data)
  return data
}
client.addHandler(compose([auth, cache]))    // auth 包 cache 包后续环节

// ===== 6. 上行加工：读响应元数据、转换数据 =====
const camelHook: Handler = async (ctx, next) => {
  const data = await next()                 // 下行环节全部跑完后拿到数据
  console.log(ctx.response?.status)         // http 环 2xx 时已填 {status, headers}
  return camelize(data)                     // 改写后交还上层
}
client.addHandler(camelHook)

// ===== 7. 测试：换终点环 = 零网络（不经门面直接 compose 亦可）=====
const run = compose([locate(index), bind, async () => ({ users: [] })])
await run({ operationId: 'findUsers', params: {} })   // 兑现 { users: [] }
```

## Test Requirements

测试基座：vitest（`environment: node`）；全部测试经注入边界（treeSource/fetch）运行——**零真实网络、零浏览器依赖**。

| 层 | 组件 | 必须覆盖 |
|---|---|---|
| ① 框架 | pipeline（compose） | 执行顺序 = 数组顺序；短路（不调 next 不触发下一环）；return 值逐层冒泡；next() 重复调用 throw；链尽头无人 return 兑现 undefined；Handler 异常原样透传不包装；深链（≥5 层）洋葱语义正确 |
| ② 组装 | source | 树输入透传 ctx.raw；网络失败/非 2xx 带原因 reject |
| ② 组装 | indexer | 树特征优先嗅探；裸 openapi 文档（paths 对象）识别；app > page > function 抽取；operationId 索引键完整；非法输入拒绝 |
| ② 组装 | locate | 已知键定位 ctx.operation；未知键 throw ApiError（消息含键名） |
| ② 组装 | bind | path 模板替换；query 平铺（数组重复键）；body JSON 序列化（仅声明 requestBody 时发送）；缺 path 参数拒绝不发请求；未声明参数忽略（对应 spec 5 场景） |
| ② 组装 | http | 2xx JSON return 解析数据 + ctx.response 填元数据；空体（204/零长度）兑现 undefined；非 2xx throw ApiError（message 自 MessageVO 提取、缺失回落状态文本、fieldErrors 原样透出）；网络失败 status=0；AbortError 原样透传不包装 |
| ② 组装 | client 门面 | create 默认/注入树源；创建失败 reject 无半初始化；tree 只读；addHandler 插槽位置（业务环在 locate 后 bind 前）；多次 addHandler 顺序 = 执行顺序；fetch 兑现链冒泡值 |
| 集成 | client | 真实 fixture 全链路（Node）；401 未注册鉴权环与普通失败无差别；snake_case 字段原文兑现；业务环请求改写抵达最终请求；短路不发 HTTP |

- **fixture 纪律**：树形 fixture 必须来自真实编译产物（启动 ping-system 导出 `GET /v1/ui/components` 响应）——后端结构演进时测试先红
- **场景对照**：spec 26 个场景每个至少一个对应测试，全绿是验收线
- maven 接入：`./mvnw -Ph2 test` 触发 ping-ui npm test，任一用例失败即构建失败

## Risks / Trade-offs

- [树形状硬编码] 树抽取逻辑耦合 app > page > function 形状，后端结构演进会静默破坏索引 → 树抽取集中在 `indexer.ts` 单一模块 + 真实产物 fixture 锁定
- [管道顺序敏感] 加密/缓存/鉴权的相对顺序错误会产生隐蔽 bug（如缓存存了密文）→ compose 顺序语义测试锁定；官方 Handler 落地时文档化推荐顺序
- [openapi 嗅探误判] 裸文档嗅探可能误判非 openapi JSON → 树特征（type/children）优先匹配，嗅探规则收窄（`paths` 存在且为对象），不确定时按树路径失败
- [next 重入与重放] 401 重放需要执行器支持语义明确的重放（如 `ctx.retry()`），当前设计未预支 → 登录流程提案一并设计，本变更执行器只保证单次下探语义
- [Context 字段交叉] bootstrap 与调用链共用一个袋子类型，字段全部可选、「谁保证有」靠环序约定 → 字段分组注释 + require* helper + 环序测试锁定（D4）
- [插槽位置固定] 业务环恒在 bind 之后 http 之前——想包住 locate/bind（如从 seed 起计时）或在 http 之后插环做不到 → 极端场景不经门面，直接 `compose` 自组链（框架层为此而存在）
- [空袋子弱类型] ctx 字段为可选，写错字段名运行时才发现（编译器不拦）→ Context 协议接口提供补全减少笔误；require* helper 把「缺字段」从 undefined 炸变为明确错误

## Open Questions

- 副作用事件机制（Handler → 渲染层的 loading/toast 通知通道，emit 出口与订阅端）——本变更裁决推后，随渲染器提案一并设计
- 渲染扩展键（operationId / route / 复合键）与注册机制载体——渲染器提案的核心议题
- 官方业务 Handler 清单与落地顺序（ajv 校验、缓存键策略、snake→camel 转换约定）——按真实需求逐个提案
- 登录流程：401 重放（`ctx.retry()` 语义）、会话刷新与 `GET /v1/system/authentications/current` 的启动预检——登录页提案一并解决
