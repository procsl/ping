# Spec Delta

## Purpose

定义抽象 UI 声明语言的组件类型系统、引用语法与编译规则，使 JSON 只声明功能组件及其与 operation 的输入/输出映射。

## ADDED Requirements

### Requirement: 组件类型系统

系统 SHALL 支持以下 8 种组件类型，每种类型有明确的职责边界：

| 类型 | 职责 | 是否渲染 |
|---|---|---|
| `app` | 根节点，唯一由 `ping-ui` 提供 | 否 |
| `operation` | 接口契约定义（method/path/request/response） | 否 |
| `function` | 功能入口（唯一标识 id + children） | 是 |
| `view` | 数据视图（读），包含 input/output 子节点 | 是 |
| `form` | 表单（写），包含 input 子节点 | 是 |
| `input` | 输入控件，通过 `$ref`（`openapi://` 协议）引用 operation.request 字段 | 是 |
| `output` | 展示控件，通过 `$ref`（`openapi://` 协议）引用 operation.response 字段 | 是 |
| `placeholder` | 占位，表示功能建设中 | 是 |

系统 SHALL NOT 在此类型系统之外渲染任何未注册的类型。

#### Scenario: 合法组件树
- **WHEN** compose.json 包含上述 8 种类型的组件树
- **THEN** 编译成功，前端按类型渲染

#### Scenario: 未注册类型降级
- **WHEN** compose.json 包含未知类型
- **THEN** 编译阶段报错，启动失败

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

### Requirement: $ref 引用与编译时解析

`input` 和 `output` 组件 SHALL 通过 `properties.$ref` 引用 operation 的字段。编译阶段 SHALL 将 `$ref` 解析为 UUID，并插入到被引用字段的 `id` 属性上。

`$ref` 语法为 `openapi://` 协议：
- operation 级别：`openapi://<operationId>`（如 `openapi://findUsers`）
- 字段级别：`openapi://<operationId>/<request|response>/<path>`（如 `openapi://findUsers/request/query/name`）

#### Scenario: $ref 解析为 UUID
- **WHEN** input 的 $ref 为 `openapi://findUsers/request/query/name`
- **THEN** 编译阶段生成 UUID，替换 $ref 值，并在 UserQuery.name 字段上写入 id 属性

#### Scenario: $ref 断链
- **WHEN** $ref 指向不存在的 operation 或字段
- **THEN** 编译阶段报错，启动失败

### Requirement: 默认值优先级

系统 SHALL 支持三级默认值，优先级为：**实际调用值 > 覆盖默认值 > operation 默认值**。

`operation.properties.defaults` 声明接口级默认值；`function` 或 `view`/`form` 可通过 `overrides` 覆盖。

#### Scenario: 默认值生效
- **WHEN** operation 声明 defaults.request.query.state = "enable"
- **THEN** 前端渲染时默认选中 "enable"

#### Scenario: 覆盖默认值
- **WHEN** function 声明 overrides.request.query.state = "disable"
- **THEN** 前端渲染时默认选中 "disable"（覆盖 operation 默认值）

### Requirement: 排序作为 input

排序 SHALL 视为一种 `input`，通过 `$ref` 引用 operation.request 中的排序字段。排序规则定义在 operation 的 `sortable_fields` 列表中。

#### Scenario: 可排序字段
- **WHEN** operation 声明 sortable_fields = ["account", "create_time"]
- **THEN** 前端在对应 output 列上提供排序入口

#### Scenario: 不可排序字段
- **WHEN** input 引用了不在 sortable_fields 中的字段
- **THEN** 编译阶段报错，启动失败

### Requirement: 声明极致克制

compose.json SHALL NOT 携带任何 UI 展示属性（width、align、icon、order、format、enum 等）。这些属性由前端渲染器自动推断或硬编码。

#### Scenario: 无 UI 属性
- **WHEN** output 只包含 $ref 属性
- **THEN** 编译成功，前端根据 schema 类型自动选择展示方式

#### Scenario: 非法 UI 属性
- **WHEN** output 包含 width 或 align 属性
- **THEN** 编译阶段报错，启动失败

### Requirement: $ref 匹配规则

`$ref` SHALL 使用 `openapi://` 协议引用操作（如 `openapi://findUsers`）。编译器内部按 `operationId` 索引（非 summary）。若 operationId 冲突，SHALL 追加类型+方法名作为唯一标识。

#### Scenario: 正常匹配
- **WHEN** $ref 为 `openapi://findUsers`
- **THEN** 匹配到 GET /v1/system/users 的 operationId "findUsers"

#### Scenario: operationId 冲突
- **WHEN** 两个方法有相同 operationId
- **THEN** 追加类型+方法名（如 "UserController.findUsers"）
