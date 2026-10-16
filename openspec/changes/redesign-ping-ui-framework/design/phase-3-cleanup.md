# Phase 3: 旧代码清理

## 任务范围概述

移除旧的 SDK 代码和前端渲染器代码。

## 逐任务实现细节

### 3.1 移除 ping-ui 旧 SDK 代码

移除：
- `ping-ui/src/main/java/cn/procsl/ping/boot/ui/component/` 下的旧类（已被新架构替代）
- 前端旧渲染器代码（registry.tsx、derive.ts、default-tree.ts 等）

验证：`./mvnw -Ph2 test` 通过

### 3.2 移除旧异常类

移除：
- `RefNotFoundException`
- `CircularReferenceException`

验证：代码中无残留引用

## 边界与降级契约

1. **测试失败**：如果测试失败，需要修复后再继续
2. **编译失败**：确保所有引用都已更新

## 验证方式

1. **构建验证**：`./mvnw -Ph2 test` 通过
2. **代码检查**：确保无旧代码残留
