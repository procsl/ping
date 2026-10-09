# Design: standalone-module-packaging

## Context

**现状**

```
root pom (ping)
├── ping-parent  (业务聚合器: product / system / captcha / ai)
│                 <!-- ping-ui / im / editor / batch 均已注释停用 -->
├── ping-common  ← ping-web / ping-jpa          基础层
└── ping-distribute  → 硬编码 product + system   聚合层
```

- `spring-boot-maven-plugin` 只存在于 `ping-distribute`；所有 `*Application` 都在 test scope。
- `ping-apt` 已在编译期生成 `XxxRepository` / `Xxx_`（`@RepositoryCreator` 驱动），是"用代码生成替代运行时动态"的既有范式。
- `ping-distribute/pom.xml` 在 `prepare-package` 用 `exec-maven-plugin` 真实启动应用，把 OpenAPI 导出到 `${project.build.outputDirectory}/ping-api-doc/`；`UiSchemaRepository:45` 硬读 `classpath:ping-api-doc/openapi.json`——业务层反向依赖聚合层产物。
- `build-profile-filter.groovy` 在 `initialize` 阶段按 `application-<profile>.properties` 是否存在过滤 `spring.profiles.active`，是"构建期生成配置"的既有范式。
- 根 pom 的 `maven-compiler-plugin` 已集中声明 `annotationProcessorPaths` 与 `compilerArgs`，所有模块继承。

**约束**

- 只用 properties，禁用 yaml（`build-profile-filter.groovy` 目前仍容忍 yml/yaml，需收紧）。
- `ping-apt` 不在 reactor，需先 `./mvnw -f ping-apt/pom.xml install -DskipTests`。
- GraalVM native 发布走 `ping-distribute -Pnative`。
- 本变更不改运行时 API、不改 schema、不涉及前端渲染（属 `abstract-component-rendering`）。

## Goals / Non-Goals

**Goals:**

- 定义并可验证地约束三层结构与业务模块正交性。
- 任意业务模块可独立打包为可执行 jar（散）；`ping-distribute` 可打包为大单体（聚）；两者共用同一套生成机制。
- mainClass 由编译期代码生成提供，业务模块 pom 无需声明 `<mainClass>`。
- `ping-apt` 具备统一的打包期生成器契约，至少覆盖 Launcher、OpenAPI、测试配置。
- 消除 `ping-api-doc/openapi.json` 单点，使任一模块独立运行时能取得自身 OpenAPI。
- 建立 GraalVM 源码可移植性规则，明确"不支持即不引入"的落地方式。

**Non-Goals:**

- 不实现前端渲染、组件 schema、`ping-ui` 的组件职责（见 `abstract-component-rendering`）。
- 不引入微前端、不改造运行时 API 路径、不改数据库 schema。
- 不在本变更中补齐各业务模块的业务代码（模块尚未搭建完成）。
- 不引入 Liquibase、不改变既有 profile 机制语义。

## Decisions

### D1. 三层结构而非平铺模块

**选择**：根 `pom.xml` 下为 `ping-parent`（业务聚合）、`ping-common` / `ping-web` / `ping-jpa` / `ping-ui`（基础层）、`ping-distribute`（聚合层）。

**理由**：`ping-ui` 要成为"需要前端的模块"的公共依赖，必须位于业务层之上、业务层之下，否则产生业务→业务依赖。依赖方向 `common ← web/jpa ← ui ← 业务 ← 聚合` 与 AGENTS.md 既有描述一致，只需把 `ui` 插入基础层。

**替代方案**：保持 `ping-ui` 在 `ping-parent` —— 业务模块依赖 `ping-ui` 即业务→业务，正交性约束立刻被破坏；且 `ping-parent` 已注释停用它，无法满足"公共依赖"语义。

### D2. mainClass 由编译期生成 + `MainClassFinder` 自动发现

**选择**：

1. 根 pom 的 `maven-compiler-plugin` 追加 `-Aping.module=${project.artifactId}` 与 `-Aping.standalone=${ping.standalone}`——**每个模块自动获得处理器选项，无需逐模块配置**。
2. 根 pom 默认 `ping.standalone=false`（基础层是库，无入口）；`ping-parent/pom.xml` 覆盖为 `true`（业务模块有入口）；`ping-distribute` 覆盖为 `false`（入口是手写的 `DistributeApplication`）。
3. `ping-apt` 在 `ping.standalone=true` 时生成 `cn.procsl.ping.launch.<Module>Launcher`，含 `public static void main`，`SpringApplication.run(<Module>Launcher.class, args)` 传 **class literal**。
4. `spring-boot-maven-plugin` 的 `repackage` 在未配置 `<mainClass>` 时会用 `MainClassFinder` 在**本模块 `target/classes`** 中查找唯一 `main`——因此业务模块 pom 只需声明 repackage 插件，无需 `mainClass`；`Start-Class` 由插件自动写入 MANIFEST。

**为何"聚合打包则不注入"自动成立**：`repackage` 只扫描**本模块自己的** `target/classes`。`ping-distribute` 的 classes 里只有 `DistributeApplication`，依赖 jar 中的 `<Module>Launcher` 永远不会被扫到——不需要任何特殊处理。

**替代方案**：

| 方案 | 弃用理由 |
|---|---|
| 每模块 pom 手写 `<mainClass>` | 违背"代码生成自动注入"，N 处重复且易漂移 |
| 共享 `PingLauncher` + `spring.main.sources` 传字符串 | 字符串形式的配置类引用在 native 下需额外 reflection hint |
| 固定类名 `cn.procsl.ping.launch.PingLauncher` 全模块同名 | 聚合 classpath 上多份同名类互相遮蔽，属反模式 |
| `-Pstandalone` profile 控制生成 | 与分层属性重复；且属性已能表达同一语义 |

**约束**：业务模块 `src/main` 不得自行声明 `main` 方法，否则 `MainClassFinder` 出现多候选。该约束以 enforcer 规则或评审约定执行。

### D3. 打包期生成器统一由 `ping-apt` 承担，落位固定约定

**选择**：`ping-apt` 作为唯一的编译期生成器宿主，产物统一落位在 `META-INF/ping/` 下：

```
META-INF/ping/
├── openapi/<module>.json        # 本模块 OpenAPI（打包期生成）
├── test-config/<module>.properties  # 测试默认配置
└── module.properties            # 模块标识（id、是否含静态资源等）
```

生成时机分两类：

- **编译期（APT）**：Launcher 类、`module.properties` 等能从注解与处理器选项推导的内容。
- **打包期（构建插件）**：需要启动应用才能取得的 OpenAPI、需要聚合 profile 的测试配置。

**理由**：AOT（`process-aot`）与 native 都在构建期发生，构建期产物天然是静态、可注册 hint 的；运行时只需读固定路径文件，零 glob、零 `Class.forName`。

**替代方案**：继续用运行时 `ClassPathResource("ping-api-doc/openapi.json")` —— 该文件仅聚合层产出，业务模块独立运行必然失败；且单点文件无法表达"每个模块各自一份"。

### D4. OpenAPI 采用"启动导出、按模块落位"

**选择**：沿用 `ping-distribute` 既有的"启动应用 → `GET /v3/api-docs` → 写文件 → `SpringApplication.exit`"模式，把该配置从 `ping-distribute` 抽为可复用的打包步骤，业务模块在 `prepare-package` 时以 `-Ph2` 启动自身 Launcher 并导出到自己的 `META-INF/ping/openapi/<module>.json`。聚合层额外导出 `_aggregate.json`。

**理由**：springdoc 无纯静态模式，启动导出是本仓库已验证可行的路径（`SystemAutoConfiguration.openApiJsonExporter` 已存在）。

**替代方案**：

- `springdoc-openapi-maven-plugin`（HTTP 拉取）—— 仍需外部起服务，与现状等价，不减少复杂度。
- 用 `ping-apt` 静态解析 `@RequestMapping` 注解生成 OpenAPI —— 需要完整解析参数注解、`@RequestBody` schema、继承与接口方法，工作量接近重写 springdoc，**列为后续演进而非本次范围**。

**代价**：打包时每个业务模块各启动一次 JVM。以模块数量（个位数）与打包频率衡量可接受；通过 profile 开关 `-Dping.openapi.skip` 支持跳过。

### D5. 测试配置生成为"jar 内元数据 + 共享测试基类加载"

**选择**：生成器把模块测试默认值（数据源类型、profile、端口占位、JPA 开关等）写入 `META-INF/ping/test-config/<module>.properties`；测试通过共享基类/`ApplicationContextInitializer` 加载，模块可用 `src/test/resources/application-*.properties` 覆盖。

**理由**：注解处理器只能写 `CLASS_OUTPUT` / `SOURCE_OUTPUT`，无法直接产出 test resources；写进 `src/main/resources` 又会被打进 jar 污染产物。jar 内元数据 + 测试时读取，既免除每模块手写重复的 `application.properties`，又不污染产物、不需额外 Maven 插件。

**替代方案**：`build-helper-maven-plugin` 把生成目录注册为 test resource —— 需引入新插件且生成动作脱离 APT，破坏 D3 的"统一宿主"。

### D6. GraalVM 走源码改造，而非标记文件或构建期排除

**选择**：按"**不支持的 jar 不引入，对应源码改用支持 GraalVM 的方案**"执行：

1. 静态盘点反射构造，全部收敛到 4 个文件（已核查）：

   | 文件 | 构造 | 处置 |
   |---|---|---|
   | `RestWebAutoConfiguration` | `Class.forName` ×6 + 反射构造 | 改为 `optional` 依赖 + 独立 `@Configuration` + `@ConditionalOnClass` 直接 import 类型，交由 AOT 处理 |
   | `CipherSecurityBuilder` | `getDeclaredFields` + `setAccessible` 字段拷贝 | 改显式构造/注入 |
   | `PredicateAnnotationExtractor` | `getDeclaredField` + `setAccessible` | 改为静态可推导的访问路径 |
   | `DistributeConfiguration.RequestProxyHint` | `hints.proxies()` | **已正确**，作为范式 |

2. 保留并推广既有范式：`ping-apt` 编译期生成 Repository（用静态代码替代运行时动态）。
3. 新增 enforcer/评审规则：非构建期代码禁止 `Class.forName`、`setAccessible`、`Proxy.newProxyInstance`。

**替代方案**：资源标记文件（`META-INF/ping/native-unsupported`）+ AOT 期报错 —— 上轮讨论提出，用户明确否决：约束应在**源码层面**就用支持的方案表达，而不是运行时打标记。

**理由**：盘点结果比预期乐观——非测试代码反射仅 4 处，全部可改造，"不可引入的 jar"大概率长期为空集。规则的价值在于**防止新增**，而非事后排除。

### D7. 生成器需要 `ping-apt` 先行安装（既有约束显式化）

**选择**：在规范中显式记录"`ping-apt` 不在 reactor，首次构建前需 `./mvnw -f ping-apt/pom.xml install -DskipTests`"，并将 `ping-apt` 的破坏性变更纳入发布流程。

**替代方案**：把 `ping-apt` 纳入 reactor —— 改动构建拓扑，且 `ping-apt` 被 `annotationProcessorPaths` 引用时需要已安装的 artifact，纳入 reactor 并不能消除首装问题（鸡生蛋）。

## Risks / Trade-offs

- **打包期启动应用开销**（D4）→ 每模块一次 JVM 启动；提供 `-Dping.openapi.skip`；后续演进为注解静态解析。
- **`MainClassFinder` 多候选冲突**（D2）→ 约束业务模块 `src/main` 不得自带 `main`；CI 以 enforcer 或脚本校验。
- **生成 Launcher 留在库 jar 中**（基础层误开 `ping.standalone`）→ 默认 `false`、仅 `ping-parent` 置 `true`，分层即开关。
- **`ping-apt` 版本漂移**（D7）→ `annotationProcessorPaths` 中版本号与 `project.version` 对齐；生成器变更需同步重装。
- **`build-profile-filter.groovy` 仍容忍 yml/yaml** → 收紧为仅 properties，否则"禁用 yaml"只是文档约定。
- **openapi 导出依赖 `-Ph2` 与本地端口**（D4，`--server.port=0` 已在用）→ 保持既有 `server.address=127.0.0.1` 与随机端口做法。
- **GraalVM 改造可能改变既有行为**（D6，尤其 `PredicateAnnotationExtractor`）→ 分模块单独提交、`./mvnw -Ph2 test` 全量回归。

## Migration Plan

1. **阶段一（无行为变化）**：分层调整——`ping-ui` 移入根 pom 并启用；收紧 groovy 的 yaml 容忍。
2. **阶段二（构建契约）**：根 pom 注入处理器选项；`ping-apt` 新增 Launcher 与 `module.properties` 生成；业务模块 pom 声明 repackage（不含 `mainClass`）。
3. **阶段三（消除单点）**：OpenAPI 与测试配置生成器接入；`UiSchemaRepository` 的 `ping-api-doc` 读取路径改为 `META-INF/ping/openapi/`。
4. **阶段四（GraalVM）**：按 D6 逐文件改造并回归；加静态检查规则。
5. **回滚**：各阶段独立可回滚；阶段二回滚只需移除 repackage 声明与处理器选项；阶段三回滚需同时恢复 `UiSchemaRepository` 读取路径。

每阶段结束以 `./mvnw -Ph2 test` 验证，阶段二另需 `./mvnw -Ph2 -DskipTests package` 验证两种打包形态。

## Open Questions

- ~~**聚合层多模块并存时的 OpenAPI 合并策略**~~ —— 已由变更 `abstract-component-rendering` 的 D3 回答：各模块分片在打包期自带 UI 绑定，聚合时只做 `META-INF/ping/ui-index.json` 分片并集；`_aggregate.json` 仅供开发查阅，不参与运行时绑定。本变更中 `_aggregate.json` 保持为纯文档产物。
- **`ping-editor` / `ping-im` / `ping-batch` 是否重新纳入**：分层规则定义后，停用模块的归属需逐个确认。
- **`ping-ui` 移层后其 `@EntityScan` / `@EnableJpaRepositories` 的加载顺序**：`UIAutoConfiguration` 缺 `@AutoConfiguration`，需补 `@AutoConfiguration(after = ...)` 以免与 `ping-jpa` 初始化竞态。
