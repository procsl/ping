# Proposal: standalone-module-packaging

## Why

项目要求"散是满天星，聚是一团火"——各业务模块既能独立作为微服务运行，也能整合进一个 jar。但目前构建层完全没有支撑：

- 所有 `*Application` 都在 **test scope**，没有任何业务模块带 `spring-boot-maven-plugin` repackage，"独立运行"实际等于"在测试里能起"。
- `ping-distribute` 硬编码依赖 `ping-product` + `ping-system`，每加一个模块都要改 pom。
- `ping-api-doc/openapi.json` 是 `ping-distribute` 打包期独占产出的**单点文件**，而 `UiSchemaRepository` 硬读 `classpath:ping-api-doc/openapi.json` —— 业务模块单独运行必然失败。业务层反向依赖了聚合层的构建产物。
- 分层未定义：`ping-ui` 挂在 `ping-parent`（业务层）且已被注释停用，无法成为"需要前端的模块"的公共依赖。
- GraalVM native 可移植性没有约束，反射用法散落各处，无人知道哪些模块能进 native 发布。

现在是架构定型期，模块尚未完全搭建完成，此时固化分层与构建契约的成本最低。

## What Changes

- **定义三层结构**（基础层 / 业务层 / 聚合层）与依赖方向，明确各模块归属；`ping-ui` 从 `ping-parent` 移入基础层并重新启用构建。
- **定义正交性约束**：业务模块之间不得相互依赖；业务模块不得依赖聚合层的构建产物。
- **新增两种打包形态**：散（单业务模块可执行 jar）与聚（`ping-distribute` 大单体），同一份 classpath、同一条流水线的两种出口。
- **mainClass 代码生成自动注入**：`ping-apt` 在编译期生成 `<Module>Launcher`（`SpringApplication` 传 class literal，AOT 原生识别）并写出含 `Start-Class` 的 MANIFEST；`spring-boot-maven-plugin` 无需在 pom 中声明 mainClass。聚合打包走 `DistributeApplication`，不生成 Launcher。
- **扩展 `ping-apt` 生成器**：除既有 Repository 生成外，新增打包期生成器，至少包含 Launcher、OpenAPI 文档产物、测试配置文件；生成动作统一发生在打包阶段。
- **消除 OpenAPI 单点**：OpenAPI 文档在打包时自动生成，按模块落位，不再由聚合层独占。
- **建立 GraalVM 源码可移植性约束**：列出反射/动态代理禁用与改造规则；不支持 GraalVM 的实现需改用支持的方案；不支持 GraalVM 的 jar 不引入聚合发布。

## Capabilities

### New Capabilities

- `module-layering`: 模块三层结构（基础层/业务层/聚合层）、依赖方向、业务模块正交性约束、各模块归属。
- `standalone-packaging`: 散与聚两种打包形态、可执行 jar 的构成、mainClass 的生成式注入规则。
- `build-time-codegen`: `ping-apt` 打包期生成器的统一契约（Launcher、OpenAPI、测试配置等），及生成产物的落位约定。
- `graalvm-native-support`: 源码级 GraalVM 可移植性规则、反射处置方式、不可支持模块的排除方式。

### Modified Capabilities

<!-- openspec/specs/ 当前为空，无既有能力需求变更 -->

## Impact

- **构建**：根 `pom.xml`、`ping-parent/pom.xml`（模块列表调整）、`ping-distribute/pom.xml`（repackage 配置简化、生成器接入）、各业务模块 pom（新增 repackage）。
- **代码生成**：`ping-apt` 新增生成器（Launcher、OpenAPI 落位、测试配置）。
- **模块结构**：`ping-ui` 移层并启用；`ping-system` 去除对聚合层产物的运行时依赖。
- **GraalVM 改造**：`ping-web/RestWebAutoConfiguration`（`Class.forName` ×6 → `@ConditionalOnClass` + 可选依赖）、`ping-web/cipher/id/CipherSecurityBuilder`（字段反射拷贝）、`ping-jpa/.../PredicateAnnotationExtractor`（`getDeclaredField`）。
- **构建流程**：打包阶段新增生成步骤；`ping-apt` 需先 `install` 才能参与编译（既有约束，需在规范中显式说明）。
- **不影响**：运行时 API 路径、数据库 schema、前端渲染（见变更 `abstract-component-rendering`）。
