# Capability: module-static-assets

模块静态资源打包落位、薄壳入口生成，及散/聚两种访问路径。

## ADDED Requirements

### Requirement: 模块静态资源必须打包进各自 jar

需要前端资源的模块 SHALL 将其静态产物置于 `src/main/resources/META-INF/resources/<module>/`（或打包期复制到 `${project.build.outputDirectory}/META-INF/resources/<module>/`），随该模块 jar 发布。模块静态资源 MUST NOT 只存在于聚合层。

#### Scenario: 资源随模块走

- **WHEN** 单独打包某含前端资源的业务模块
- **THEN** 其 jar 内含 `META-INF/resources/<module>/` 及其中全部文件

#### Scenario: 资源进入聚合产物

- **WHEN** 聚合打包引入该模块
- **THEN** 聚合 jar 内同样含 `META-INF/resources/<module>/`，路径与单模块产物一致

### Requirement: 必须提供模块入口薄壳

每个需要前端的模块 SHALL 在 `META-INF/resources/<module>/index.html` 拥有入口。该入口 SHALL 由 `ping-ui` 提供的共享模板按模块名生成，内容为加载共享渲染器并传入命名空间。业务模块 MUST NOT 各自手写入口。

#### Scenario: 薄壳自动生成

- **WHEN** 打包业务模块 `foo`
- **THEN** 产物中存在 `META-INF/resources/foo/index.html`，其内容 import 共享渲染器且命名空间为 `foo`

#### Scenario: 入口无需业务模块配置

- **WHEN** 检查业务模块 `pom.xml`
- **THEN** 不存在手写入口 HTML 的资源配置，仅需声明模块标识属性

#### Scenario: 渲染器在散形态可用

- **WHEN** 独立运行某业务模块并访问 `/<module>/index.html`
- **THEN** 页面成功加载 `ping-ui` 的渲染器 bundle（因其为基础层依赖自动在位）

### Requirement: 必须支持散形态的模块入口路径

散形态（独立模块运行）SHALL 支持直接访问 `/<module>/index.html` 并完整渲染该模块界面。该路径 MUST 在不依赖聚合层任何产物的情况下可用。

#### Scenario: 独立访问成功

- **WHEN** 业务模块以独立可执行 jar 运行，访问 `http://<host>:<port>/<module>/index.html`
- **THEN** 返回 200，页面加载渲染器并渲染该模块组件树

#### Scenario: 无聚合层依赖

- **WHEN** 独立形态运行
- **THEN** 不出现因缺少聚合层产物导致的资源 404 或启动失败

### Requirement: 必须支持聚形态的主壳入口

聚形态（单体 jar）SHALL 在根路径提供 `index.html` 主壳，渲染全站组件树；同时 MUST 保留 `/<module>/index.html` 供模块深链与调试。

#### Scenario: 主壳渲染全站

- **WHEN** 聚合层运行，访问 `http://<host>:<port>/index.html`
- **THEN** 返回 200 并渲染包含全部引入模块的组件树

#### Scenario: 模块深链仍可用

- **WHEN** 聚合层运行，访问 `/<module>/index.html`
- **THEN** 返回 200，渲染器以对应命名空间初始化

#### Scenario: 同一份渲染器

- **WHEN** 比较主壳与薄壳加载的渲染器 bundle
- **THEN** 二者为同一构建产物，版本一致

### Requirement: 自定义组件构建产物必须落位模块命名空间

含自定义组件的模块 SHALL 通过 node 构建产出组件文件并复制到 `META-INF/resources/<module>/components/`。构建输出目录 SHALL 与打包复制步骤在模块 `pom.xml` 中声明，复制目标 MUST 位于模块命名空间子目录内。

#### Scenario: 构建产物复制

- **WHEN** 模块执行包含前端构建的打包
- **THEN** 构建输出被复制到 `META-INF/resources/<module>/components/`

#### Scenario: 无前端构建的模块可跳过

- **WHEN** 模块不含自定义组件
- **THEN** 打包不执行任何 node 构建，且不因缺少前端工程而失败

#### Scenario: 命名空间不越界

- **WHEN** 检查复制目标路径
- **THEN** 目标以 `META-INF/resources/<module>/` 开头，不写入 `META-INF/resources/` 根

### Requirement: 静态资源必须保持免登录访问

静态资源路径 SHALL 继续纳入既有静态资源放行列表，MUST NOT 因引入模块前缀而纳入登录拦截。

#### Scenario: 静态资源放行

- **WHEN** 未登录用户请求 `/<module>/index.html` 或其静态资源文件
- **THEN** 返回资源内容而非认证错误

#### Scenario: 前缀纳入放行列表

- **WHEN** 检查静态资源放行匹配规则
- **THEN** 模块命名空间下的 html、js、css、图片均被覆盖
