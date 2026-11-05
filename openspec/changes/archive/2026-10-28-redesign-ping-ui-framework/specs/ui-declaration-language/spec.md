# Spec Delta

## Purpose

定义抽象 UI 声明语言的组件结构、operationId 引用与 openapi 内联规则，使 compose.json 只声明功能树与接口引用，字段细节全部来自 openapi。

## ADDED Requirements

### Requirement: 功能树结构

系统 SHALL 用统一节点构成功能树，每个节点 `{ type, id, properties, children }`，`type` 字段标识节点类型（app/page/function）：

```
{ type: "app",      id, properties, children: [page...] }
  { type: "page",   id, properties, children: [function...] }
    { type: "function", id, properties }
```

- `type: "app"` 根节点，`properties` SHALL 可扩展（如 `version`/`source`）
- `type: "page"` 功能页面容器，`id` SHALL 全局唯一
- `type: "function"` 接口功能，`id`（page 内唯一）+ `properties`（openapi 接口描述）

compose.json 源为节点数组（`type` 标识），其中 `type: "function"` SHALL 仅声明 `{ type, id, properties: { operationId } }`，字段细节 SHALL NOT 在源中声明。

#### Scenario: 合法功能树
- **WHEN** compose.json 包含 app > pages > function 三级结构
- **THEN** 编译成功，前端据产物生成菜单与 page

#### Scenario: page 无 name
- **WHEN** page 只声明 id、未声明 name
- **THEN** 合法，page 标识即 id

### Requirement: function 路由规则

function 的全局标识 / 前端路由 SHALL 为 `page.id + "/" + function.id`。`function.id` SHALL 仅需在 page 内唯一（开发者写短名），全局唯一性由 page 前缀保证。

#### Scenario: 路由生成
- **WHEN** page.id = "user"，function.id = "list"
- **THEN** 前端路由为 "user/list"

### Requirement: 扁平菜单

前端菜单 SHALL 为扁平结构：各 function 以路由标识（`page.id/function.id`）平铺为一级菜单项，SHALL NOT 按 page 分级。`page.id` SHALL 仅作路由前缀，不作分组标签。菜单显示名由前端自定，compose 不声明显示名。

#### Scenario: 菜单生成
- **WHEN** 功能树含多个 page 的多个 function
- **THEN** 前端菜单平铺所有 function（以路由标识），无分组层级，显示名前端自定

### Requirement: operationId 引用

`function` SHALL 通过 `operationId` 引用 openapi 中的 operation。同一 `operationId` SHALL 可被多个 page 的多个 function 引用（openapi operation 是唯一源，function 是其使用实例）。

#### Scenario: 同一 operationId 多引用
- **WHEN** page "user" 和 page "dashboard" 的 function 都引用 "findUsers"
- **THEN** 两个 function 各自内联一份完整描述，编译成功

### Requirement: openapi 内联

编译阶段 SHALL 按 `operationId` 定位 openapi operation，将 `method`/`path`/`parameters`/`requestBody`/`responses` 内联进 `function.properties`，展开所有 `$ref`。内联 SHALL 接受冗余，使 function 完全自包含、无 `$ref` 残留。

`function.properties` SHALL 保留 openapi operation 的结构形态，字段角色由 openapi 的天然锚点决定：`parameters[].in=path/query` → 输入、`requestBody` → 表单、`responses` → 展示。

#### Scenario: operation 内联
- **WHEN** function 引用 operationId "findUsers"
- **THEN** 编译产物的 function.properties 包含 findUsers 的 method/path/parameters/responses，schema 全内联

#### Scenario: 无 $ref 残留
- **WHEN** 编译完成
- **THEN** function.properties 中不存在任何 `$ref`，前端可单函数渲染与调用

### Requirement: 响应只保留 2xx

`function.properties.responses` SHALL 只序列化 2xx 状态码（200/201/204）的 schema，非 2xx（4xx/5xx）SHALL NOT 序列化。错误响应的处理归轻量 client（据 HTTP 状态码自判），不在本次产物范围。

#### Scenario: 只保留 2xx
- **WHEN** operation 有 200/400/500 状态码
- **THEN** function.properties.responses 只含 200 的 schema，400/500 不序列化

### Requirement: 编译校验

编译阶段 SHALL 校验以下结构性错误，任一 SHALL 导致启动失败：

| 情况 | 处理 |
|---|---|
| `page.id` 重复 | 报错，启动失败 |
| `function.id` 在 page 内重复 | 报错，启动失败 |
| `operationId` 在 openapi 中找不到 | 报错，启动失败 |
| openapi 内 operationId 冲突 | 报错，启动失败 |
| 未知属性（未来使用意图扩展字段） | 忽略 + debug 日志 |
| 未知类型（page/function 之外） | 忽略 + debug 日志 |

全有或全无——任一 compose.json 编译失败，整个应用启动失败。报错信息 SHALL 含具体引用路径与声明来源。

#### Scenario: operationId 断链
- **WHEN** function 引用的 operationId 在 openapi 中不存在
- **THEN** 启动失败，报错含该 operationId 与声明来源

#### Scenario: operationId 冲突
- **WHEN** openapi 内两个 operation 有相同 operationId
- **THEN** 启动失败，不兜底

#### Scenario: 未知属性忽略
- **WHEN** function.properties 出现未知字段（未来使用意图扩展）
- **THEN** 忽略并打印 debug 日志，不报错

### Requirement: 使用意图扩展点

`function.properties` SHALL 作为未来使用意图（字段裁剪、默认值、可排序、分页模式）的扩展载体。系统 SHALL 通过可扩展的编译阶段（`Transformer`）添加这些字段，不修改已有编译器代码。本次 SHALL NOT 声明使用意图。

分页/排序识别本期 SHALL 归前端据固定 schema 字段推断（request `offset`/`size`/`sort`、response `content`/`total`），编译器 SHALL NOT 物化分页容器或排序 input。字段裁剪本期不做，v1 显示 operation 全部 response 字段。

#### Scenario: 编译阶段扩展
- **WHEN** 未来新增使用意图编译阶段
- **THEN** 该阶段往 function.properties 添加字段，不改动 openapi 内联阶段
