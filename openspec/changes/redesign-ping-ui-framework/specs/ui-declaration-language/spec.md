# Spec Delta

## Purpose

定义抽象 UI 声明语言的组件类型系统、引用语法与编译规则，使 JSON 只声明功能组件及其与 operation 的输入/输出映射。

## ADDED Requirements

### Requirement: 组件类型系统

系统 SHALL 支持以下 8 种组件类型，每种类型有明确的职责边界：

| 类型 | 职责 | 是否渲染 |
|---|---|---|
| `app` | 根节点（ping-ui 提供） | 否 |
| `operation` | 接口契约（method/path/request/response） | 否 |
| `function` | 功能入口（children 非空） | 是 |
| `view` | 数据视图（读） | 是 |
| `form` | 表单（写） | 是 |
| `input` | 输入控件，`$ref` → operation.request 字段 | 是 |
| `output` | 展示控件，`$ref` → operation.response 字段 | 是 |
| `pagination` | 分页容器（编译器自动生成，见「分页物化」） | 是 |

系统 SHALL NOT 在此类型系统之外渲染未注册类型（未知类型处理见「编译校验分级」）。无占位类型——未实现的功能不声明（不声明 = 不存在）。

#### Scenario: 合法组件树
- **WHEN** compose.json 包含上述 8 种类型的组件树
- **THEN** 编译成功，前端按类型渲染

### Requirement: operation 结构定义

`operation` 组件 SHALL 包含以下结构：

```json
{
  "type": "operation",
  "id": "<唯一标识>",
  "properties": {
    "method": "<HTTP 方法>",
    "path": "<URL 路径>",
    "request": {
      "path": { ... },
      "query": { ... },
      "body": { ... }
    },
    "response": { ... },
    "defaults": { ... },
    "sortable_fields": [...]
  }
}
```

`request` 中的 `path`/`query`/`body` SHALL 按 OpenAPI 参数位置分类。`response` 为空对象 `{}` 时表示 204 成功响应（无返回字段）。

#### Scenario: 完整 operation
- **WHEN** operation 包含 method/path/request/response
- **THEN** 编译成功，前端可调用

#### Scenario: 204 响应
- **WHEN** operation 的 response 为空对象
- **THEN** 编译成功，表示成功但无可解析字段

#### Scenario: operation id 重复
- **WHEN** 多个 compose.json 声明了相同 id 的 operation（或 OpenAPI 中缺失 operationId）
- **THEN** 编译阶段报错，启动失败，报错信息包含两处声明的来源（模块文件名）

### Requirement: 组件结构约束

`function` 的 `children` SHALL 非空——`function` 至少包含一个 `view` 或 `form` 子节点，否则编译报错、启动失败。动作类功能（如「重置密码」）SHALL 表达为 `function` 下挂 `form`，不单独引入动作类型。

`view` 与 `form` SHALL 各关联且仅关联一个 operation（`properties.operation`）：一个 view 读一个接口，一个 form 提交一个接口。一屏多接口 SHALL 用 `function` 下挂多个 view/form 表达。`view` 的子节点 SHALL 为 `input`/`output`（含编译器自动生成的分页/排序 input），`form` 的子节点 SHALL 为 `input`。

> ⏸️ **遗留（待重新设计）**：`function` 自身是否携带 `properties.operation`（design D13 原文与示例互相矛盾）尚未定论，可能完全重新设计。当前实现以「只有 view/form 挂 operation、function 不携带」为准；这是唯一可增量演进的方向（将来加可选属性不破坏，反之则返工）。

#### Scenario: 空 function
- **WHEN** function 不含任何 children
- **THEN** 编译阶段报错，启动失败

#### Scenario: 动作功能表达为 form
- **WHEN** 需要声明「重置密码」类动作功能
- **THEN** 声明为 function 下挂 form（form 关联对应 operation）

### Requirement: $ref 引用与编译时解析

`input` 和 `output` 组件 SHALL 通过 `properties.$ref` 引用 operation 的字段。编译阶段 SHALL 将 `$ref` 解析为 UUID，并插入到被引用字段的 `id` 属性上。

`$ref` 语法为 `openapi://` 协议：
- operation 级别：`openapi://<operationId>`（如 `openapi://findUsers`）
- 字段级别：`openapi://<operationId>/<request|response>/<path>`（如 `openapi://findUsers/request/query/name`）

字段路径 SHALL 逐段下钻；数组包装层 SHALL 使用固定段名 `items` 继续下钻到元素定义，即 `response/content/items/<field>` 等价于 `response.content.items.properties.<field>`。数组字段上缺 `items` 段（或多写了 `items` 段）SHALL 视为断链。

#### Scenario: $ref 解析为 UUID
- **WHEN** input 的 $ref 为 `openapi://findUsers/request/query/name`
- **THEN** 编译阶段生成 UUID，替换 $ref 值，并在 UserQuery.name 字段上写入 id 属性

#### Scenario: 数组元素字段引用
- **WHEN** output 的 $ref 为 `openapi://findUsers/response/content/items/account`
- **THEN** 编译阶段解析到 `FormatPageUserRecord.content.items`（即 `UserRecord`）的 `account` 字段并物化

#### Scenario: 数组路径缺少 items 段
- **WHEN** output 的 $ref 为 `openapi://findUsers/response/content/account`（content 是数组）
- **THEN** 编译阶段按断链报错，启动失败

#### Scenario: $ref 断链
- **WHEN** $ref 指向不存在的 operation 或字段
- **THEN** 编译阶段报错，启动失败

### Requirement: 编译校验分级

编译阶段 SHALL 按下表分级处理，避免把不可枚举的情况变成启动阻塞：

| 情况 | 处理 |
|---|---|
| `$ref` 断链（operation 或字段不存在、数组路径缺 `items`） | 报错，启动失败 |
| 空 schema `{}` 且**被** input/output 引用 | 报错，启动失败（前端无法推断控件） |
| 空 schema `{}` 未被引用 | 忽略 |
| operation / function id 重复 | 报错，启动失败（带两处声明来源） |
| view/form 未关联 operation、operation 缺 operationId | 报错，启动失败 |
| `sortable_fields` 中的字段不存在于 response | 报错，启动失败 |
| 未知组件类型 | 忽略 + debug 日志 |
| 未知/多余属性（width、align 等） | 忽略 + debug 日志（版本升级兼容） |

#### Scenario: 被引用的空 schema
- **WHEN** output 引用的字段 schema 为空对象 `{}`
- **THEN** 编译阶段报错，启动失败，报错信息包含引用路径

#### Scenario: 未知类型被忽略
- **WHEN** compose.json 包含类型系统之外的类型（如旧格式的 `dataset`、`placeholder`）
- **THEN** 编译阶段忽略该节点并打印 debug 日志，不报错、不影响启动

#### Scenario: 未知属性被忽略
- **WHEN** output 的 properties 中出现 `width` 或 `align`
- **THEN** 编译阶段忽略该属性并打印 debug 日志，不报错

### Requirement: 默认值优先级

系统 SHALL 支持三级默认值，优先级为：**实际调用值 > 覆盖默认值 > operation 默认值**。

`operation.properties.defaults` 声明接口级默认值；`function` SHALL 可通过 `overrides` 覆盖（依据 design D17，`view`/`form` 不允许声明 `overrides`，其默认值由编译生成）。

#### Scenario: 默认值生效
- **WHEN** operation 声明 defaults.request.query.state = "enable"
- **THEN** 前端渲染时默认选中 "enable"

#### Scenario: 覆盖默认值
- **WHEN** function 声明 overrides.request.query.state = "disable"
- **THEN** 前端渲染时默认选中 "disable"（覆盖 operation 默认值）

### Requirement: 排序作为 input

排序 SHALL 视为一种 `input`，由**编译器自动生成**（引用 operation 的排序参数，不带 value），开发者 SHALL NOT 手写排序 input。可排序字段 SHALL 在 operation 的 `sortable_fields` 列表中声明；`sortable_fields` 中的字段 SHALL 存在于 response，否则断链报错。默认排序如需指定，SHALL 通过 `defaults` 给出。前端按 `sortable_fields` 决定列头排序入口，点击时自行拼接 value。

#### Scenario: 可排序字段
- **WHEN** operation 声明 sortable_fields = ["account", "create_time"]
- **THEN** 编译器自动生成排序 input；前端在对应 output 列上提供排序入口

#### Scenario: sortable_fields 引用不存在的字段
- **WHEN** operation 的 sortable_fields 中包含 response 里不存在的字段
- **THEN** 编译阶段按断链报错，启动失败

### Requirement: 分页物化

编译阶段 SHALL 检测 operation 的 request 是否含 `offset` + `size`、response 是否含 `content` + `total`，并自动生成 `pagination` 容器：

- 检测到分页（request + response 均匹配）：生成 `type: "pagination"` 容器包裹 output 子节点（`mode: "page"`），并自动生成 `offset`/`size` 的 input（与 query、sort 并列）
- 仅 response 匹配（request 无 offset/size）：生成 `mode: "scroll"` 的 pagination 容器，**SHALL NOT 生成** offset/size input，容器属性中也不带 offset/size/content 引用（无字段可引用，生成即断链）

#### Scenario: 按页分页
- **WHEN** operation 的 request 含 offset+size、response 含 content+total
- **THEN** 编译生成 mode=page 的 pagination 容器和 offset/size input

#### Scenario: 滑动分页
- **WHEN** operation 的 response 含 content+total 但 request 无 offset/size
- **THEN** 编译生成 mode=scroll 的 pagination 容器，且不生成 offset/size input

### Requirement: 声明极致克制

compose.json SHALL NOT 携带任何 UI 展示属性（width、align、icon、order、format、enum 等）。这些属性由前端渲染器自动推断或硬编码。

#### Scenario: 无 UI 属性
- **WHEN** output 只包含 $ref 属性
- **THEN** 编译成功，前端根据 schema 类型自动选择展示方式

#### Scenario: 非法 UI 属性
- **WHEN** output 包含 width 或 align 属性
- **THEN** 编译阶段忽略该属性并打印 debug 日志，不报错、不影响启动（详见「编译校验分级」）

### Requirement: $ref 匹配规则

`$ref` SHALL 使用 `openapi://` 协议引用操作（如 `openapi://findUsers`）。编译器内部按 `operationId` 索引（非 summary）。若 operationId 冲突，SHALL 追加类型+方法名作为唯一标识。

#### Scenario: 正常匹配
- **WHEN** $ref 为 `openapi://findUsers`
- **THEN** 匹配到 GET /v1/system/users 的 operationId "findUsers"

#### Scenario: operationId 冲突
- **WHEN** 两个方法有相同 operationId
- **THEN** 追加类型+方法名（如 "UserController.findUsers"）
