# Tasks: standalone-module-packaging

## 1. 分层调整（无行为变化）

- [ ] 1.1 根 `pom.xml` `<modules>` 加入 `ping-ui`，从 `ping-parent/pom.xml` 的模块列表中移除（当前为注释状态，直接删除注释项）
- [ ] 1.2 根 `pom.xml` `dependencyManagement` 补充 `cn.procsl:ping-ui` 版本条目
- [ ] 1.3 `ping-ui` 的 `UIAutoConfiguration` 补 `@AutoConfiguration` 与 `@AutoConfigureAfter`，校验加载顺序
- [ ] 1.4 复核 `ping-system/pom.xml` 对 `ping-captcha` 的依赖，评估并移除业务→业务依赖（若验证码能力确需保留，则改造为 captcha 独立提供、由聚合层组合）
- [ ] 1.5 收紧 `build-profile-filter.groovy`：仅认 `application-<id>.properties`，删除 yml/yaml 分支
- [ ] 1.6 运行 `./mvnw -Ph2 test` 验证分层调整无回归

## 2. 构建契约：处理器选项与分层属性

- [ ] 2.1 根 `pom.xml` `properties` 增加 `<ping.standalone>false</ping.standalone>`
- [ ] 2.2 `ping-parent/pom.xml` `properties` 覆盖 `<ping.standalone>true</ping.standalone>`
- [ ] 2.3 `ping-distribute/pom.xml` `properties` 显式声明 `<ping.standalone>false</ping.standalone>`
- [ ] 2.4 根 `pom.xml` `maven-compiler-plugin` `<compilerArgs>` 追加 `-Aping.module=${project.artifactId}` 与 `-Aping.standalone=${ping.standalone}`
- [ ] 2.5 `ping-apt` 的 `RepositoryAptProcessor` 声明 `@SupportedOptions({"ping.module", "ping.standalone"})` 并读取选项

## 3. 生成器：Launcher 与 module.properties

- [ ] 3.1 `ping-apt` 新增 Launcher 生成器：当 `ping.standalone=true` 时生成 `cn.procsl.ping.launch.<Module>Launcher`，`main` 内使用 `SpringApplication.run(<Module>Launcher.class, args)`
- [ ] 3.2 由 `ping.module` 推导类名（`ping-product` → `PingProductLauncher`），含连字符与多段命名的转义规则
- [ ] 3.3 `ping-apt` 生成 `META-INF/ping/module.properties`（`module`、`standalone`、`version` 键）
- [ ] 3.4 业务模块 `pom.xml` 声明 `spring-boot-maven-plugin` `repackage`（不写 `<mainClass>`），`ping-distribute` 保持既有 `<mainClass>`
- [ ] 3.5 `./mvnw -f ping-apt/pom.xml install -DskipTests` 后构建 `ping-product`，校验生成源存在且 `repackage` 自动发现入口
- [ ] 3.6 校验基础层（`ping-web`）与聚合层（`ping-distribute`）均未生成 Launcher
- [ ] 3.7 `./mvnw -Ph2 -DskipTests package` 校验单体 `Start-Class` 仍为 `cn.procsl.ping.app.DistributeApplication`

## 4. 打包期生成器：OpenAPI 按模块落位

- [ ] 4.1 抽取 `ping-distribute` 的启动导出配置（`exec-maven-plugin` + `openapi.export`）为可复用片段
- [ ] 4.2 业务模块接入该片段于 `prepare-package`，导出至 `${project.build.outputDirectory}/META-INF/ping/openapi/<module>.json`
- [ ] 4.3 `SystemAutoConfiguration.openApiJsonExporter` 的输出路径改为读取 `META-INF/ping/openapi/` 前缀（保留 `openapi.output.dir` 作为目录参数）
- [ ] 4.4 增加 `-Dping.openapi.skip` 开关
- [ ] 4.5 聚合层在模块分片之外额外生成 `_aggregate.json`
- [ ] 4.6 修改 `UiSchemaRepository.loadOpenapiDoc()`：改为按 `META-INF/ping/openapi/` 下分片合并读取，删除 `classpath:ping-api-doc/openapi.json` 引用
- [ ] 4.7 全仓搜索 `ping-api-doc` 确认无残留引用
- [ ] 4.8 分别验证：单模块打包产物含分片、单体打包产物含分片与聚合、`-Dping.openapi.skip` 可跳过

## 5. 打包期生成器：测试配置

- [ ] 5.1 `ping-apt` 新增测试配置生成器，产出 `META-INF/ping/test-config/<module>.properties`（数据源类型、profile、JPA 开关、端口占位）
- [ ] 5.2 提供共享测试加载入口（`ApplicationContextInitializer` 或测试基类），按「生成默认值 → 模块 test resources 覆盖」顺序合并
- [ ] 5.3 迁移 `ping-system`、`ping-product` 的 `src/test/resources/application.properties` 中的重复键到生成默认值，保留模块特有键
- [ ] 5.4 校验产物 jar 中 `META-INF/ping/test-config/` 不含真实凭据
- [ ] 5.5 `./mvnw -Ph2 test` 全量回归

## 6. GraalVM 源码改造

- [ ] 6.1 `ping-web` 增加 springdoc `optional` 依赖；将 `RestWebAutoConfiguration.modelResolver()` 的 `Class.forName` ×6 改为独立 `@Configuration` + `@ConditionalOnClass` 直接类型引用
- [ ] 6.2 同样处理 admin marker 的 `Class.forName`（`initialize` 方法）
- [ ] 6.3 `CipherSecurityBuilder` 的 `getDeclaredFields` + `setAccessible` 字段拷贝改为显式构造/注入
- [ ] 6.4 `PredicateAnnotationExtractor` 的 `getDeclaredField` + `setAccessible` 改为静态可推导的访问路径
- [ ] 6.5 `./mvnw -Ph2 test` 全量回归，重点校验投影查询与 cipher 相关用例
- [ ] 6.6 增加静态检查（构建期脚本或 enforcer 规则）：`src/main/java` 禁止 `Class.forName`、`setAccessible`、`Proxy.newProxyInstance`，例外需同模块 `RuntimeHintsRegistrar` 佐证
- [ ] 6.7 `./mvnw -Ph2 -Pnative -DskipTests package` 验证 native 构建依赖树与运行

## 7. 约束文档化与收口

- [ ] 7.1 更新 `AGENTS.md`：三层结构与依赖方向、`ping.standalone` 属性、`META-INF/ping/` 产物约定、`ping-apt` 先行安装约束
- [ ] 7.2 记录业务模块 `src/main` 不得自带 `main` 的约束，并加校验
- [ ] 7.3 `openspec verify standalone-module-packaging` 通过后按需归档
