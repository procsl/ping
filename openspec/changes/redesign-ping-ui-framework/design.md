# Design

## Context

当前 `ping-ui` SDK（`add-ping-ui-web-sdk` 产物）采用运行时注册表 + 消息总线 + 生命周期的重模型，18 种组件类型中大量与前端导航壳耦合。后端聚合链路已断裂（`GET /v1/ui/components` 不存在），前端降级到本地 `default-tree.ts`。动机参见 `proposal.md - Why`。

## Goals / Non-Goals

**Goals:**
- 定义一套极致克制的 JSON 声明语言，只声明功能组件及其与 operation 的输入/输出映射
- 编译阶段全量解析（`openapi://` → OpenAPI 操作、OpenAPI `$ref` 展开、`openapi://` 字段路径 → UUID、默认值合并），失败则启动失败
- 前端拿到完全内联的最终 JSON，零运行时解析
- 恢复后端 `GET /v1/ui/components` 聚合接口
- 移除 SDK 运行时（注册表、生命周期、消息总线、错误处理器）
- **DDD 代码架构**：引入 `Component` 组合类型、`App` 应用根、`ComposeEngine` 编译器、`ComposeRepository` 仓储
- **开闭原则**：新增编译阶段只需实现 `CompilePhase` 接口，不修改已有代码

**Non-Goals:**
- 不实现前端渲染器（各端自行实现；本次前端只做减法，见 D19）
- 不实现组件通信机制（前端内部事务）
- 不支持运行时动态加载组件描述
- 不声明 UI 展示细节（布局、样式、图标等）
- 不做 platform 过滤（D8）
- 不涉及任何权限/角色过滤（无权限体系，不做钩子）
- 不做分模块 openapi 导出与散装豁免（后续提案）

## Decisions

### D1: 组件类型系统从 18 种精简到 8 种

**决策**：只保留 `app`、`operation`、`function`、`view`、`form`、`input`、`output`、`pagination`。

**理由**：移除的 10 种类型（layout、nav_group、menu、breadcrumb、tabs、user_info_panel、main_container、row_action、form_action、empty）都是前端壳或 UI 细节，不属于"功能声明"。`row_action` 和 `form_action` 统一为 `input`（引用 operation.request 字段），`empty` 由前端自行处理。`pagination` 作为正式组件类型，前端需要注册渲染器。`placeholder` 移除（AI 误加，无实际场景）。

**备选方案**：保留更多类型以覆盖更多场景 → 被拒绝，违背克制原则。

### D2: operation 作为接口契约中心

**决策**：`operation` 是接口契约的定义点，包含 `method`/`path`/`request`/`response`/`defaults`/`sortable_fields`。`input`/`output` 通过 `$ref`（`openapi://` 协议）引用 operation 的字段。operation id **全局唯一**，重复即编译报错，报错信息带两处声明来源（模块文件名）——冲突的是接口默认值与 sortable_fields，静默覆盖会让前端拿到不可预期的契约。

**理由**：将接口描述集中管理，避免重复声明。前端可从 operation 生成可调用方法。编译时在被引用字段上插入 UUID 作为唯一标识。

**备选方案**：
- 每个 input/output 内联完整 schema → 被拒绝，重复且难以维护
- 保持引用不解析 → 被拒绝，前端需要运行时解析

### D3: 编译时解析 $ref → UUID，嵌入字段 id 属性

**决策**：编译阶段将 `openapi://` 字段路径解析为 UUID，替换 `$ref` 值，并在被引用字段的 `id` 属性上写入该 UUID。UUID 使用**确定性哈希**（基于字段路径），同一字段被多次引用时**共享同一 UUID**。前端不应依赖 UUID 的确定性特性。

**理由**：
- 编译时校验所有引用完整性，启动失败而非运行时降级
- 前端不需要理解路径引用，只需查 UUID
- UUID 嵌入字段本身，零额外结构
- 确定性哈希保证同一字段共享 UUID，但是否视为同一实例由前端决定

**备选方案**：
- 维护独立的 UUID 映射表 → 被拒绝，增加结构复杂度
- 使用有意义的别名 → 被拒绝，用户明确说"无需有意义，随机 id 即可"

### D4: 排序作为 input 处理

**决策**：排序是一种特殊的 `input`，由**编译器自动生成**（引用 operation 的排序参数，不带 value），开发者不手写。可排序字段定义在 operation 的 `sortable_fields` 列表中，且必须存在于 response（否则断链报错）；默认排序通过 `defaults` 给出。前端按 `sortable_fields` 决定列头排序入口，点击时自行拼 value（如 `account,asc`）。

**理由**：排序本质上是"传给接口的参数"，和查询条件、分页没有本质区别，统一为 `input` 保持模型一致性。排序状态是 sort **单值**参数，手写多个 `sort.x` input 会互相冲突，故与 D9 分页一致采用编译器自动生成——"如何识别排序 input"的问题也随之消失。

**备选方案**：在 output 上声明 `sortable: true` → 被拒绝，output 不应该关心行为，且需要后端支持；开发者手写排序 input → 被拒绝，单值参数多实例会冲突。

### D5: $ref 使用 openapi:// 协议引用 operation

**决策**：`$ref` 使用自定义协议 `openapi://<operationId>` 引用 OpenAPI 操作（如 `openapi://findUsers`）。编译器按 `operationId` 在 OpenAPI 文档中查找对应 path + method。冲突时追加类型+方法名。**没有 operationId 的操作会导致编译报错**。

字段路径 SHALL 逐段下钻；数组包装层使用**固定段名 `items`** 继续下钻，即 `openapi://findUsers/response/content/items/account` ≡ `response.content.items.properties.account`（`content` 是数组）。数组字段上缺 `items` 段（或多写）按断链报错。

**理由**：
- `openapi://` 协议简洁直观，避免 JSON Pointer 长路径（如 `#/paths/~1v1~1system~1users/get`）
- 字段引用使用 `openapi://<operationId>/<request|response>/<path>` 语义路径，不依赖数组索引
- `operationId` 是机器生成的唯一标识，比 `summary`（人类可读文案）更稳定
- 冲突时追加类型+方法名保证唯一性

**备选方案**：
- 使用 JSON Pointer（`#/paths/~1v1~1system~1users/get`）→ 被拒绝，路径过长且数组索引不稳定
- 匹配 summary → 被拒绝，summary 可能重复且不稳定

### D6: 编译阶段全量校验，失败则启动失败

**决策**：编译校验按分级处理（明细见 spec「编译校验分级」）：

| 情况 | 处理 |
|---|---|
| `$ref` 断链（含数组路径缺 `items`）、被引用的空 schema `{}` | 报错，启动失败 |
| operation/function id 重复、view/form 未关联 operation、缺 operationId、`sortable_fields` 断链 | 报错，启动失败 |
| 未知组件类型、未知/多余属性（width、align…）、未被引用的空 schema | 忽略 + debug 日志 |

**全有或全无**——任何一个模块编译失败，整个应用启动失败。**报错信息必须包含具体引用路径与声明来源**，便于快速定位。

**理由**：断链意味着声明有错误，运行时降级会隐藏问题；但"非法属性"是**黑名单**问题，永远列不全，且未知属性留着能兼容版本升级（属性放行不渲染即可），与"未知类型忽略"同一待遇。被引用的空 schema 则不同——引用到了却内容为空，前端无法推断控件，报错能逼后端补全（见示例中 `UserAccountRecord: {}` 的真实案例）。

**备选方案**：运行时降级 + WARN → 被拒绝，与"启动永不失败"的旧不变量冲突，但新设计认为声明错误应该尽早暴露。

### D7: 后端聚合器扫描 classpath 收集 compose.json

**决策**：启动时扫描 classpath 下所有 `META-INF/ping/ui/compose.json`（**文件名固定为 `compose.json`，源文件直接放该目录，不经构建期聚合**），每个文件内容为 operation/function 数组（不需要 `type: "app"` 包装），合并编译为最终 app JSON，组装到内存（只读快照）。**启动时编译一次，运行期间不更新，直到重启**。function 按 id 字母序返回，但前端不可依赖此特性。

编译输入为打包期导出的 OpenAPI 文档：**`META-INF/ping/openapi-<module>.json`**（如 `openapi-ping-distribute.json`、`openapi-ping-system.json`），运行时扫描全部文件合并进 operation 索引。本次只有 distribute 导出（内容是全量 API）。

- **存在 compose.json 却不存在任何 openapi 文件 → 报错、启动失败**（散装运行的豁免归后续提案，本次不做）
- **多文件合并规则**：本次读到几个文件就合并进索引，operationId 重复即报错，不做内容比对。**约束（给分模块导出提案）**：分模块导出上线前必须先解决全量与部分文件的互斥规则（如 distribute 不再生成全量文件），否则 operation id 重复会锁死聚合启动。

**理由**：各业务模块独立提供 compose.json，后端聚合器统一编译并合并为 app.children。与现有"统一落 META-INF/ping/"的约定一致，且与 AGENTS"构建期产物统一落 META-INF/ping/、运行时只读该固定路径"对齐（`openapi.json` 从 classpath 根 `ping-api-doc/` 迁入 `META-INF/ping/`，同时改名加模块前缀）。顺序不应由后端决定，后续如需排序会增加新提案。

**备选方案**：构建期聚合 → 被拒绝，当前构建期聚合链路已移除，且启动期聚合更灵活；运行时用 springdoc 现生成输入 → 被拒绝，本次明确"只认打包期导出文件"，来源可插拔留待后续；无 openapi 时静默跳过编译 → 被拒绝，有 compose.json 无输入是声明错误，应尽早暴露。

### D8: platform 过滤（不做）

**决策**：本次彻底不做，后续需要时再提新提案。spec 中的「平台过滤」需求与 tasks 中的对应任务已移除——没有第二个平台在用，留着就是空承诺。

### D9: 分页自动检测 + 物化为 pagination 容器 + input 组件

**决策**：编译阶段检测 operation 的 request 是否含 `offset` + `size`、response 是否含 `content` + `total`。检测到后：
1. 生成 `type: "pagination"` 容器组件，children 为该分页接口的 output 子节点，`mode` 属性区分 `page`（按页分页）或 `scroll`（滑动分页），默认 `page`
2. **request + response 均匹配时**（mode=page）：自动生成 `type: "input"` 组件（`offset` 和 `size`），插入到 view 的 children 中，与 query、sort 并列
3. **仅 response 匹配、request 无分页参数时**（mode=scroll）：**不生成** offset/size input，pagination 容器属性中也不带 offset/size/content 引用——request 里根本没有这两个字段，生成即 `$ref` 断链，scroll 模式将永远无法编译成功；容器只保留 `mode` 并包裹 output children，翻页行为由前端滚动触发

**理由**：分页是后台系统的固定模式（offset/size 请求 + content/total 响应），应该由编译器自动物化。pagination 作为容器包裹 output 子节点，前端既知道数据来自分页接口，又能针对性渲染分页器。`mode` 让前端选择按页分页器还是滑动加载。分页参数自动生成为 input，前端无需额外推断。

**备选方案**：
- 标记 `operation.paged = true`，前端自行推断 → 被拒绝，前端需要额外推断逻辑
- 在 compose.json 中显式声明分页组件 → 被拒绝，违背克制原则

### D10: DDD 代码架构——组合类型 + 编译器 + 仓储

**决策**：引入 DDD 代码架构，包结构如下（合并 compiler 和 service 包）：

```
cn.procsl.ping.boot.ui
├── domain/
│   └─ Component.java         # 组合类型（type + id + properties: Map<String, JsonNode> + children）
├── compiler/
│   ├── ComposeEngine.java     # 主编排器（合并原 ComposeCompiler + ComposeService），协调各编译阶段，返回 App
│   ├── OperationCompiler.java # operation 契约构建（解析 + 展开 + 默认值）
│   ├── ComponentCompiler.java # 组件树物化（UUID + 分页）
│   ├── CompilePhase.java      # 编译阶段接口
│   ├── CompileContext.java    # 编译上下文
│   └── Schemas.java           # 常量
├── repository/
│   └── ComposeRepository.java  # 返回 List<JsonNode>
└── exception/
    ├── ComposeException.java       # 基类，所有编译错误（引用断链、循环引用、格式错误等）
    └── ComposeParseException.java  # 文件级解析失败（JSON 格式错误、顶层类型不合法）
```

**理由**：
- `Component` 采用组合优于继承，不同类型只是 `type` 字段不同
- `properties` 类型为 `Map<String, JsonNode>`，直接依赖 Jackson 和 OpenAPI，实现简单
- `ComposeEngine` 合并原 ComposeCompiler 和 ComposeService，负责编译流程编排，返回类型安全的 `App`
- `ComposeRepository` 只负责数据访问，返回原始 `List<JsonNode>`
- 开闭原则：新增编译阶段只需实现 `CompilePhase` 接口

**备选方案**：
- 保持现有扁平结构 → 被拒绝，职责混乱、命名不清晰
- 引入 Component 继承层次 → 被拒绝，用户明确说"组合优于继承"

### D11: compiler 包整合——按职责域整合

**决策**：将现有 6 个类整合为 3 个：

| 新类 | 整合来源 | 职责 |
|---|---|---|
| `ComposeEngine` | 新增（合并原 ComposeCompiler + ComposeService） | 主编排器，协调各编译阶段，返回 App |
| `OperationCompiler` | `OperationRefResolver` + `SchemaRefExpander` + `DefaultResolver` | operation 契约完整构建：解析 → 展开 → 默认值 |
| `ComponentCompiler` | `RefResolver` + `PaginationMaterializer` | 组件树物化：UUID + 分页 |

**理由**：
- 按职责域整合，而非按编译阶段拆分
- operation 相关的操作（解析、展开、默认值）都是围绕 operation 契约的
- 组件树相关的操作（UUID、分页）都是围绕组件树的
- 命名统一为 `Compiler`/`Engine` 后缀，清晰一致

**备选方案**：保持现有细粒度拆分 → 被拒绝，太乱太细

### D12: exception 精简到 2 个

**决策**：从 4 个异常类精简到 2 个：

| 异常 | 用途 |
|---|---|
| `ComposeException` | 基类，所有编译错误（引用断链、循环引用、格式错误等） |
| `ComposeParseException` | 文件级解析失败（JSON 格式错误、顶层类型不合法） |

**理由**：
- 文件级解析错误与编译错误是不同性质的错误，值得区分
- 引用断链、循环引用等都是编译错误的一种，通过错误消息区分即可
- 异常类不多不少，报错明确

**备选方案**：
- 保持 4 个异常 → 被拒绝，用户明确说"太多了"
- 精简到 1 个 → 被拒绝，文件级错误与编译错误需要区分

### D13: operation 挂接点在 view/form，function 不携带 operation

**决策**：编译后的 JSON 中，operation 和 function 是平级关系（都在 app.children 下）；但 operation 的**挂接点只有 view/form**（`properties.operation`，一个 view 读一个接口、一个 form 提交一个接口），`function` **不携带** operation 属性。动作类功能（如「重置密码」）表达为 `function` 下挂 `form`，不引入动作类型。`function` 的 `children` 非空——至少一个 view/form，否则编译报错；类型系统中没有占位类型，未实现的功能不声明（不声明 = 不存在）。

**理由**：两处挂接必然产生"两个来源不一致听谁的"的问题；function 是纯导航入口，没有自己的接口；单 view 单 form 单接口的规则已拍板，function 再带 operation 就是纯冗余。

> ⏸️ **遗留（待完全重新设计）**：`function` 与 operation 的挂接关系可能被完全重新设计。当前以「不携带」落地——这是唯一**可增量演进**的方向（将来加可选属性不破坏；现在带了、将来要删才是真返工）。重新设计完成后更新本条与 spec 中的遗留标记。

### D14: path 参数自动生成 input 组件

**决策**：path 参数（如 `accountStatusSetting` 的 `{id}`）由编译器自动生成 input 组件，与 query/body 参数保持一致。前端从上下文（如行数据）取值。

**理由**：path 参数和 query/body 参数本质都是"传给接口的参数"，统一为 input 保持模型一致性。

### D15: 204 响应不特殊处理

**决策**：204 响应（无 response body）编译后 `response: {}`，编译器自然从 OpenAPI 得到空 response。如果有 output 引用 204 接口的 response 字段，编译失败（断链）。

**理由**：204 在 OpenAPI 中自然没有 response body，不需要特殊处理。

### D16: 无 operationId 的操作编译报错

**决策**：OpenAPI 中操作没有 `operationId` 时，编译器报错。

**理由**：`operationId` 是 `openapi://` 引用的基础，缺失则无法引用。

### D17: overrides 只能在 function 中声明

**决策**：`function` 可通过 `properties.overrides` 覆盖 operation 默认值，`view`/`form` 的默认值由编译生成。

**理由**：overrides 是功能级别的配置，只在 function 层声明，view/form 继承。

### D18: 打包导出模式跳过 UI 编译（解鸡生蛋）

**决策**：`ping-distribute` 打包期那次真实启动只负责导出 openapi 文档；`openapi.export=true` 时 **ping-ui 跳过组件编译与端点注册**（条件装配）。其余任何启动场景都完整编译校验。

**理由**：首次构建时 `target/classes/META-INF/ping/` 下还没有 openapi 文件，而 classpath 里已有业务模块的 compose.json——若导出进程也跑编译，按 D7 规则会直接启动失败，**永远造不出第一个 openapi 文件**（自锁）。这不是给声明错误开口子：导出进程的世界里本来就没有编译输入可校验。

### D19: 本次前端只做减法

**决策**：本次**不做前端渲染器**（维持 Non-Goal）。前端改动仅限：移除 SDK 机制文件（`registry.ts`、`message-bus.ts`、`sdk-error.ts`、`handlers/`），**保留 `sdk/index.ts` 作为最小占位 entry**（导出签名 `createPingUi({namespace})` 与 `init()/destroy` 行为不变，函数体降级为占位），薄壳 `META-INF/ping/entry/index.html` 一行不动，`npm run build` 必须通过。

**理由**：`sdk/` 是 vite 构建 entry，整目录删除会让 `npm run build` 必失败（与旧任务 4.4 自带的验证矛盾）；保留同签名占位使契约零变化，后续渲染器重建在同 entry 内填实现。旧任务点名要删的 `registry.tsx`、`derive.ts`、`default-tree.ts` 在仓库中已不存在，属空任务。

### D20: 端点鉴权不作特殊处理

**决策**：`GET /v1/ui/components` 跟随现有全局登录拦截器（未登录 401），不加 `@PermitAll` 等豁免，spec 不写鉴权 Scenario，权限策略后续另行考虑（本次不涉及任何权限，不做钩子）。

**理由**：组件树暴露全部接口的 method/path/字段结构，等于接口清单，不应给未登录者；静态资源本就放行，前端 401 场景留给后续壳工程处理。

## 调用流程

> 输入②：`META-INF/ping/openapi-<module>.json`（打包期导出的 OpenAPI 文档，运行时扫描合并）；`openapi.export=true` 时整体跳过（D18）。

```
┌─────────────────────────────────────────────────────────────┐
│                  ComposeEngine.compile()                     │
│                                                              │
│  1. ComposeRepository.loadAll() → List<JsonNode>            │
│  2. compile(List<JsonNode>) → App                           │
│                                                              │
│  输出: App（包含编译后的 Component 树）                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    ComposeEngine.compile()                    │
│                                                              │
│  输入: List<JsonNode>（ComposeRepository.loadAll() 返回）    │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Phase 1: OperationCompiler                             │  │
│  │                                                        │  │
│  │  遍历所有 operation 组件:                              │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │ 1. 解析 openapi:// 引用                          │  │  │
│  │  │ 2. 展开 OpenAPI $ref                             │  │  │
│  │  │ 3. 合并默认值                                     │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  │                                                        │  │
│  │  结果: context.operations (Map<String, JsonNode>)      │  │
│  │  │                                                        │  │
│  │  检测:                                                │  │
│  │  - operation id 全局唯一，重复则报错                  │  │
│  │  - 必须有 operationId，否则报错                      │  │
│  │  - 204 响应自然得到空 response {}                     │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Phase 2: ComponentCompiler                             │  │
│  │                                                        │  │
│  │  遍历所有 function/view/form 组件:                     │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │ 1. 字段路径 → UUID（确定性哈希）                  │  │  │
│  │  │ 2. 分页物化（page/scroll 模式）                  │  │  │
│  │  │ 3. path 参数自动生成 input                        │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  │                                                        │  │
│  │  结果: 编译后的 List<Component>（$ref 已替换为 UUID）  │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  输出: App（包含编译后的 Component 树）                       │
└─────────────────────────────────────────────────────────────┘
```

## 注意事项

1. **开闭原则**：新增编译阶段只需实现 `CompilePhase` 接口，注册到 `ComposeEngine`，不修改已有代码
2. **类型安全**：`Component` 采用组合类型，不同类型只是 `type` 字段不同，不引入继承层次
3. **职责清晰**：Repository 只负责数据访问，Engine 负责编排，Compiler 负责编译逻辑
4. **异常精简**：只保留 2 个异常类，通过错误消息区分不同错误场景
5. **命名规范**：统一使用 `Compiler`/`Engine` 后缀，避免 Resolver/Expander/Materializer 等混乱命名
6. **UUID 策略**：确定性哈希，同一字段共享 UUID，前端不应依赖此特性
7. **缓存策略**：启动时编译一次，组装到内存，运行期间不更新，直到重启
8. **编译失败**：全有或全无，任何一个模块失败则整个应用启动失败；分级见 D6（结构性错误报错、未知类型/属性忽略）
9. **编译时机**：在 Spring 上下文启动阶段完成（Bean 初始化），失败即上下文启动失败、进程非 0 退出——这是"启动失败"的语义载体，不拖到首个请求
10. **导出模式豁免**：`openapi.export=true` 时跳过编译（D18），除该模式外没有任何豁免路径

## 测试原则

1. **单元测试**：每个 Compiler 阶段都有对应的单元测试
2. **集成测试**：`ComposeEngine` 的端到端测试，验证完整编译流程
3. **异常测试**：验证各种错误场景（引用断链、循环引用、格式错误等）
4. **边界测试**：验证空数组、空对象、缺失字段等边界情况

## 示例

### 真实 openapi.json 片段（编译输入）

以下来自 `ping-distribute/target/classes/ping-api-doc/openapi.json`，是编译阶段解析 OpenAPI 内部 `$ref` 的输入：

```json
{
  "paths": {
    "/v1/system/users": {
      "get": {
        "tags": ["User"],
        "summary": "获取用户列表",
        "operationId": "findUsers",
        "parameters": [
          {
            "name": "query",
            "in": "query",
            "required": true,
            "schema": { "$ref": "#/components/schemas/UserQuery" }
          },
          {
            "name": "offset",
            "in": "query",
            "description": "基于从1开始的分页 (1..N)",
            "schema": { "type": "integer", "format": "int32", "default": 1 }
          },
          {
            "name": "size",
            "in": "query",
            "description": "每页大小",
            "schema": { "type": "integer", "format": "int32", "default": 10 }
          },
          {
            "name": "sort",
            "in": "query",
            "description": "指定排序字段, 格式为: property,(asc|desc)",
            "schema": { "type": "array", "items": { "type": "string" } }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": { "$ref": "#/components/schemas/FormatPageUserRecord" }
              }
            }
          }
        }
      }
    },
    "/v1/system/users/{id}/account": {
      "patch": {
        "tags": ["User"],
        "summary": "修改用户账户状态",
        "operationId": "accountStatusSetting",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "required": true,
            "schema": { "type": "integer", "format": "int64" }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": { "$ref": "#/components/schemas/AccountStateDTO" }
            }
          },
          "required": true
        },
        "responses": {
          "204": { "description": "No Content" }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "UserQuery": {
        "type": "object",
        "properties": {
          "name": { "type": "string" },
          "account": { "type": "string" },
          "state": { "type": "string", "enum": ["disable", "enable"] },
          "gender": { "type": "string", "enum": ["man", "woman", "unknown"] }
        }
      },
      "AccountStateDTO": {
        "type": "object",
        "properties": {
          "state": { "type": "string", "enum": ["disable", "enable"] }
        },
        "required": ["state"]
      },
      "FormatPageUserRecord": {
        "type": "object",
        "properties": {
          "empty": { "type": "boolean" },
          "content": {
            "type": "array",
            "items": { "$ref": "#/components/schemas/UserRecord" }
          },
          "offset": { "type": "integer", "format": "int64" },
          "total": { "type": "integer", "format": "int64" },
          "next": { "type": "boolean" },
          "limit": { "type": "integer", "format": "int32" },
          "first": { "type": "boolean" }
        }
      },
      "UserRecord": {
        "type": "object",
        "properties": {
          "id": { "type": "integer", "format": "int64" },
          "name": { "type": "string" },
          "gender": { "type": "string", "enum": ["man", "woman", "unknown"] },
          "remark": { "type": "string" },
          "account": { "$ref": "#/components/schemas/UserAccountRecord" }
        }
      },
      "UserAccountRecord": {}
    }
  }
}
```

> **注意**：`UserAccountRecord` 为空 schema `{}`。如果 `output` 引用 `content[].account`，编译阶段会因空 schema 报错。后端需补全此定义。

### 源 compose.json（业务模块声明）

每个模块提供 `META-INF/ping/ui/compose.json`，内容为 operation/function 数组（不需要 `type: "app"` 包装）：

```json
[
  {
    "type": "operation",
    "id": "findUsers",
    "properties": {
      "api": { "$ref": "openapi://findUsers" },
      "defaults": {
        "request": { "state": "enable" }
      },
      "sortable_fields": ["account", "create_time"]
    }
  },
  {
    "type": "operation",
    "id": "accountStatusSetting",
    "properties": {
      "api": { "$ref": "openapi://accountStatusSetting" }
    }
  },
  {
    "type": "function",
    "id": "system/user",
    "children": [
      {
        "type": "view",
        "id": "user-list",
        "properties": {
          "operation": "findUsers"
        },
        "children": [
          { "type": "input", "id": "f.name", "properties": { "$ref": "openapi://findUsers/request/query/name" } },
          { "type": "input", "id": "f.state", "properties": { "$ref": "openapi://findUsers/request/query/state" } },
          { "type": "output", "id": "c.account", "properties": { "$ref": "openapi://findUsers/response/content/items/account" } },
          { "type": "output", "id": "c.name", "properties": { "$ref": "openapi://findUsers/response/content/items/name" } },
          { "type": "output", "id": "c.gender", "properties": { "$ref": "openapi://findUsers/response/content/items/gender" } }
        ]
      },
      {
        "type": "form",
        "id": "edit-user",
        "properties": {
          "operation": "accountStatusSetting"
        },
        "children": [
          { "type": "input", "id": "f.state", "properties": { "$ref": "openapi://accountStatusSetting/request/body/state" } }
        ]
      }
    ]
  }
]
```

多个模块的 compose.json 最终合并到 `app.children` 下返回。

### 编译阶段处理过程

```
Step 1: openapi:// → OpenAPI 操作描述

"$ref": "openapi://findUsers"
      ↓ 提取 operationId = findUsers
      ↓ 在 OpenAPI 文档中按 operationId 查找
      ↓ 找到 paths./v1/system/users.get
      ↓
{
  "method": "GET",
  "path": "/v1/system/users",
  "request": {
    "query": { "$ref": "#/components/schemas/UserQuery" },   ← 需展开
    "offset": { "type": "integer", "default": 1 },
    "size": { "type": "integer", "default": 10 },
    "sort": { "type": "array", "items": { "type": "string" } }
  },
  "response": {
    "content": { "type": "array", "items": { "$ref": "#/components/schemas/UserRecord" } },  ← 需展开
    "offset": { "type": "integer" },
    "total": { "type": "integer" },
    "limit": { "type": "integer" }
  }
}
```

```
Step 2: query $ref 展开

query: { "$ref": "#/components/schemas/UserQuery" }
      ↓ 展开为
query: {
  "type": "object",
  "properties": {
    "name": { "type": "string" },
    "account": { "type": "string" },
    "state": { "type": "string", "enum": ["disable", "enable"] },
    "gender": { "type": "string", "enum": ["man", "woman", "unknown"] }
  }
}
```

```
Step 3: 分页自动检测 + 物化

request 有 offset + size，response 有 content + total
      ↓ 检测到分页模式
      ↓ 生成 type: "input" 组件（offset/size），插入 view children
      ↓ 生成 type: "pagination" 容器组件，children 为 output 子节点

自动生成 input 组件:
{ "type": "input", "id": "page.offset", "properties": { "$ref": "openapi://findUsers/request/offset" } }
{ "type": "input", "id": "page.size", "properties": { "$ref": "openapi://findUsers/request/size" } }
{ "type": "input", "id": "sort.user-list", "properties": { "$ref": "openapi://findUsers/request/sort" } }   ← 排序 input 同样自动生成，不带 value（D4）

生成的 pagination 容器:
{
  "type": "pagination",
  "id": "page.user-list",
  "properties": {
    "mode": "page",
    "offset": { "$ref": "openapi://findUsers/request/offset" },
    "size": { "$ref": "openapi://findUsers/request/size" },
    "total": { "$ref": "openapi://findUsers/response/total" },
    "content": { "$ref": "openapi://findUsers/response/content" }
  },
  "children": [
    { "type": "output", "id": "c.account", "properties": { "$ref": "openapi://findUsers/response/content/items/account" } },
    { "type": "output", "id": "c.name", "properties": { "$ref": "openapi://findUsers/response/content/items/name" } },
    { "type": "output", "id": "c.gender", "properties": { "$ref": "openapi://findUsers/response/content/items/gender" } }
  ]
}
      ↓ 在 view 的 children 中，output 被包裹进 pagination 容器
      ↓ offset/size 作为 input 与 query、sort 并列
      ↓ 前端看到 pagination → 渲染分页器，children 即为行数据的 output 描述
      ↓ mode: "page" → 按页分页器；mode: "scroll" → 滑动加载
```

```
Step 4: openapi:// 字段路径 → UUID，写入被引用字段的 id 属性

"$ref": "openapi://findUsers/request/query/name"
      ↓ 提取 operationId = findUsers
      ↓ 在已展开的 operation 中定位 request.query.name
      ↓ 找到 UserQuery.name 字段
      ↓ 生成确定性哈希 UUID: a1b2c3d4
      ↓ 在 name 字段上写入 "id": "a1b2c3d4"
      ↓ 替换 $ref 值为 "a1b2c3d4"

"$ref": "openapi://findUsers/response/content/items/account"
      ↓ 提取 operationId = findUsers
      ↓ 在已展开的 operation 中定位 response.content.items.account
      ↓ 找到 UserRecord.account 字段
      ↓ 生成确定性哈希 UUID: f6g7h8i9
      ↓ 在 account 字段上写入 "id": "f6g7h8i9"
```

```
Step 5: 合并默认值

defaults.request.state = "enable"
      ↓ 写入 request.query.state.default = "enable"
      ↓
state: { "type": "string", "enum": ["disable","enable"], "default": "enable" }
```

### 编译后最终 JSON（前端拿到的）

多个模块的 compose.json 合并到 `app.children` 下返回：

```json
{
  "type": "app",
  "id": "ping.app",
  "children": [
    {
      "type": "operation",
      "id": "findUsers",
      "properties": {
        "method": "GET",
        "path": "/v1/system/users",
        "request": {
          "query": {
            "type": "object",
            "properties": {
              "name": { "type": "string", "id": "a1b2c3d4" },
              "account": { "type": "string" },
              "state": { "type": "string", "enum": ["disable","enable"], "id": "b2c3d4e5", "default": "enable" },
              "gender": { "type": "string", "enum": ["man","woman","unknown"] }
            }
          },
          "offset": { "type": "integer", "default": 1, "id": "d4e5f6a1" },
          "size": { "type": "integer", "default": 10, "id": "e5f6g7b2" },
          "sort": { "type": "array", "items": { "type": "string" }, "id": "c3d4e5f6" }
        },
        "response": {
          "content": {
            "type": "array",
            "items": {
              "type": "object",
              "properties": {
                "id": { "type": "integer" },
                "name": { "type": "string", "id": "d4e5f6g7" },
                "gender": { "type": "string", "enum": ["man","woman","unknown"], "id": "e5f6g7h8" },
                "remark": { "type": "string" },
                "account": { "id": "f6g7h8i9" }
              }
            },
            "id": "f6g7h8c3"
          },
          "offset": { "type": "integer" },
          "total": { "type": "integer", "id": "g7h8i9d4" },
          "limit": { "type": "integer" }
        },
        "sortable_fields": ["account", "create_time"]
      }
    },
    {
      "type": "operation",
      "id": "accountStatusSetting",
      "properties": {
        "method": "PATCH",
        "path": "/v1/system/users/{id}/account",
        "request": {
          "path": {
            "id": { "type": "integer", "id": "j0k1l2m3" }
          },
          "body": {
            "type": "object",
            "properties": {
              "state": { "type": "string", "enum": ["disable","enable"], "id": "h9i0j1k2" }
            }
          }
        },
        "response": {}
      }
    },
    {
      "type": "function",
      "id": "system/user",
      "children": [
        {
          "type": "view",
          "id": "user-list",
          "children": [
            { "type": "input", "id": "f.name", "properties": { "$ref": "a1b2c3d4" } },
            { "type": "input", "id": "f.state", "properties": { "$ref": "b2c3d4e5" } },
            { "type": "input", "id": "sort.user-list", "properties": { "$ref": "c3d4e5f6" } },
            { "type": "input", "id": "page.offset", "properties": { "$ref": "d4e5f6a1" } },
            { "type": "input", "id": "page.size", "properties": { "$ref": "e5f6g7b2" } },
            {
              "type": "pagination",
              "id": "page.user-list",
              "properties": {
                "mode": "page",
                "offset": { "$ref": "d4e5f6a1" },
                "size": { "$ref": "e5f6g7b2" },
                "total": { "$ref": "g7h8i9d4" },
                "content": { "$ref": "f6g7h8c3" }
              },
              "children": [
                { "type": "output", "id": "c.account", "properties": { "$ref": "f6g7h8i9" } },
                { "type": "output", "id": "c.name", "properties": { "$ref": "d4e5f6g7" } },
                { "type": "output", "id": "c.gender", "properties": { "$ref": "e5f6g7h8" } }
              ]
            }
          ]
        },
        {
          "type": "form",
          "id": "edit-user",
          "children": [
            { "type": "input", "id": "f.id", "properties": { "$ref": "j0k1l2m3" } },
            { "type": "input", "id": "f.state", "properties": { "$ref": "h9i0j1k2" } }
          ]
        }
      ]
    }
  ]
}
```

### path 参数处理

path 参数（如 `accountStatusSetting` 的 `{id}`）由编译器自动生成 input 组件，与 query/body 参数一致：

```json
{
  "type": "input",
  "id": "f.id",
  "properties": { "$ref": "openapi://accountStatusSetting/request/path/id" }
}
```

前端渲染时：path 参数生成隐藏 input 控件，调用时自动从行上下文（如 `$row.id`）取值。

### 204 响应处理

`accountStatusSetting` 只有 204 响应（无 response body），编译后 `response: {}`：

- 编译成功（204 = 成功但无可解析字段）
- 如果有 `output` 引用 `accountStatusSetting.response` 的任何字段 → 编译失败（断链）

详见 `design/phase-1-compiler-core.md`、`design/phase-2-backend-api.md`、`design/phase-3-cleanup.md`

## 阶段设计文档索引

| 阶段 | 文档 | 状态 |
|---|---|---|
| 1. 编译器核心 | design/phase-1-compiler-core.md | 已细化 |
| 2. 后端聚合接口 | design/phase-2-backend-api.md | 已细化 |
| 3. 旧代码清理 | design/phase-3-cleanup.md | 已细化 |

## Risks / Trade-offs

- **[风险]** compose.json 格式破坏性变更，但**不做迁移**（无迁移指南/脚本/fallback）——存量文件直接删除，业务侧按新规范自行声明
- **[风险]** `$ref` 引用语法从自定义路径改为 `openapi://` 协议
- **[权衡]** 编译时校验导致启动失败，可能影响开发体验 → 报错信息包含具体引用路径与声明来源，便于快速定位；未知类型/属性走忽略（D6）
- **[权衡]** 不支持运行时动态加载组件描述 → 当前无此需求，如有需要可后续扩展
- **[风险]** OpenAPI 中 `$ref` 嵌套可能导致编译时循环引用 → 编译阶段检测循环引用并报错
- **[风险]** UserAccountRecord 为空 schema `{}`，output 引用它时前端无法推断控件类型 → 被引用的空 schema 报错并要求后端补全（未被引用的忽略）
- **[风险]** DDD 重构可能影响现有测试 → 保留现有测试，逐步迁移到新架构
- **[风险]** 未来第一个新 compose.json 落地时，ping-system 的 4 个 `@SpringBootTest` 会因缺 openapi 文件启动失败 → 与散装启动问题合并，归后续提案
- **[风险]** function 挂接关系若被完全重新设计，可能触及 function/view/form 结构 → 已按可增量方向（function 不携带 operation）落地，降低返工面

## Migration Plan

1. 将所有 `$ref` 引用统一到 `openapi://` 协议风格
2. 重写 `ping-ui` SDK 核心为编译/组装器，引入 DDD 架构
3. 实现后端 `GET /v1/ui/components` 聚合接口
4. **删除**存量旧格式文件 `ping-system/src/main/resources/ui/user-center/compose.json`（死文件：路径不在扫描范围、模型是旧 dataset，不做迁移，业务侧以后按新规范自行声明）
5. 前端只做减法：移除 SDK 机制、保留 `createPingUi` 占位 entry（D19）
6. 移除旧 compiler/exception 代码

**不做回滚兜底**：不保留旧 compose.json 解析逻辑、不设 feature flag——旧格式本就无法被现行代码加载成功，回滚对象不存在。

## Open Questions

- ⏸️ **function 与 operation 的挂接关系**：可能完全重新设计（见 D13 遗留标记）；当前按「只有 view/form 挂 operation、function 不携带」落地
- ⏸️ **分模块 openapi 导出的互斥规则**：全量文件（distribute）与部分文件（模块）并存时 operation id 重复会锁死聚合启动，分模块导出提案必须先解决（见 D7）
- ⏸️ **散装启动问题**：业务模块独立运行时有 compose.json、无 openapi 文件 → 启动失败，豁免方案归后续提案
- ⏸️ **测试影响**：未来第一个新 compose.json 落地时，ping-system 的 4 个 `@SpringBootTest` 会因缺 openapi 文件启动失败（与散装问题同案，一并归后续提案；本次因旧 compose.json 被删除，测试不受影响）
