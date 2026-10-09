# Tasks: upgrade-spring-boot-4-1-1

## 1. 版本基线升级

- [ ] 1.1 根 `pom.xml`：`<parent>` 版本 `4.0.6 → 4.1.1`，`springboot.version` 属性 `4.0.6 → 4.1.1`
- [ ] 1.2 `ping-apt/pom.xml`：`spring-boot-dependencies` import `4.0.6 → 4.1.1`，然后 `export JAVA_HOME=$HOME/.jdks/graalvm-jdk-25 && ./mvnw -f ping-apt/pom.xml install -DskipTests`（编译前置）
- [ ] 1.3 springdoc `3.0.3 → 3.1.1`：根 `pom.xml` 与 `ping-distribute/pom.xml` 的两处 `springdoc.version` 同步
- [ ] 1.4 spring-boot-admin `4.0.4 → 4.1.4`：`ping-parent/pom.xml` 两处、`ping-distribute/pom.xml` 两处（client/server）同步
- [ ] 1.5 `hibernate-processor 7.2.12.Final → 7.4.5.Final`（根 pom `annotationProcessorPaths`），核对与 `spring-boot-dependencies:4.1.1` 托管的 `hibernate.version`（7.4.5.Final）同线
- [ ] 1.6 基线断言：全仓 `pom.xml` grep 无 `4.0.6` 残留；两处 `springdoc.version` 相等；四处 SBA 版本相等；确认 `ping-tool/pom.xml` 未被修改

## 2. 编译与弃用清理

- [ ] 2.1 `./mvnw -Ph2 clean compile`，收集编译错误与 `-Xlint:deprecation` 警告清单，筛出指向 Spring Boot/Framework 4.1 已移除 API 的项
- [ ] 2.2 逐项清理 4.1 已移除的 4.0 弃用 API 调用（改造代码，不用 `@SuppressWarnings`/注释规避）
- [ ] 2.3 确认编译期生成物正常：`target/generated-sources/annotations` 中实体元模型 `Xxx_`、`ping-apt` 的 `XxxRepository`、打包期 `<Module>Launcher` 均生成，无注解处理器报错

## 3. 配置与运行时行为核对

- [ ] 3.1 对照 Spring Boot 4.1.0 配置变更清单核对 `ping-distribute/src/main/resources/application*.properties` 与各模块 `src/test/resources/application*.properties`，处置失效/更名属性
- [ ] 3.2 打包期启动日志中 `spring-boot-properties-migrator` 的待迁移告警清零（逐条迁移或确认不适用）
- [ ] 3.3 验证 `spring.data.jpa.repositories.bootstrap-mode=lazy` 在 4.1 语义下启动正常（打包期真实启动即为验证点）；如失败，显式配置修正，不静默删除属性

## 4. 验证门槛

- [ ] 4.1 `./mvnw -Ph2 test` 全量通过（BUILD SUCCESS），`ping-system`、`ping-product` 测试数不低于升级前
- [ ] 4.2 `./mvnw -Ph2 -DskipTests package` 端到端成功：`target/spring-doc/openapi.json`、`META-INF/ping/` 构建期产物、可执行 jar 均生成，无测试 AOT 相关报错
- [ ] 4.3 对比升级前后 `openapi.json`，确认 API 路径与结构无意外变化
- [ ] 4.4（可选）`./mvnw -Ph2 -Pnative -DskipTests package` native 冒烟，确认 AOT 变化不影响 `-Pnative` 发布路径

## 5. 收尾

- [ ] 5.1 回顾 design.md 的 Open Questions：版本属性是否收敛为单一来源、native 冒烟是否升为门槛——记录结论供后续变更使用
- [ ] 5.2 确认 AGENTS.md 中与本次升级相关的描述（构建命令、依赖说明）仍准确，如需更新则同步
