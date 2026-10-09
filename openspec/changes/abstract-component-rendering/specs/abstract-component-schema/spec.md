# Capability: abstract-component-schema

抽象组件树的 schema 定义、节点类型与声明落位，及旧结构的作废。

## ADDED Requirements

### Requirement: 必须存在唯一一套抽象组件 schema

系统 SHALL 只存在一套抽象组件 schema，以 `ping-ui` 的节点模型为准。schema SHALL 同时覆盖应用壳（布局、菜单）与页面内容（表格、动作等叶子组件），MUST NOT 为页面内容另立第二套结构。

#### Scenario: 单一 schema 来源

- **WHEN** 搜索仓库中的抽象组件 schema 定义
- **THEN** 仅 `ping-ui` 持有 schema 定义与类型，`ping-system` 中不存在独立的组件结构定义

#### Scenario: 应用壳与页面内容同树表达

- **WHEN** 解析一份完整声明
- **THEN** 从 `application` 根节点经 `layout`、`menu` 可达页面节点，页面节点下可含 `table`、`column`、`action` 等叶子节点，全程使用同一节点结构

### Requirement: 组件节点必须具备稳定标识与类型

每个组件节点 SHALL 至少包含 `type`（组件类型）与 `id`（树内唯一标识）。声明中所有 `id` MUST 在同一份 `ui-index` 内唯一；重复 `id` SHALL 使构建失败。

#### Scenario: id 唯一性校验

- **WHEN** 某模块的两处声明使用相同 `id`
- **THEN** 打包期生成 `ui-index` 时构建失败，并报告冲突的 `id` 与所在文件

#### Scenario: 缺少必填字段

- **WHEN** 某节点缺少 `type` 或 `id`
- **THEN** 打包期生成失败并指明节点位置

### Requirement: 菜单节点必须声明路由与排序

菜单类节点 SHALL 声明 `router`（路由目标）与 `order`（排序权重）。`router` SHALL 使用带模块前缀的相对路径以隔离命名空间，MUST NOT 指向外部绝对地址作为默认值。

#### Scenario: 路由带模块前缀

- **WHEN** 业务模块 `foo` 声明菜单节点
- **THEN** 其 `router` 以 `foo/` 为前缀

#### Scenario: 排序稳定

- **WHEN** 多个同级菜单节点的 `order` 相同
- **THEN** 返回顺序按声明文件名与节点出现顺序稳定排序

### Requirement: 旧结构必须作废

`ping-system` 的 `functions` 嵌套结构与以 OpenAPI 中文 `summary` 挂载 API 的方式 SHALL 作废。所有既有页面声明 MUST 迁移为新 schema，系统 MUST NOT 同时接受新旧两种结构。

#### Scenario: user-center 迁移

- **WHEN** 读取 `ping-system/src/main/resources/ui/user-center/index.json`
- **THEN** 文件使用 `type`/`id`/`containers` 结构，不含 `functions` 字段

#### Scenario: 中文 summary 挂载消失

- **WHEN** 搜索 `UiSchemaService.compileApiMap` 及其按 `summary` 建索引的逻辑
- **THEN** 该逻辑已不存在

#### Scenario: 样例与设计稿对齐

- **WHEN** 比较 `ping-ui/components/ui.json` 与 `ping-ui/doc/design.md`
- **THEN** 两者描述的字段一致，且样例可被 schema 校验通过

### Requirement: 页面声明必须随模块打包落位

各业务模块 SHALL 将其页面声明置于 `src/main/resources/ui/<page>/index.json`，并在打包期规范化汇总为 `META-INF/ping/ui-index.json`。源声明 MUST NOT 被运行时直接读取。

#### Scenario: 源与产物分离

- **WHEN** 解压业务模块产物 jar
- **THEN** 存在 `META-INF/ping/ui-index.json`，且运行时不读取 `ui/<page>/index.json` 路径

#### Scenario: 无声明的模块

- **WHEN** 某业务模块不含任何 `ui/**` 声明
- **THEN** 打包成功，且产物中不生成 `ui-index.json`
