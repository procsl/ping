# Capability: build-time-codegen

`ping-apt` 打包期生成器的统一契约、产物落位与构建流程约束。

## ADDED Requirements

### Requirement: 生成器必须以 ping-apt 为唯一宿主

所有编译期与打包期生成产物 SHALL 由 `ping-apt` 及其配套构建步骤产出。业务模块 MUST NOT 各自实现私有的代码或配置生成逻辑。

#### Scenario: 新增生成器落在 ping-apt

- **WHEN** 需要为模块生成新的构建期产物
- **THEN** 实现位于 `ping-apt` 模块内，并通过既有的 `annotationProcessorPaths` 分发到所有模块

#### Scenario: 业务模块无自定义生成插件

- **WHEN** 检查业务模块 `pom.xml`
- **THEN** 不存在仅服务于该模块的代码生成或配置生成插件

### Requirement: 处理器选项必须由根 pom 自动注入

`ping-apt` 所需的处理器选项 SHALL 由根 `pom.xml` 的 `maven-compiler-plugin` 统一以 `-A<key>=<value>` 形式注入，取值使用 Maven 属性（如 `${project.artifactId}`）在各模块解析。业务模块 MUST NOT 逐个声明这些选项。

#### Scenario: 模块标识自动传递

- **WHEN** 构建 `ping-product`
- **THEN** 注解处理器可读取到标识为 `ping-product` 的模块选项，且该模块 `pom.xml` 中无 `-A` 形式的 compilerArg

#### Scenario: 分层属性自动传递

- **WHEN** 构建基础层模块 `ping-web`
- **THEN** 注解处理器读取到的 standalone 标志为关闭状态

### Requirement: 生成产物必须落位于 META-INF/ping

生成产物 SHALL 统一落位于类路径 `META-INF/ping/` 下，按用途分目录。运行时代码 SHALL 只读取该固定路径下的文件，MUST NOT 使用 `classpath*:` 通配扫描或依赖聚合层独占路径。

#### Scenario: 产物路径固定

- **WHEN** 构建任一业务模块并解压其 jar
- **THEN** 存在 `META-INF/ping/module.properties`，且该模块若启用了 OpenAPI 导出则同时存在 `META-INF/ping/openapi/<module>.json`

#### Scenario: 运行时无通配扫描

- **WHEN** 搜索业务模块运行时源码中的 `classpath*:` 字符串
- **THEN** 不存在用于定位生成产物的通配表达式

### Requirement: OpenAPI 必须在打包期按模块生成

每个启用打包的模块 SHALL 在 `prepare-package` 阶段生成自身的 OpenAPI 文档并写入 `META-INF/ping/openapi/<module>.json`；聚合层 SHALL 额外生成聚合文档。生成步骤 MUST 可通过 `-Dping.openapi.skip` 跳过。

#### Scenario: 单模块生成自身文档

- **WHEN** 执行 `./mvnw -f ping-product/pom.xml -Ph2 -DskipTests package`
- **THEN** 产物 jar 中存在 `META-INF/ping/openapi/ping-product.json`，内容为该模块暴露的接口定义

#### Scenario: 聚合层生成聚合文档

- **WHEN** 执行 `./mvnw -Ph2 -DskipTests package` 并完成 `ping-distribute` 打包
- **THEN** 聚合产物中同时存在各引入模块的分片文档与聚合文档

#### Scenario: 跳过导出

- **WHEN** 以 `-Dping.openapi.skip` 执行打包
- **THEN** 构建成功且不启动导出进程

### Requirement: 必须生成测试配置元数据

每个业务模块 SHALL 生成 `META-INF/ping/test-config/<module>.properties`，包含该模块测试所需的默认配置（数据源类型、profile、JPA 开关、端口占位等）。模块的 `src/test/resources/application-*.properties` SHALL 作为覆盖层，MUST NOT 与生成内容重复声明相同键。

#### Scenario: 测试默认配置可获取

- **WHEN** 运行 `ping-system` 的测试
- **THEN** 测试上下文可读取到 `META-INF/ping/test-config/ping-system.properties` 中的默认值

#### Scenario: 模块可覆盖生成默认值

- **WHEN** 模块在 `src/test/resources/application-test.properties` 中声明了同一键
- **THEN** 测试使用模块声明的值而非生成的默认值

#### Scenario: 产物不含测试配置内容

- **WHEN** 解压业务模块产物 jar
- **THEN** `META-INF/ping/test-config/` 中的文件不含任何真实数据源凭据

### Requirement: ping-apt 必须先行安装

构建流程 SHALL 保证 `ping-apt` 在参与编译前已完成安装。首次构建或 `ping-apt` 版本变更后 MUST 执行 `./mvnw -f ping-apt/pom.xml install -DskipTests`。

#### Scenario: 首次构建

- **WHEN** 在干净环境中首次执行 `./mvnw -Ph2 test`
- **THEN** 先完成 `ping-apt` 安装，随后编译成功且生成产物存在

#### Scenario: 生成器版本一致

- **WHEN** 检查 `annotationProcessorPaths` 中 `cn.procsl:ping-apt` 的版本
- **THEN** 与 `project.version` 一致
