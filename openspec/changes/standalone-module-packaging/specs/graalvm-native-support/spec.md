# Capability: graalvm-native-support

源码级 GraalVM 可移植性规则、既有反射点处置与不可支持模块的排除方式。

## ADDED Requirements

### Requirement: 运行时代码必须避免不可静态分析的反射

非构建期运行的代码 MUST NOT 使用 `Class.forName(String)` 以字符串加载类型、`setAccessible` 修改访问权限、或 `Proxy.newProxyInstance` 构造运行时代理。允许的例外是已通过 `RuntimeHintsRegistrar` 显式注册对应 hint 的场景，且该 hint SHALL 与反射点位于同一模块。

#### Scenario: 静态检查通过

- **WHEN** 对 `src/main/java` 执行反射构造扫描
- **THEN** `Class.forName`、`setAccessible`、`Proxy.newProxyInstance` 的出现次数为 0，或每处均能定位到同模块内的 `RuntimeHintsRegistrar` 注册

#### Scenario: 既有代理 hint 范式保留

- **WHEN** 检查 `ping-distribute` 的 `RequestProxyHint`
- **THEN** `hints.proxies().registerJdkProxy(...)` 与 `HttpServletRequest`/`HttpServletResponse` 动态代理使用点保持对应关系

### Requirement: 可选依赖必须通过条件装配接入

需要按 classpath 有无决定是否启用的功能 SHALL 通过 `optional`（或 `provided`）依赖 + 独立 `@Configuration` 类 + `@ConditionalOnClass` 接入，并在该配置类内直接引用类型。MUST NOT 以 `Class.forName` + 反射构造绕过编译期类型检查。

#### Scenario: springdoc 条件装配

- **WHEN** classpath 含 springdoc
- **THEN** SNAKE_CASE 与 `ModelResolver` 相关 bean 由直接类型引用构建，且 AOT 处理时能为其生成反射 hint

#### Scenario: classpath 不含 springdoc

- **WHEN** classpath 不含 springdoc
- **THEN** 含 springdoc 类型引用的配置类不被加载，应用正常启动且不抛 `NoClassDefFoundError`

### Requirement: 已知反射点必须完成改造

下列已盘点的反射点 MUST 在本变更内完成改造，改造后全量测试必须通过：

| 文件 | 现状构造 | 处置方式 |
|---|---|---|
| `ping-web/RestWebAutoConfiguration` | `Class.forName` ×6 + 反射构造 `ModelResolver`、admin marker | 改为 optional 依赖 + `@ConditionalOnClass` 直接类型引用 |
| `ping-web/cipher/id/CipherSecurityBuilder` | `getDeclaredFields` + `setAccessible` 字段拷贝 | 改为显式构造或注入 |
| `ping-jpa/.../PredicateAnnotationExtractor` | `getDeclaredField` + `setAccessible` | 改为静态可推导的访问路径 |

#### Scenario: 全仓反射清零

- **WHEN** 改造完成后执行反射构造扫描
- **THEN** 上述三处不再出现相应构造

#### Scenario: 行为回归

- **WHEN** 执行 `./mvnw -Ph2 test`
- **THEN** 全部测试通过，`PredicateAnnotationExtractor` 覆盖的投影查询行为与改造前一致

### Requirement: 编译期生成必须替代运行时动态

需要在运行时动态定位或构造的协作对象（Repository、启动入口等）SHALL 通过 `ping-apt` 在编译期生成静态代码实现。MUST NOT 依赖运行时扫描、运行时代理工厂或字符串驱动的类型装配。

#### Scenario: Repository 保持编译期生成

- **WHEN** 检查任一 `@RepositoryCreator` 实体所在模块
- **THEN** `target/generated-sources/annotations` 中存在对应 `XxxRepository`，且运行时无创建该 Repository 的动态工厂

#### Scenario: 启动入口为静态类

- **WHEN** 检查业务模块生成的 `<Module>Launcher`
- **THEN** 其启动类引用为编译期可确定的类字面量，AOT 处理时无需额外反射 hint

### Requirement: 不支持 GraalVM 的 jar 不得进入聚合发布

用于 GraalVM 编译发布的聚合构建 SHALL 只引入通过源码可移植性规则的模块。发现不可移植实现时，SHALL 先改造其源码；确实无法改造的模块 MUST NOT 出现在 `-Pnative` 构建的依赖树中。

#### Scenario: native 构建依赖树受限

- **WHEN** 执行 `./mvnw -Ph2 -Pnative -DskipTests package`
- **THEN** 依赖树中每个 `cn.procsl` 模块均满足本能力的反射规则

#### Scenario: 发现不支持模块

- **WHEN** 某模块存在无法改造的运行时反射且需参与 native 发布
- **THEN** 该模块从聚合 pom 中移除，并记录其为非 native 兼容模块

### Requirement: native 发布必须走聚合层

GraalVM 编译发布 SHALL 由 `ping-distribute` 的 `-Pnative` profile 完成，MUST NOT 由业务模块单独执行 native 编译。

#### Scenario: native 构建入口唯一

- **WHEN** 搜索 `-Pnative` 与 `native-maven-plugin` 的声明
- **THEN** 仅 `ping-distribute` 声明了 native 编译 profile

#### Scenario: native 产物可运行

- **WHEN** native 构建成功后运行产物
- **THEN** 应用启动成功，`DistributeConfiguration` 注册的代理 hint 生效
