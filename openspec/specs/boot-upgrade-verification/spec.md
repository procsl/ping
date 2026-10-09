# Capability: boot-upgrade-verification

## Purpose

升级验证门槛——编译（无 4.0 已弃用 API）、全量测试、打包流水线（OpenAPI 导出 + process-aot + repackage）、配置属性与 JPA bootstrap 行为的可验证判据。

## Requirements

### Requirement: 升级后编译必须无错误且不调用已移除的弃用 API

升级到 4.1.1 后，全仓 MUST 编译通过；Spring Boot 4.0 中标记弃用、在 4.1 被移除的类/方法/属性 MUST NOT 被调用。发现的弃用调用 MUST 通过适配代码清理，MUST NOT 以 `@SuppressWarnings`、注释掉测试或回退版本的方式规避。

#### Scenario: 编译通过

- **WHEN** 执行 `export JAVA_HOME=$HOME/.jdks/graalvm-jdk-25 && ./mvnw -f ping-apt/pom.xml install -DskipTests` 后执行 `./mvnw -Ph2 clean compile`
- **THEN** 编译成功，无 ERROR

#### Scenario: 弃用调用清零

- **WHEN** 检查编译输出中的 `-Xlint:deprecation` 警告
- **THEN** 不存在指向 Spring 4.1 已移除 API 的警告；如存在其他弃用警告，均已记录且不构成 4.1 移除项

### Requirement: 全量测试必须通过

升级完成后，H2 内存数据库下的全量测试 MUST 全部通过，测试数量与升级前持平（被整体注释的测试源保持原状，`Tests run: 0` 的模块不算失败）。

#### Scenario: 全量测试全绿

- **WHEN** 执行 `./mvnw -Ph2 test`
- **THEN** BUILD SUCCESS，无失败与错误测试

#### Scenario: 测试数量不倒退

- **WHEN** 对比升级前后 `ping-system`、`ping-product` 的测试统计
- **THEN** 执行的测试数不低于升级前

### Requirement: 打包流水线在 4.1.1 下行为必须不变

`ping-distribute` 的打包流水线 MUST 在 4.1.1 下完整执行：`prepare-package` 阶段真实启动应用导出 OpenAPI、执行 `process-aot`，随后 repackage 产出可执行 jar；OpenAPI 导出 MUST NOT 失败（`failOnError=true`）。

#### Scenario: 打包端到端成功

- **WHEN** 执行 `./mvnw -Ph2 -DskipTests package`
- **THEN** BUILD SUCCESS，OpenAPI 导出产物 `target/classes/ping-api-doc/openapi.json` 与可执行 jar（MANIFEST `Spring-Boot-Version: 4.1.1`、`Start-Class` 正确）均生成

> 注（实现期修订）：`META-INF/ping/` 构建期产物与 `<Module>Launcher` 生成属变更 `standalone-module-packaging`，当前代码库尚无该生成器，不作为本变更门槛；`target/spring-doc/openapi.json` 是未绑定的 `springdoc-openapi-maven-plugin` 输出路径，实际导出走 `exec-maven-plugin`。

#### Scenario: AOT 与测试跳过语义不受影响

- **WHEN** 以 `-DskipTests` 执行打包
- **THEN** 流水线不因 4.1 的测试 AOT 语义变化而报错或执行测试 AOT；如需跳过测试相关处理，改用 `maven.test.skip`

### Requirement: 配置属性必须通过 4.1 属性迁移核对

启动与打包期 MUST NOT 出现因 Spring Boot 4.1 属性更名/移除导致的失效配置；`spring-boot-properties-migrator` 报告的 Spring 自有属性迁移项 MUST 全部处置（迁移或确认不适用），项目自定义属性不受影响。

#### Scenario: 属性迁移报告干净

- **WHEN** 应用在打包期以 `h2,openapi,openapi-export` profile 启动
- **THEN** 日志中无 properties-migrator 产生的待迁移属性告警，或每条告警均已对照 4.1 配置变更清单处置

#### Scenario: properties 约定不破坏

- **WHEN** 检查升级 diff
- **THEN** 未引入任何 yaml 配置，`@...@` 资源过滤定界符与 `application-<profile>.properties` 结构保持不变

### Requirement: JPA bootstrap-mode=lazy 行为必须验证保持可用

主配置与测试配置中的 `spring.data.jpa.repositories.bootstrap-mode=lazy` MUST 在 4.1 语义变化（自动配置的 `LocalContainerEntityManagerFactoryBean` 不再设置 bootstrap executor）下保持启动与查询行为正常；如验证失败，MUST 通过显式配置修正而非静默删除该属性。

#### Scenario: 应用正常启动

- **WHEN** 打包期应用以主 `application.properties`（含 `bootstrap-mode=lazy`）启动
- **THEN** Context 启动成功，Repository 初始化无异常

#### Scenario: Repository 查询回归

- **WHEN** 执行 `./mvnw -Ph2 test` 中涉及 Repository/投影查询的测试（如 `ping-jpa`、`ping-system`）
- **THEN** 查询行为与升级前一致，全部通过
