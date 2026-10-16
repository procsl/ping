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
- **DDD 代码架构**：引入 `Component` 组合类型、`App` 应用根、`ComposeService` 领域服务、`ComposeCompiler` 编译器、`ComposeRepository` 仓储
- **开闭原则**：新增编译阶段只需实现 `CompilePhase` 接口，不修改已有代码

**Non-Goals:**
- 不实现前端渲染器（各端自行实现）
- 不实现组件通信机制（前端内部事务）
- 不支持运行时动态加载组件描述
- 不声明 UI 展示细节（布局、样式、图标等）

## Decisions

### D1: 组件类型系统从 18 种精简到 8 种

**决策**：只保留 `app`、`operation`、`function`、`view`、`form`、`input`、`output`、`placeholder`。

**理由**：移除的 10 种类型（layout、nav_group、menu、breadcrumb、tabs、user_info_panel、main_container、row_action、form_action、empty）都是前端壳或 UI 细节，不属于"功能声明"。`row_action` 和 `form_action` 统一为 `input`（引用 operation.request 字段），`empty` 由前端自行处理。

**备选方案**：保留更多类型以覆盖更多场景 → 被拒绝，违背克制原则。

### D2: operation 作为接口契约中心

**决策**：`operation` 是接口契约的定义点，包含 `method`/`path`/`request`/`response`/`defaults`/`sortable_fields`。`input`/`output` 通过 `$ref`（`openapi://` 协议）引用 operation 的字段。

**理由**：将接口描述集中管理，避免重复声明。前端可从 operation 生成可调用方法。编译时在被引用字段上插入 UUID 作为唯一标识。

**备选方案**：
- 每个 input/output 内联完整 schema → 被拒绝，重复且难以维护
- 保持引用不解析 → 被拒绝，前端需要运行时解析

### D3: 编译时解析 $ref → UUID，嵌入字段 id 属性

**决策**：编译阶段将 `openapi://` 字段路径解析为 UUID，替换 `$ref` 值，并在被引用字段的 `id` 属性上写入该 UUID。

**理由**：
- 编译时校验所有引用完整性，启动失败而非运行时降级
- 前端不需要理解路径引用，只需查 UUID
- UUID 嵌入字段本身，零额外结构

**备选方案**：
- 维护独立的 UUID 映射表 → 被拒绝，增加结构复杂度
- 使用有意义的别名 → 被拒绝，用户明确说"无需有意义，随机 id 即可"

### D4: 排序作为 input 处理

**决策**：排序是一种特殊的 `input`，通过 `$ref`（`openapi://` 协议）引用 operation.request 中的排序字段。排序规则定义在 operation 的 `sortable_fields` 列表中。

**理由**：排序本质上是"传给接口的参数"，和查询条件、分页没有本质区别。统一为 `input` 保持模型一致性。

**备选方案**：在 output 上声明 `sortable: true` → 被拒绝，output 不应该关心行为，且需要后端支持。

### D5: $ref 使用 openapi:// 协议引用 operation

**决策**：`$ref` 使用自定义协议 `openapi://<operationId>` 引用 OpenAPI 操作（如 `openapi://findUsers`）。编译器按 `operationId` 在 OpenAPI 文档中查找对应 path + method。冲突时追加类型+方法名。

**理由**：
- `openapi://` 协议简洁直观，避免 JSON Pointer 长路径（如 `#/paths/~1v1~1system~1users/get`）
- 字段引用使用 `openapi://<operationId>/<request|response>/<path>` 语义路径，不依赖数组索引
- `operationId` 是机器生成的唯一标识，比 `summary`（人类可读文案）更稳定
- 冲突时追加类型+方法名保证唯一性

**备选方案**：
- 使用 JSON Pointer（`#/paths/~1v1~1system~1users/get`）→ 被拒绝，路径过长且数组索引不稳定
- 匹配 summary → 被拒绝，summary 可能重复且不稳定

### D6: 编译阶段全量校验，失败则启动失败

**决策**：任何引用断链、空 schema、非法属性都导致启动失败。

**理由**：断链意味着声明有错误，运行时降级会隐藏问题。启动失败强制开发者修复。

**备选方案**：运行时降级 + WARN → 被拒绝，与"启动永不失败"的旧不变量冲突，但新设计认为声明错误应该尽早暴露。

### D7: 后端聚合器扫描 classpath 收集 compose.json

**决策**：启动时扫描 classpath 下所有 `META-INF/ping/ui/compose.json`，每个文件内容为 operation/function 数组（不需要 `type: "app"` 包装），合并编译为最终 app JSON，缓存为只读快照。

**理由**：各业务模块独立提供 compose.json，后端聚合器统一编译并合并为 app.children。与现有"统一落 META-INF/ping/"的约定一致。

**备选方案**：构建期聚合 → 被拒绝，当前构建期聚合链路已移除，且启动期聚合更灵活。

### D8: platform 过滤在组装时注入

**决策**：前端请求时携带平台标识（query param 或 header），后端在返回时过滤不支持的组件。

**理由**：同一份 compose.json 在不同平台可能有不同可用性，过滤逻辑由后端控制。

**备选方案**：在 compose.json 中声明 `platform` 属性 → 被拒绝，用户明确说"组装时声明即可"。

### D9: 分页自动检测 + 物化为 pagination 容器 + input 组件

**决策**：编译阶段检测 operation 的 request 是否含 `offset` + `size`、response 是否含 `content` + `total`。检测到后：
1. 生成 `type: "pagination"` 容器组件，children 为该分页接口的 output 子节点，`mode` 属性区分 `page`（按页分页）或 `scroll`（滑动分页），默认 `page`
2. 自动生成 `type: "input"` 组件（`offset` 和 `size`），插入到 view 的 children 中，与 query、sort 并列

**理由**：分页是后台系统的固定模式（offset/size 请求 + content/total 响应），应该由编译器自动物化。pagination 作为容器包裹 output 子节点，前端既知道数据来自分页接口，又能针对性渲染分页器。`mode` 让前端选择按页分页器还是滑动加载。分页参数自动生成为 input，前端无需额外推断。

**备选方案**：
- 标记 `operation.paged = true`，前端自行推断 → 被拒绝，前端需要额外推断逻辑
- 在 compose.json 中显式声明分页组件 → 被拒绝，违背克制原则

### D10: DDD 代码架构——组合类型 + 领域服务 + 编译器

**决策**：引入 DDD 代码架构，包结构如下：

```
cn.procsl.ping.boot.ui
├── domain/
│   ├── Component.java         # 组合类型（type + id + properties + children）
│   └── App.java               # 应用根，包含编译后的 Component 树
├── compiler/
│   ├── ComposeCompiler.java   # 编译器（List<JsonNode> → List<Component>）
│   ├── OperationCompiler.java # operation 契约构建（解析 + 展开 + 默认值）
│   ├── ComponentCompiler.java # 组件树物化（UUID + 分页）
│   ├── CompilePhase.java      # 编译阶段接口
│   ├── CompileContext.java    # 编译上下文
│   └── Schemas.java           # 常量
├── service/
│   └── ComposeService.java    # 领域服务，调用 Compiler，返回 App
├── repository/
│   └── ComposeRepository.java  # 返回 List<JsonNode>
└── exception/
    ├── ComposeException.java
    └── ComposeParseException.java
```

**理由**：
- `Component` 采用组合优于继承，不同类型只是 `type` 字段不同
- `ComposeService` 作为领域服务，串联 Repository → Compiler → App
- `ComposeCompiler` 负责编译流程编排，返回类型安全的 `List<Component>`
- `ComposeRepository` 只负责数据访问，返回原始 `List<JsonNode>`
- 开闭原则：新增编译阶段只需实现 `CompilePhase` 接口

**备选方案**：
- 保持现有扁平结构 → 被拒绝，职责混乱、命名不清晰
- 引入 Component 继承层次 → 被拒绝，用户明确说"组合优于继承"

### D11: compiler 包整合——按职责域整合

**决策**：将现有 6 个类整合为 3 个：

| 新类 | 整合来源 | 职责 |
|---|---|---|
| `ComposeCompiler` | 新增 | 主编排器，协调各编译阶段 |
| `OperationCompiler` | `OperationRefResolver` + `SchemaRefExpander` + `DefaultResolver` | operation 契约完整构建：解析 → 展开 → 默认值 |
| `ComponentCompiler` | `RefResolver` + `PaginationMaterializer` | 组件树物化：UUID + 分页 |

**理由**：
- 按职责域整合，而非按编译阶段拆分
- operation 相关的操作（解析、展开、默认值）都是围绕 operation 契约的
- 组件树相关的操作（UUID、分页）都是围绕组件树的
- 命名统一为 `Compiler` 后缀，清晰一致

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

## 调用流程

```
┌─────────────────────────────────────────────────────────────┐
│                  ComposeService.compile()                    │
│                                                              │
│  1. ComposeRepository.loadAll() → List<JsonNode>            │
│  2. ComposeCompiler.compile(List<JsonNode>) → List<Component>│
│  3. new App(List<Component>) → App                          │
│                                                              │
│  输出: App（包含编译后的 Component 树）                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    ComposeCompiler.compile()                 │
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
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ Phase 2: ComponentCompiler                             │  │
│  │                                                        │  │
│  │  遍历所有 function/view/form 组件:                     │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │ 1. 字段路径 → UUID                               │  │  │
│  │  │ 2. 分页物化                                       │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  │                                                        │  │
│  │  结果: 编译后的 List<Component>（$ref 已替换为 UUID）  │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  输出: List<Component>（编译后的组件树）                      │
└─────────────────────────────────────────────────────────────┘
```

## 注意事项

1. **开闭原则**：新增编译阶段只需实现 `CompilePhase` 接口，注册到 `ComposeCompiler`，不修改已有代码
2. **类型安全**：`Component` 采用组合类型，不同类型只是 `type` 字段不同，不引入继承层次
3. **职责清晰**：Repository 只负责数据访问，Compiler 负责编译逻辑，Service 负责编排
4. **异常精简**：只保留 2 个异常类，通过错误消息区分不同错误场景
5. **命名规范**：统一使用 `Compiler` 后缀，避免 Resolver/Expander/Materializer 等混乱命名

## 测试原则

1. **单元测试**：每个 Compiler 阶段都有对应的单元测试
2. **集成测试**：`ComposeCompiler` 的端到端测试，验证完整编译流程
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
          { "type": "output", "id": "c.gender", "properties": { "$ref": "openapi://findUsers/response/content/items/gender" } },
          { "type": "input", "id": "sort.account", "properties": { "$ref": "openapi://findUsers/request/sort", "value": "account,asc" } }
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
      ↓ 生成 UUID: a1b2c3d4
      ↓ 在 name 字段上写入 "id": "a1b2c3d4"
      ↓ 替换 $ref 值为 "a1b2c3d4"

"$ref": "openapi://findUsers/response/content/items/account"
      ↓ 提取 operationId = findUsers
      ↓ 在已展开的 operation 中定位 response.content.items.account
      ↓ 找到 UserRecord.account 字段
      ↓ 生成 UUID: f6g7h8i9
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
            "id": { "type": "integer" }
          },
          "body": {
            "type": "object",
            "properties": {
              "state": { "type": "string", "enum": ["disable","enable"] }
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
            { "type": "input", "id": "sort.account", "properties": { "$ref": "c3d4e5f6", "value": "account,asc" } },
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
            { "type": "input", "id": "f.state", "properties": { "$ref": "openapi://accountStatusSetting/request/body/state" } }
          ]
        }
      ]
    }
  ]
}
```

### path 参数处理

path 参数（如 `accountStatusSetting` 的 `{id}`）由 SDK 根据配置在运行时处理。operation 中包含 path 参数定义，前端调用时从上下文（如当前行数据）取值：

```json
{
  "type": "operation",
  "id": "accountStatusSetting",
  "properties": {
    "method": "PATCH",
    "path": "/v1/system/users/{id}/account",
    "request": {
      "path": {
        "id": { "type": "integer" }
      }
    }
  }
}
```

前端渲染时：path 参数不生成输入控件，调用时自动从行上下文（如 `$row.id`）取值。

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

- **[风险]** compose.json 格式破坏性变更，所有业务模块需迁移 → 提供迁移指南和自动化脚本
- **[风险]** `$ref` 引用语法从自定义路径改为 `openapi://` 协议
- **[权衡]** 编译时校验导致启动失败，可能影响开发体验 → 报错信息包含具体引用路径，便于快速定位
- **[权衡]** 不支持运行时动态加载组件描述 → 当前无此需求，如有需要可后续扩展
- **[风险]** OpenAPI 中 `$ref` 嵌套可能导致编译时循环引用 → 编译阶段检测循环引用并报错
- **[风险]** UserAccountRecord 为空 schema `{}`，output 引用它时前端无法推断控件类型 → 编译阶段检测空 schema 并报错，要求后端补全
- **[风险]** DDD 重构可能影响现有测试 → 保留现有测试，逐步迁移到新架构

## Migration Plan

1. 将所有 `$ref` 引用统一到 `openapi://` 协议风格
2. 重写 `ping-ui` SDK 核心为编译/组装器，引入 DDD 架构
3. 实现后端 `GET /v1/ui/components` 聚合接口
4. 迁移所有业务模块 compose.json 到新格式
5. 前端渲染器重写为静态 JSON 驱动
6. 移除旧 SDK 代码（注册表、生命周期、消息总线）

回滚策略：保留旧 compose.json 解析逻辑作为 fallback，通过 feature flag 切换。

## Open Questions

- 无
