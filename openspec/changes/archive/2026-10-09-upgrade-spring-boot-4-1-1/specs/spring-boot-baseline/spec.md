# Capability: spring-boot-baseline

平台版本基线——Spring Boot 4.1.1 及其生态（springdoc、spring-boot-admin、hibernate-processor）在全部构建入口中的统一引用与对齐规则。

## ADDED Requirements

### Requirement: Spring Boot 版本引用必须统一为 4.1.1

全部 Spring Boot 版本引用点 MUST 为 4.1.1：根 `pom.xml` 的 `<parent>` 版本、根 `pom.xml` 的 `springboot.version` 属性、`ping-apt/pom.xml` 的 `spring-boot-dependencies` import 版本。全仓 MUST NOT 残留 `4.0.6` 的 Spring Boot 版本引用。

#### Scenario: 无旧版本残留

- **WHEN** 在所有 `pom.xml` 中搜索 Spring Boot 版本字符串 `4.0.6`
- **THEN** 匹配数为 0，且上述三个引用点均为 `4.1.1`

#### Scenario: 派生版本自动跟随

- **WHEN** 检查 `spring-boot-maven-plugin`、`spring-boot-configuration-processor` 等通过 `${springboot.version}` 取版本的声明
- **THEN** 解析后的实际版本为 4.1.1，无需逐个模块单独修改

### Requirement: 生态依赖必须对齐 Spring Boot 4.1 版本线

与 Boot 同线演进的生态依赖 MUST 升级到 4.1 对应版本线：springdoc-openapi MUST 为 3.1.x（`springdoc.version` 在根 pom 与 `ping-distribute/pom.xml` 的两处定义值必须一致），spring-boot-admin client/server MUST 为 4.1.x（`ping-parent` 与 `ping-distribute` 的全部四处 pin 必须一致）。

#### Scenario: springdoc 对齐

- **WHEN** 检查两处 `springdoc.version` 属性
- **THEN** 均为 3.1.x 且彼此相等，且 springdoc 构建所基于的 Spring Boot 线为 4.1

#### Scenario: spring-boot-admin 对齐

- **WHEN** 检查 `ping-parent` 与 `ping-distribute` 中 `spring-boot-admin-starter-client`/`server` 的版本
- **THEN** 四处均为 4.1.x 且彼此相等

### Requirement: Hibernate 注解处理器必须与 ORM 运行时同线

`annotationProcessorPaths` 中的 `hibernate-processor` 版本 MUST 与 Spring Boot 4.1.1 BOM 托管的 `hibernate.version` 保持同 minor 版本线（7.4.x），避免编译期生成的元模型与运行时 Hibernate ORM API 错配。

#### Scenario: processor 与 BOM 同线

- **WHEN** 比较根 pom 中 `hibernate-processor` 的 pin 版本与 `spring-boot-dependencies:4.1.1` 托管的 `hibernate.version`
- **THEN** 两者的 major.minor 一致（7.4）

#### Scenario: 元模型生成正常

- **WHEN** 执行编译并检查 `target/generated-sources/annotations`
- **THEN** 实体元模型类（`Xxx_`）与 `ping-apt` 生成的 `XxxRepository` 均正常生成，无注解处理器报错

### Requirement: 基线范围仅限 reactor 模块

版本基线 MUST 只覆盖参与 reactor 构建的模块与独立构建的 `ping-apt`；`ping-tool`（独立工程，Boot 3.2.6）与已停用模块 MUST NOT 因本次升级被改动。

#### Scenario: 独立工程不受影响

- **WHEN** 检查 `ping-tool/pom.xml` 的 `spring-boot-dependencies` 版本
- **THEN** 仍为升级前的版本，本次变更未修改该文件
