# Capability: module-layering

模块三层结构、依赖方向与正交性约束。

## ADDED Requirements

### Requirement: 模块必须归属于三层之一

系统 SHALL 将每个 Maven 模块归属于三层之一：**基础层**（`ping-common`、`ping-web`、`ping-jpa`、`ping-ui`）、**业务层**（由 `ping-parent` 聚合的模块）、**聚合层**（`ping-distribute`）。每层的模块列表 MUST 在对应 `pom.xml` 中显式声明，不得存在未声明归属的模块。

#### Scenario: 基础层模块位于根 pom

- **WHEN** 检查根 `pom.xml` 的 `<modules>`
- **THEN** `ping-parent`、`ping-common`、`ping-web`、`ping-jpa`、`ping-ui`、`ping-distribute` 均在其中

#### Scenario: 业务层模块由 ping-parent 聚合

- **WHEN** 检查 `ping-parent/pom.xml` 的 `<modules>`
- **THEN** 业务模块（如 `ping-product`、`ping-system`、`ping-captcha`、`ping-ai`）在其中，且 `ping-ui` 不在其中

#### Scenario: ping-ui 归属基础层

- **WHEN** 构建任一业务模块
- **THEN** `ping-ui` 作为基础层依赖被解析，且该依赖不产生业务层模块间的相互引用

### Requirement: 依赖方向必须单向且符合层级

模块依赖 SHALL 遵循 `common ← web/jpa ← ui ← 业务 ← 聚合` 的单向关系。低层模块 MUST NOT 依赖高层模块；同层模块之间的依赖 MUST NOT 存在（业务层内部亦然）。

#### Scenario: 业务模块依赖基础层

- **WHEN** 检查任一业务模块的 `pom.xml` 中 `cn.procsl` 依赖
- **THEN** 其依赖仅来自 `ping-common`、`ping-web`、`ping-jpa`、`ping-ui`、`ping-captcha` 之外的基础层集合

#### Scenario: 基础层不依赖业务层

- **WHEN** 检查 `ping-common`、`ping-web`、`ping-jpa`、`ping-ui` 的 `pom.xml`
- **THEN** 不存在指向任何业务模块或 `ping-distribute` 的 `cn.procsl` 依赖

### Requirement: 业务模块必须正交

业务模块之间 MUST NOT 存在 Maven 依赖。每个业务模块 SHALL 可在仅依赖基础层的情况下独立编译与运行。

#### Scenario: system 不再依赖 captcha

- **WHEN** 检查 `ping-system/pom.xml` 的 `cn.procsl` 依赖
- **THEN** 不包含 `ping-captcha`；若功能确需验证码能力，则由 captcha 单独提供并由基础层或聚合层组合

#### Scenario: 任一业务模块独立构建

- **WHEN** 以 `./mvnw -f ping-product/pom.xml -Ph2 install` 单独构建 `ping-product`
- **THEN** 构建成功，且未解析到任何其他业务模块

### Requirement: 业务模块不得依赖聚合层构建产物

业务模块 MUST NOT 在运行时读取仅由 `ping-distribute` 打包阶段产出的文件。所有运行时必需的类路径资源 MUST 由模块自身或其依赖在构建期内产出。

#### Scenario: 消除 ping-api-doc 单点

- **WHEN** `ping-system` 以独立 Launcher 启动
- **THEN** 其 OpenAPI 文档可从本模块自身的 `META-INF/ping/openapi/<module>.json` 取得，且不存在对 `classpath:ping-api-doc/openapi.json` 的读取

#### Scenario: 不再存在聚合层独占资源路径

- **WHEN** 全仓搜索 `ping-api-doc`
- **THEN** 无业务模块源码引用该路径
