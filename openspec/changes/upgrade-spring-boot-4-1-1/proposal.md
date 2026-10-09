# Proposal: upgrade-spring-boot-4-1-1

## Why

项目当前锁定 Spring Boot **4.0.6**（根 `pom.xml` parent + `springboot.version`、`ping-apt` 的 `spring-boot-dependencies` import），而 4.1.1 已发布。4.1 带来新的基线（Hibernate 7.4、Spring Framework 7.0.9、Spring Security 7.1.1、Spring Data Bom 2026.0.0）与缺陷修复；4.0 中被弃用的 API/配置在 4.1 已被移除，滞留越久、后续跨大版本升级的清理成本越高。当前活跃的另两个变更是架构/构建层改造，先行升级平台基线可避免它们建立在过期依赖上。

## What Changes

- **BREAKING** Spring Boot 4.0.6 → **4.1.1**：根 pom `<parent>` 版本、`springboot.version` 属性、`ping-apt/pom.xml` 的 `spring-boot-dependencies` import 三处同步升级（当前 `4.0.6` 硬编码仅此三处）。
- **生态版本对齐**（Boot 4.1 对应线）：
  - springdoc 3.0.3 → **3.1.1**（3.1.x 基于 `spring-boot-starter-parent:4.1.0` 构建；根 pom 与 `ping-distribute/pom.xml` 各有一份 `springdoc.version`）；
  - spring-boot-admin client/server 4.0.4 → **4.1.4**（`ping-parent`、`ping-distribute` 各自 pin）；
  - `hibernate-processor` 7.2.12.Final → **7.4.5.Final**（根 pom `annotationProcessorPaths` 硬编码；Boot 4.1.1 托管 Hibernate `7.4.5.Final`，4.0.6 托管 `7.2.12.Final`，注解处理器须与 ORM 运行时同线，否则生成的 `Xxx_` 元模型与运行时 API 错配）。
- **BREAKING** 移除 4.0 已弃用 API：4.1 删除了 4.0 期间标记弃用的类/方法/属性，项目开着 `-Xlint:deprecation`，编译期暴露的弃用调用须在本次一并清理。
- **运行时行为核对**：`spring.data.jpa.repositories.bootstrap-mode=lazy` 在 4.1 下不再为自动配置的 `LocalContainerEntityManagerFactoryBean` 设置 bootstrap executor（`ping-distribute` 主配置与多个模块测试配置使用该值），需验证启动与测试行为不变。
- **构建语义核对**：Boot 4.1 起 Maven 插件的测试 AOT 只响应 `maven.test.skip` 而不再响应 `-DskipTests`；本项目打包流水线（`exec-maven-plugin` 导 OpenAPI + `process-aot` + repackage）需实测 `-Ph2 -DskipTests package` 行为不变。
- **不影响**：对外 REST 路径、数据库 schema、前端渲染、「配置只用 properties」「禁 `classpath*:`」等既有约定。`ping-tool`（独立工程，Boot 3.2.6）与已停用模块不在本次范围。

## Capabilities

### New Capabilities

- `spring-boot-baseline`: 平台版本基线——Spring Boot 4.1.1 及其生态（springdoc、spring-boot-admin、hibernate-processor）在全部构建入口中的统一引用与对齐规则。
- `boot-upgrade-verification`: 升级验证门槛——编译（无 4.0 已弃用 API）、全量测试、打包流水线（OpenAPI 导出 + process-aot + repackage）、配置属性与 JPA bootstrap 行为的可验证判据。

### Modified Capabilities

<!-- openspec/specs/ 当前为空，无既有能力需求变更 -->

## Impact

- **构建文件**：`pom.xml`（parent 版本、`springboot.version`、`springdoc.version`、`hibernate-processor` 路径版本）、`ping-apt/pom.xml`（BOM import）、`ping-parent/pom.xml`（SBA 4.0.4 ×2）、`ping-distribute/pom.xml`（`springdoc.version`、SBA 4.0.4 ×2、`spring-boot-maven-plugin` 版本走 `${springboot.version}` 自动跟随）。
- **依赖树**：Spring Framework 7.0.7→7.0.9、Security 7.1.0→7.1.1、Hibernate ORM 7.2→7.4、Hibernate Validator 9.x、Mockito 5.23、Spring Data 2026.0.0 等随 BOM 整体浮动。
- **代码**：`-Xlint:deprecation` 下暴露的 4.0 弃用 API 调用点（具体清单以升级后编译输出为准）。
- **配置**：`ping-distribute/src/main/resources/application.properties`（`bootstrap-mode=lazy`）、各模块 `src/test/resources/application*.properties`；`spring-boot-properties-migrator` 已在 `ping-distribute` 依赖中，启动期自动报告属性迁移。
- **验证入口**：`./mvnw -Ph2 test`、`./mvnw -Ph2 -DskipTests package`（含 OpenAPI 导出与 process-aot）。
- **不涉及**：`ping-tool`（独立工程 Boot 3.2.6）、`Dockerfile`（已过时的独立问题）、停用模块（`ping-connect` 等）。
