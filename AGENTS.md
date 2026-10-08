# AGENTS.md

## 命令

- 必须 JDK 25，构建前 `export JAVA_HOME=$HOME/.jdks/graalvm-jdk-25`（默认 java 是 11）
- 用 `./mvnw`（无全局 mvn）；**必须带数据库 profile**：`-Ph2` / `-Pmysql8` / `-Ppostgresql`
  （提供 `database.type`，否则 `application-@database.type@.properties` 和 jar 名留着未解析占位符）
- 全量测试：`./mvnw -Ph2 test`
- 单测试：`./mvnw -pl ping-system test -Dtest=MenuTest`（无匹配会失败，加 `-Dsurefire.failIfNoSpecifiedTests=false`）
- 打包：`./mvnw -Ph2 -DskipTests package` —— `ping-distribute` 的 `package` 会**真实起应用**导 OpenAPI + process-aot，约 1 分钟
- 首次构建先 `./mvnw -f ping-apt/pom.xml install -DskipTests`（`ping-apt` 不在 reactor，但编译器必需）
- 无 lint/format/CI；验证就靠 `./mvnw -Ph2 test`

## 模块

- reactor：`ping-parent`（→ `ping-product` `ping-system` `ping-captcha` `ping-ai`）、`ping-common`、`ping-web`、`ping-jpa`、`ping-distribute`
- 已停用（pom 中注释，不参与构建）：`ping-connect` `ping-im` `ping-editor` `ping-ui` `ping-batch`
- 独立工程（只能 `-f` 构建）：`ping-apt`、`ping-tool`
- 唯一入口：`cn.procsl.ping.app.DistributeApplication`，端口 10000
- 自动配置类登记到 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（`spring.factories` 是遗留，不读）
- 依赖方向：`common` ← `web`/`jpa` ← `captcha`/`product`/`ai` ← `system` ← `distribute`（distribute 不含 `ping-ai`）
- 包名 `cn.procsl.ping.boot.<module>`（例外：`ping-ai` 是 `cn.procsl.ping.ai`）
- 路径每个 mapping 写全 `/v1/...`；分页 `offset`/`limit` one-indexed；JSON SNAKE_CASE

## 编译期生成

- `ping-apt` 为 `@Entity` 生成 `<pkg>.repository.XxxRepository` 和 `Xxx_`（`target/generated-sources/annotations`）—— **别手写同名 Repository**
- 资源过滤定界符是 `@...@`（不是 `${}`）
- `build-profile-filter.groovy`：`-P<id>` 只有在存在 `application-<id>.properties|yml` 时才进 `spring.profiles.active`
  —— 日志里大量 `【Profile 过滤警告】` 是正常的
- `lombok.config`：链式 setter 关、日志字段固定 `log`、`@EqualsAndHashCode` 不用 getter

## 测试

- 数据源是内存 H2，无需外部服务
- **大量测试源整体被注释**（`ping-jpa` 的 6 个 `*Test.java` 全注释，`Tests run: 0` 不是失败）
- 有真实测试的只有 `ping-system`、`ping-product`

## 注意

- `Dockerfile`：`openjdk:17` + `ping-distribute-1.0.0.jar`，均已过时
- `.gitmodules`：路径不存在；vendored 源码实际在 `third/`
- Liquibase 未启用（全仓库无依赖），schema 由 `ddl-auto=update` 管
- `.opencode/` 被 gitignore（skills/commands 仅本地）

## OpenSpec

- `openspec/`（schema `spec-driven`）+ `/opsx:*` 命令 + `.opencode/skills/openspec-*`，CLI `openspec` 已装
- 当前分支 `dev-new`
