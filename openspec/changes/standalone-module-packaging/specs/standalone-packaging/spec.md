# Capability: standalone-packaging

散（单业务模块可执行 jar）与聚（大单体）两种打包形态，及 mainClass 的生成式注入。

## ADDED Requirements

### Requirement: 业务模块必须可独立打包为可执行 jar

每个业务模块 SHALL 可通过 `./mvnw -f <module>/pom.xml -Ph2 -DskipTests package` 产出一个可执行 jar；该 jar MUST 含 Spring Boot 启动清单，且可直接以 `java -jar` 运行并监听配置端口。

#### Scenario: 业务模块产出可执行 jar

- **WHEN** 对 `ping-product` 执行 `./mvnw -f ping-product/pom.xml -Ph2 -DskipTests package`
- **THEN** 生成的 jar 中 `META-INF/MANIFEST.MF` 含有 `Start-Class` 与 `Main-Class: org.springframework.boot.loader.launch.JarLauncher`

#### Scenario: 独立 jar 可启动

- **WHEN** 执行 `java -jar target/ping-product-*.jar`
- **THEN** 应用成功启动，且仅加载该模块及其基础层依赖的自动配置

### Requirement: mainClass 必须由编译期生成提供

业务模块的 `pom.xml` MUST NOT 声明 `spring-boot-maven-plugin` 的 `<mainClass>`。启动入口类 SHALL 由 `ping-apt` 在编译期生成，`repackage` 通过 `MainClassFinder` 自动发现。

#### Scenario: pom 中无 mainClass 声明

- **WHEN** 检查任一业务模块 `pom.xml` 的 `spring-boot-maven-plugin` 配置
- **THEN** 不存在 `<mainClass>` 元素，且 `repackage` goal 已启用

#### Scenario: 生成的 Launcher 使用 class literal

- **WHEN** 检查生成源 `cn.procsl.ping.launch.<Module>Launcher`
- **THEN** 其 `main` 方法以 `SpringApplication.run(<Module>Launcher.class, args)` 形式引用启动类（类字面量），而非字符串形式的配置类名

#### Scenario: 模块内唯一 main

- **WHEN** `ping.standalone=true` 的模块被 `repackage` 处理
- **THEN** `target/classes` 中仅存在一个 `main` 方法，即生成的 `<Module>Launcher`

### Requirement: 打包形态由分层属性决定

`ping.standalone` 属性 SHALL 决定是否生成 Launcher：基础层与聚合层 MUST 为 `false`，业务层 MUST 为 `true`。该属性 SHALL 在根 `pom.xml` 给出默认值，由 `ping-parent` 与 `ping-distribute` 各自覆盖，业务模块 MUST NOT 逐个声明。

#### Scenario: 基础层不生成 Launcher

- **WHEN** 构建 `ping-web`
- **THEN** 生成源目录中不存在 `cn.procsl.ping.launch` 下的 Launcher 类

#### Scenario: 业务层默认生成

- **WHEN** 构建 `ping-system`
- **THEN** 生成源目录中存在 `cn.procsl.ping.launch.PingSystemLauncher`（名称按 `<Module>Launcher` 规则生成）

### Requirement: 聚合打包不注入业务 Launcher

聚合打包 SHALL 仅以 `cn.procsl.ping.app.DistributeApplication` 为入口。`ping-distribute` 的 `repackage` MUST NOT 扫描或引用依赖 jar 中的业务 Launcher。

#### Scenario: 单体 jar 入口唯一

- **WHEN** 执行 `./mvnw -Ph2 -DskipTests package` 并检查 `ping-distribute` 产物 MANIFEST
- **THEN** `Start-Class` 为 `cn.procsl.ping.app.DistributeApplication`

#### Scenario: 依赖 jar 中的 Launcher 不影响单体

- **WHEN** 单体 jar 中同时包含业务模块的 `<Module>Launcher` 字节码
- **THEN** 单体启动仍走 `DistributeApplication`，且启动日志显示自动配置来源覆盖全部引入的业务模块

### Requirement: 两种形态必须共用同一套生成机制

散与聚 SHALL 由同一次 `ping-apt` 编译期生成产出，MUST NOT 为两种形态维护两套入口或两套构建脚本。聚合形态 MUST NOT 关闭或绕过编译期生成。

#### Scenario: 同一模块两种形态产物一致

- **WHEN** 分别单独构建与在单体中引入同一业务模块
- **THEN** 该模块的 class 内容与 `META-INF/ping/` 下的生成产物完全一致
