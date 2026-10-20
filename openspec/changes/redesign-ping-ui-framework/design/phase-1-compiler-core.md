# Phase 1: 编译器核心

## 任务范围概述

把 `cn.procsl.ping.boot.ui.component` 旧包重构为 DDD 架构：统一节点组合类型、`ComposeCompiler` 主编排、`Transformer` 阶段接口、`OpenApiInliner`（openapi 内联阶段）、`ComposeRepository`/`OpenApiRepository` 仓储。对应 tasks 1.x / 2.x。

## 包结构

```
cn.procsl.ping.boot.ui
├── domain/
│   └── Component.java         # 统一节点（type + id + properties + children）
├── compiler/
│   ├── ComposeCompiler.java     # 主编排，协调各 Transformer，返回 type:app 根
│   ├── Transformer.java      # 编译阶段接口（开闭原则）
│   ├── CompileContext.java    # 编译上下文（openapi operations 索引）
│   └── OpenApiInliner.java    # openapi 内联阶段（本次唯一核心阶段）
├── repository/
│   ├── ComposeRepository.java  # 扫 META-INF/ping/ui/compose.json，返回 List<JsonNode>
│   └── OpenApiRepository.java  # 扫 META-INF/ping/openapi-<module>.json
└── exception/
    ├── ComposeException.java   # 基类，编译错误
    └── ComposeParseException.java  # 文件解析错误
```

## 逐任务实现细节

### 1.1 domain 层

#### 1.1.1 统一节点组合类型

```java
package cn.procsl.ping.boot.ui.domain;

import tools.jackson.databind.JsonNode;
import java.util.List;

/**
 * 统一节点（组合类型）。app/page/function 只是 type 值不同，不引入继承层次。
 * 不可变（final 字段）：改字段 = 重建节点，纯函数式变换返回新树。
 */
public class Component {
    private final String type;          // "app" | "page" | "function"
    private final String id;
    private final JsonNode properties;  // function: openapi 描述内联
    private final List<Component> children;

    // constructor, getters
}
```

验证：单测覆盖 type/id/properties/children 读写

#### 1.1.2 按 type 分派

校验与编译按 `type` 分派（app/page/function 各自约束），不为每类建子类。

验证：单测覆盖按 type 分派

### 1.2 exception 层

保留 `ComposeException`（基类，编译错误）+ `ComposeParseException`（文件解析错误）。移除旧的 `RefNotFoundException`、`CircularReferenceException`、`ComposeCompileException`。

验证：单测覆盖各错误场景异常消息

### 1.3 repository 层

- `ComposeRepository` 扫 `META-INF/ping/ui/compose.json`，返回 `List<JsonNode>`（节点数组）
- `OpenApiRepository` 扫 `META-INF/ping/openapi-<module>.json`，多文件合并

验证：单测覆盖 classpath 扫描、JSON 解析、多文件合并

### 1.4 compiler 层

#### 1.4.1 Transformer 接口

```java
package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import java.util.List;

/**
 * 变换接口（扩展点）。新增编译阶段（如未来使用意图）只需实现此接口，注册到 ComposeCompiler。
 * 输入整棵树 → 输出变换后的树，纯函数式 List → List，支持过滤/增删/改字段。
 */
public interface Transformer {
    List<Component> process(List<Component> nodes, CompileContext context);
}
```

#### 1.4.2 CompileContext

```java
package cn.procsl.ping.boot.ui.compiler;

import tools.jackson.databind.JsonNode;
import java.util.Map;

/**
 * 编译上下文：openapi operations 索引（operationId -> operation 描述）。
 */
public class CompileContext {
    private final Map<String, JsonNode> operations;
    // constructor, getters
}
```

#### 1.4.3 OpenApiInliner（openapi 内联阶段）

本次唯一核心阶段。流程：

```
输入: List<Component>（source nodes）+ CompileContext.operations（openapi 索引）

遍历所有 type="function" 节点:
  │
  ├─ Step 1: 按 properties.operationId 定位 openapi operation
  │     找不到 -> 抛 ComposeException（断链）
  │
  ├─ Step 2: 内联 method/path/parameters/requestBody/responses 到 properties
  │     responses 只保留 2xx（200/201/204），非 2xx 丢弃
  │
  └─ Step 3: 递归展开所有 $ref（#/components/schemas/...），内联 schema
        接受冗余，无 $ref 残留
        循环引用 -> 抛 ComposeException

输出: function.properties = 自包含 openapi operation 描述
```

#### 1.4.4 ComposeCompiler（主编排）

```java
package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import java.util.List;

/**
 * 主编排器：链式执行各 Transformer（纯变换 List→List），返回 type:app 根节点。
 */
public class ComposeCompiler {
    private final List<Transformer> transformers;

    public ComposeCompiler(List<Transformer> transformers) {
        this.transformers = List.copyOf(transformers);
    }

    public Component compile(List<Component> sourceNodes, CompileContext context) {
        List<Component> nodes = sourceNodes;
        for (Transformer t : transformers) {
            nodes = t.process(nodes, context);   // 链式：每步可过滤/增删/改字段
        }
        return assemble(nodes);                  // 固定方法：合并为 type:"app" 根
    }

    // assemble：稳定合并逻辑，app 根结构是不变量，不做成扩展点
}
```

## 示例

### compose.json 源（业务模块提供，节点数组）

```json
[
  {
    "type": "page", "id": "user",
    "children": [
      { "type": "function", "id": "list",
        "properties": { "operationId": "findUsers" } },
      { "type": "function", "id": "edit",
        "properties": { "operationId": "accountStatusSetting" } }
    ]
  }
]
```

### openapi 输入片段（findUsers）

```json
{
  "paths": {
    "/v1/system/users": {
      "get": {
        "operationId": "findUsers",
        "parameters": [
          { "name": "name", "in": "query", "schema": { "type": "string" } },
          { "name": "state", "in": "query", "schema": { "type": "string", "enum": ["disable","enable"] } },
          { "name": "offset", "in": "query", "schema": { "type": "integer" } },
          { "name": "size", "in": "query", "schema": { "type": "integer" } }
        ],
        "responses": {
          "200": { "content": { "application/json": { "schema": { "$ref": "#/components/schemas/FormatPageUserRecord" } } } },
          "400": { "content": { "application/json": { "schema": { "$ref": "#/components/schemas/ErrorVO" } } } }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "FormatPageUserRecord": {
        "type": "object",
        "properties": {
          "content": { "type": "array", "items": { "$ref": "#/components/schemas/UserRecord" } },
          "total": { "type": "integer" }
        }
      },
      "UserRecord": {
        "type": "object",
        "properties": { "id": { "type": "integer" }, "name": { "type": "string" } }
      },
      "ErrorVO": { "type": "object", "properties": { "message": { "type": "string" } } }
    }
  }
}
```

### 编译产物（GET /v1/ui/components 返回，function 自包含）

```json
{
  "type": "app", "id": "ping.app",
  "children": [
    {
      "type": "page", "id": "user",
      "children": [
        {
          "type": "function", "id": "list",
          "properties": {
            "operationId": "findUsers",
            "method": "GET",
            "path": "/v1/system/users",
            "parameters": [
              { "name": "name", "in": "query", "schema": { "type": "string" } },
              { "name": "state", "in": "query", "schema": { "type": "string", "enum": ["disable","enable"] } },
              { "name": "offset", "in": "query", "schema": { "type": "integer" } },
              { "name": "size", "in": "query", "schema": { "type": "integer" } }
            ],
            "responses": {
              "200": {
                "content": {
                  "application/json": {
                    "schema": {
                      "type": "object",
                      "properties": {
                        "content": {
                          "type": "array",
                          "items": {
                            "type": "object",
                            "properties": { "id": { "type": "integer" }, "name": { "type": "string" } }
                          }
                        },
                        "total": { "type": "integer" }
                      }
                    }
                  }
                }
              }
            }
          }
        },
        {
          "type": "function", "id": "edit",
          "properties": {
            "operationId": "accountStatusSetting",
            "method": "PATCH",
            "path": "/v1/system/users/{id}/account",
            "parameters": [ { "name": "id", "in": "path", "schema": { "type": "integer" } } ],
            "requestBody": { "content": { "application/json": { "schema": { "type": "object", "properties": { "state": { "type": "string" } } } } } },
            "responses": { "204": {} }
          }
        }
      ]
    }
  ]
}
```

**要点**：schema 全内联（`FormatPageUserRecord`/`UserRecord` 已展开，无 `$ref`）；`responses` 只保留 2xx（400 不在产物里）；query 参数已展开为 name/state/offset/size 各一个（`@ParameterObject` 保证 openapi 源准确）。

## 编译校验

| 情况 | 处理 |
|---|---|
| `page.id` 重复 | `ComposeException`，启动失败 |
| `function.id` 在 page 内重复 | `ComposeException`，启动失败 |
| `operationId` 在 openapi 中找不到 | `ComposeException`（断链），启动失败 |
| openapi 内 operationId 冲突 | `ComposeException`，启动失败 |
| 未知属性 / 未知类型 | 忽略 + debug 日志 |

## 组件初始化/组装流程

组件不依赖 Spring，直接 new 创建，无状态，用完即释放：

```java
ObjectMapper om = new ObjectMapper();
ComposeRepository composeRepo = new ComposeRepository(om);
OpenApiRepository openApiRepo = new OpenApiRepository(om);
CompileContext ctx = new CompileContext(openApiRepo.loadOperations());
ComposeCompiler engine = new ComposeCompiler(List.of(new OpenApiInliner()));
Component app = engine.compile(composeRepo.loadAll(), ctx);
```

依赖链：

```
ObjectMapper
  ↓
ComposeRepository / OpenApiRepository
  ↓
CompileContext（openapi operations 索引）
  ↓
ComposeCompiler(List.of(new OpenApiInliner()))
  ↓
engine.compile(...) → type:"app" 根
```

生命周期：无状态，每次编译新建实例，用完即释放，无内存泄漏风险。

## 边界与降级契约

1. **空输入**：`ComposeRepository.loadAll()` 返回空列表 → `compile()` 返回空 `type:"app"`（children 空），端点返回 200 空树
2. **编译失败**：任何结构性错误抛 `ComposeException`，启动失败，不降级
3. **断链**：`operationId` 找不到、`$ref` 循环引用，抛 `ComposeException` 含完整路径

## 验证方式

1. 单元测试：Component、CompileContext、OpenApiInliner、ComposeCompiler 各自单测
2. 集成测试：`ComposeCompiler.compile()` 端到端（compose.json + openapi → app）
3. 异常测试：断链、id 重复、operationId 冲突
4. 边界测试：空输入、无 $ref 残留、responses 只含 2xx
