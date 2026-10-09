# Design: upgrade-spring-boot-4-1-1

## Context

- 版本引用点（全部 `4.0.6` 硬编码）：根 `pom.xml` `<parent>`、根 `pom.xml` `springboot.version` 属性、`ping-apt/pom.xml` BOM import。`spring-boot-maven-plugin`、`spring-boot-configuration-processor` 等均走 `${springboot.version}`，随属性自动跟随。
- 生态 pin 点：`springdoc.version` 在根 pom 与 `ping-distribute/pom.xml` 各定义一份；spring-boot-admin `4.0.4` 在 `ping-parent` 与 `ping-distribute` 各 pin 两处（client/server）；`hibernate-processor 7.2.12.Final` 在根 pom `annotationProcessorPaths` 硬编码。
- Boot 4.1.1 托管 Hibernate `7.4.5.Final`（4.0.6 为 `7.2.12.Final`），Spring Framework 7.0.9，Spring Security 7.1.1。
- springdoc `3.1.1` 的父 pom 是 `spring-boot-starter-parent:4.1.0`；`3.0.3` 属 Boot 4.0 线。springdoc 直接依赖 Boot 4 模块化构件（`spring-boot-jackson`、`spring-boot-web-server` 等）。
- spring-boot-admin 版本线与 Boot minor 对齐（4.0.x ↔ Boot 4.0，4.1.4 ↔ Boot 4.1）。
- 打包流水线：`ping-distribute` 在 `prepare-package` 依次执行 `exec-maven-plugin`（真实启动应用导 OpenAPI）与 `process-aot`，随后 repackage；`spring-boot-properties-migrator` 已在依赖中。
- 约束（AGENTS.md）：JDK 25、`./mvnw -Ph2 test` 是唯一验证手段、配置只用 properties、native 只由 `ping-distribute -Pnative` 执行。

## Goals / Non-Goals

**Goals:**

- 全仓 Spring Boot 引用统一到 4.1.1，生态（springdoc / spring-boot-admin / hibernate-processor）对齐 Boot 4.1 线。
- 升级后编译零错误、无 4.0 已弃用 API 调用残留；`./mvnw -Ph2 test` 全绿。
- 打包流水线（OpenAPI 导出 + process-aot + repackage）在 4.1.1 下行为不变。
- 配置属性经 properties-migrator 与 4.1 配置变更清单核对，无失效项。

**Non-Goals:**

- 不重构版本管理结构（不引入 flatten/CI-friendly 版本、不合并重复的 `springdoc.version` 定义——仅同步其值）。
- 不升级 `ping-tool`（独立工程，Boot 3.2.6）、不处理已停用模块、不动过时的 `Dockerfile`。
- 不主动采纳 4.1 新特性（gRPC、`spring.jackson.factory` 等）；升级是平移，不是功能引入。
- 不移除 `spring-boot-properties-migrator`（留待后续变更决定）。

## Decisions

### D1：保持「parent 硬编码 + `springboot.version` 属性」双写，用任务清单保证一致

- **选择**：`<parent>` 版本保持字面量 4.1.1，`springboot.version` 属性同步 4.1.1，验证任务用全仓 grep 断言无 `4.0.6` 残留。
- **备选**：改用 `spring-boot-dependencies` BOM import + 自管插件 → 拒绝：会失去 starter-parent 提供的插件管理、资源过滤默认值与 enforcer 基线，改造面远超升级本身；且 Maven `<parent>` 版本不能可靠引用本 pom 属性，双写无法根除。
- **理由**：最小 diff、回滚即 revert；一致性由可执行检查保障而非结构重构。

### D2：生态版本跟随 Boot 4.1 线，而非「先试旧版」

- **选择**：springdoc `3.0.3 → 3.1.1`、spring-boot-admin `4.0.4 → 4.1.4`、`hibernate-processor 7.2.12.Final → 7.4.5.Final`。
- **备选 A**：生态保持旧版、只升 Boot → 拒绝：springdoc/SBA 均按 Boot minor 线构建，跨线运行在自动配置类移动/模块化拆分场景下是隐性炸弹；Hibernate processor 与 ORM 跨 minor（7.2 vs 7.4）会出现元模型生成代码与运行时 API 错配。
- **备选 B**：springdoc 跳到更新的 3.1.x 以后版本（若存在）→ 拒绝：3.1.1 是当前最新稳定版。
- **理由**：三者都是「与 Boot 同线演进」的依赖，对齐线版本是官方推荐姿势；OpenAPI 导出与打包真实启动即为端到端验证。

### D3：`hibernate-processor` 继续显式 pin，不依赖 BOM

- **选择**：在 `annotationProcessorPaths` 中 pin `7.4.5.Final`（与 Boot 4.1.1 托管的 `hibernate.version` 同值），并在 tasks 中加入「与 BOM 托管版本一致」的核对项。
- **备选**：不写版本（由依赖管理接管）→ 不可行：`annotationProcessorPaths` 是 maven-compiler-plugin 自己的配置，不经过 `dependencyManagement`，必须显式版本。
- **理由**：processor 与 ORM 错配的失败模式是运行期/元模型期才暴露，代价高；显式 pin + 核对任务是唯一可靠手段。

### D4：`bootstrap-mode=lazy` 保持不变

- **选择**：`application.properties` 及各测试配置继续使用 `lazy`，以升级后测试全绿 + 打包期真实启动为判据。
- **备选**：改为 `deferred` → 拒绝：4.1 下 `deferred` 要求存在可用的 `AsyncTaskExecutor` bean，缺失会直接抛异常，等于引入新装配需求；删掉该属性回到默认值则改变现有语义。
- **理由**：4.1 对 `lazy` 的变化是「不再设置 bootstrap executor」——恰好是 lazy 想要的效果，属行为收敛而非破坏；不改配置是最小风险路径。

### D5：验证顺序 = 编译 → 弃用清理 → 全量测试 → 打包流水线 →（可选）native 冒烟

- **选择**：先 bump 版本让 `-Xlint:deprecation` 暴露全部 4.0 弃用调用并清理，再跑 `./mvnw -Ph2 test`，最后跑 `-Ph2 -DskipTests package` 验证 OpenAPI 导出与 process-aot。
- **备选**：先跑测试再修编译 → 不可能：4.0 已删弃用 API 会使编译直接失败，测试根本跑不起来。
- **理由**：编译器是 4.1 移除项的第一检测器；打包流水线含真实启动与 AOT，是运行时兼容性的最强单点验证。

## Risks / Trade-offs

- [4.0 弃用 API 被 4.1 删除导致编译失败，清单未知] → 编译输出即清单；`-Xlint:deprecation` 已全局开启，逐个改造，禁止用 `@SuppressWarnings` 掩盖真实移除。
- [Hibernate 7.2 → 7.4 行为差异（DDL `ddl-auto=update` 生成语句、校验模式、方言）] → H2 内存测试全量覆盖 + 打包期真实启动（`ddl-auto` 实际执行）兜底；schema 是 `ddl-auto=update` 管理，无需手写迁移，但升级后应核对启动日志无 schema 校验告警。
- [springdoc/SBA 对齐线版本后仍可能与 Boot 4.1.1 细微不兼容] → OpenAPI 导出（`failOnError=true`）与 monitor profile 启动即失败暴露；出问题时的降级路径是锁回上一个同线 patch 版本。
- [`-DskipTests` 不再跳过测试 AOT 的语义变化] → 本项目未绑定 `test-aot` goal，理论无影响；以打包流水线实测确认（若打包突然变慢或报测试 AOT 错误，改用 `maven.test.skip`）。
- [注解处理器组合（lombok 1.18.46 / manifold / mapstruct / ping-apt / hibernate-processor）与 Boot 4.1 的编译交互] → 全量编译即验证；`ping-apt` 需先 `install`（既有约束）。
- [native `-Pnative` 受 AOT 变化影响但构建耗时长] → 列为打包验证后的可选冒烟；主验证路径不阻塞在 native 上。
- [双写版本（parent + 属性）未来再次漂移] → tasks 中保留 grep 断言项，可复用于后续升级。

## Migration Plan

1. bump 三处 `4.0.6 → 4.1.1` + 生态三处版本（springdoc ×2、SBA ×2、hibernate-processor ×1）。
2. 编译，清理暴露出的 4.0 弃用 API 调用。
3. `./mvnw -Ph2 test` 全量回归。
4. `./mvnw -Ph2 -DskipTests package` 验证 OpenAPI 导出、process-aot、repackage；核对 properties-migrator 输出。
5. （可选）`./mvnw -Ph2 -Pnative -DskipTests package` native 冒烟。
6. 单 commit 提交版本升级与配套代码清理。

**回滚**：全部变更集中于版本号与由其引发的适配代码，revert 单个 commit 即回到 4.0.6；无数据格式、无 API 契约变化，不需要数据回滚。

## Open Questions

- 生态版本是否收敛为根 pom 单一属性（消除 `springdoc.version` 双定义、SBA 四处硬编码）——建议留作后续重构变更，本变更只同步值。
- native 冒烟是否纳入本次验收门槛（当前定位为可选，主门槛是 test + package）。
