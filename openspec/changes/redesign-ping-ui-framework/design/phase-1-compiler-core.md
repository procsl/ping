# Phase 1: 编译器核心

## 任务范围概述

重构 `cn.procsl.ping.boot.ui.component` 包为 DDD 架构，引入 `Component` 组合类型、`App` 应用根、`ComposeService` 领域服务、`ComposeCompiler` 编译器、`ComposeRepository` 仓储。

## 包结构

```
cn.procsl.ping.boot.ui
├── domain/
│   ├── Component.java         # 组合类型（type + id + properties + children）
│   └── App.java               # 应用根，包含编译后的 Component 树
├── compiler/
│   ├── ComposeCompiler.java   # 编译器（List<JsonNode> → List<Component>）
│   ├── OperationCompiler.java # operation 契约构建
│   ├── ComponentCompiler.java # 组件树物化
│   ├── CompilePhase.java      # 编译阶段接口
│   ├── CompileContext.java    # 编译上下文
│   └── Schemas.java           # 常量
├── service/
│   └── ComposeService.java    # 领域服务
├── repository/
│   └── ComposeRepository.java  # 返回 List<JsonNode>
└── exception/
    ├── ComposeException.java
    └── ComposeParseException.java
```

## 逐任务实现细节

### 1.1 domain 层

#### 1.1.1 创建 Component 组合类型

```java
package cn.procsl.ping.boot.ui.domain;

import tools.jackson.databind.JsonNode;
import java.util.List;

/**
 * 抽象 UI 组件（组合类型）。
 * 不同类型只是 type 字段不同，不引入继承层次。
 */
public class Component {
    private final String type;
    private final String id;
    private final JsonNode properties;
    private final List<Component> children;
    
    // constructor, getters
}
```

验证：单测覆盖 type/id/properties/children 的读写

#### 1.1.2 创建 App 应用根

```java
package cn.procsl.ping.boot.ui.domain;

import java.util.List;

/**
 * 应用根，包含编译后的 Component 树。
 */
public class App {
    private final List<Component> children;
    
    public App(List<Component> children) {
        this.children = List.copyOf(children);
    }
    
    public List<Component> getChildren() {
        return children;
    }
}
```

验证：单测覆盖 children 读写

### 1.2 exception 层

#### 1.2.1 精简异常到 2 个

保留：
- `ComposeException`（基类）
- `ComposeParseException`（文件级解析失败）

移除：
- `RefNotFoundException` → 合并到 `ComposeException`
- `CircularReferenceException` → 合并到 `ComposeException`

验证：单测覆盖各种错误场景的异常消息

### 1.3 repository 层

#### 1.3.1 重命名 infra → repository

将 `ComposeRepository` 移到 `repository` 包，保持返回 `List<JsonNode>`。

验证：单测覆盖 classpath 扫描、JSON 解析、多文件合并

### 1.4 compiler 层

#### 1.4.1 创建 CompilePhase 接口

```java
package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import java.util.List;

/**
 * 编译阶段接口。
 * 新增编译阶段只需实现此接口，注册到 ComposeCompiler。
 */
public interface CompilePhase {
    void execute(List<Component> components, CompileContext context);
}
```

验证：单测覆盖接口定义

#### 1.4.2 创建 CompileContext

```java
package cn.procsl.ping.boot.ui.compiler;

import tools.jackson.databind.JsonNode;
import java.util.Map;

/**
 * 编译上下文，共享编译过程中的数据。
 */
public class CompileContext {
    private final Map<String, JsonNode> operations;
    
    // constructor, getters, setters
}
```

验证：单测覆盖 operations 读写

#### 1.4.3 创建 OperationCompiler

整合 `OperationRefResolver` + `SchemaRefExpander` + `DefaultResolver`。

**解析流程：**

```
输入: List<Component>（所有组件）
输出: CompileContext.operations（Map<String, JsonNode>）

遍历所有 type="operation" 的组件:
  │
  ├─ Step 1: 解析 openapi:// 引用
  │  │  从 component.properties.api.$ref 提取 operationId
  │  │  在 OpenAPI 文档中按 operationId 查找 path + method
  │  │  解析 parameters → request.path / request.query / request.<name>
  │  │  解析 requestBody → request.body（仅 application/json）
  │  │  解析 responses → response（200/201 取 schema，204 为空对象）
  │  │
  │  └─ 输出: operation 契约（method/path/request/response）
  │
  ├─ Step 2: 展开 OpenAPI $ref
  │  │  递归展开 request/response schema 树中的所有 $ref
  │  │  检测循环引用，命中即抛 ComposeException
  │  │
  │  └─ 输出: 完全展开的 operation 契约
  │
  └─ Step 3: 合并默认值
     │  将 operation.properties.defaults 合并到 request 对应字段的 default 属性
     │
     └─ 输出: 合并默认值后的 operation 契约

最终结果: context.operations.put(operationId, operationContract)
```

验证：单测覆盖 operation 解析、$ref 展开、默认值合并

#### 1.4.4 创建 ComponentCompiler

整合 `RefResolver` + `PaginationMaterializer`。

**解析流程：**

```
输入: List<Component>（所有组件）+ CompileContext.operations
输出: 编译后的 List<Component>（$ref 已替换为 UUID）

遍历所有 type="function"/"view"/"form" 的组件:
  │
  ├─ Step 1: 字段路径 → UUID
  │  │  遍历组件的 children（input/output）
  │  │  从 child.properties.$ref 提取 openapi:// 路径
  │  │  在 context.operations 中定位 operation 的字段节点
  │  │  生成 UUID 写入字段 id 属性
  │  │  替换 $ref 值为 UUID
  │  │
  │  └─ 输出: $ref 已替换为 UUID 的组件
  │
  └─ Step 2: 分页物化
     │  检测 operation 是否分页（request 有 offset+size，response 有 content+total）
     │  命中后:
     │    1. 生成 type="input" 组件（page.offset / page.size），插入 view children
     │    2. 生成 type="pagination" 容器，children 为 output 子节点，插入 view children 末尾
     │
     └─ 输出: 包含 pagination 容器的组件树
```

验证：单测覆盖 UUID 解析、分页物化

#### 1.4.5 创建 ComposeCompiler

```java
package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import tools.jackson.databind.JsonNode;
import java.util.List;

/**
 * 主编排器，协调各编译阶段。
 */
public class ComposeCompiler {
    private final List<CompilePhase> phases;
    
    public ComposeCompiler(List<CompilePhase> phases) {
        this.phases = List.copyOf(phases);
    }
    
    public List<Component> compile(List<JsonNode> rawComponents) {
        // 1. 将 JsonNode 转换为 Component
        // 2. 执行各编译阶段
        // 3. 返回编译后的 List<Component>
    }
}
```

验证：单测覆盖完整编译流程

### 1.5 service 层

#### 1.5.1 创建 ComposeService

```java
package cn.procsl.ping.boot.ui.service;

import cn.procsl.ping.boot.ui.domain.App;
import cn.procsl.ping.boot.ui.compiler.ComposeCompiler;
import cn.procsl.ping.boot.ui.repository.ComposeRepository;

/**
 * 领域服务，串联 Repository → Compiler → App。
 */
public class ComposeService {
    private final ComposeRepository repository;
    private final ComposeCompiler compiler;
    
    public App compile() {
        List<JsonNode> raw = repository.loadAll();
        List<Component> compiled = compiler.compile(raw);
        return new App(compiled);
    }
}
```

验证：单测覆盖完整调用流程

## 组件初始化/组装流程

### 创建方式

组件不依赖 Spring，直接 new 创建，用完即释放：

```java
// 每次请求时创建，用完即释放
public App compile() {
    ComposeRepository repository = new ComposeRepository(objectMapper);
    ComposeCompiler compiler = new ComposeCompiler(List.of(
        new OperationCompiler(),
        new ComponentCompiler()
    ));
    ComposeService service = new ComposeService(repository, compiler);
    return service.compile();
}
```

### 依赖链

```
ObjectMapper
    ↓
new ComposeRepository(objectMapper)
    ↓
new ComposeCompiler(List.of(new OperationCompiler(), new ComponentCompiler()))
    ↓
new ComposeService(repository, compiler)
    ↓
service.compile() → App
```

### 生命周期

- 组件生命周期由调用方控制
- 每次编译创建新实例，用完即释放
- 无需一直持有，无内存泄漏风险

## 边界与降级契约

1. **空输入**：`ComposeRepository.loadAll()` 返回空列表时，`ComposeCompiler.compile()` 返回空列表，`ComposeService.compile()` 返回空 App
2. **编译失败**：任何编译错误都抛出 `ComposeException`，不降级
3. **引用断链**：`$ref` 指向不存在的 operation 或字段时，抛出 `ComposeException`，包含完整路径

## 验证方式

1. **单元测试**：每个类都有对应的单元测试
2. **集成测试**：`ComposeService.compile()` 的端到端测试
3. **异常测试**：验证各种错误场景
4. **边界测试**：验证空输入、空对象等边界情况
5. **初始化测试**：验证 Spring Bean 正确注册和组装
