# Phase 3: 旧代码清理

## 任务范围概述

移除旧编译器（$ref/UUID/pagination/排序/默认值那套）、旧异常、旧前端 SDK 机制、存量旧格式文件。对应 tasks 4.x。

## 逐任务实现细节

### 4.1 移除旧编译器

移除 `OperationRefResolver`、`SchemaRefExpander`、`RefResolver`、`PaginationMaterializer`、`DefaultResolver`，及旧 `Component`/`Compose` 聚合类型（已被统一节点 + OperationTransformer 替代）。

验证：代码无残留引用

### 4.2 移除旧异常

移除 `RefNotFoundException`、`CircularReferenceException`、`ComposeCompileException`（已合并到 `ComposeException`/`ComposeParseException`）。

验证：代码无残留引用

### 4.3 移除旧前端 SDK 机制

移除 SDK 机制（registry、message-bus、sdk-error、handlers），**保留 `createPingUi` 占位 entry**，薄壳 `META-INF/ping/entry/index.html` 不动。

验证：`npm run build` 通过

> 注：旧前端渲染器 `registry.tsx`/`derive.ts`/`default-tree.ts` 早在前次重构已删除，本次无对象可删。

### 4.4 移除存量旧格式文件

删除 `ping-system/src/main/resources/ui/user-center/compose.json`（旧 dataset 模型，路径不在扫描范围）。

验证：代码无残留引用

## 边界与降级契约

1. **测试失败**：修复后再继续
2. **构建失败**：确保所有引用已更新，`npm run build` 与 `./mvnw -Ph2 test` 均通过

## 验证方式

1. 构建验证：`./mvnw -Ph2 test` 通过、`npm run build` 通过
2. 代码检查：无旧编译器/旧异常/旧前端 SDK 残留
