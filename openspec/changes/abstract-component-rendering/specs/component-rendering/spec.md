# Capability: component-rendering

渲染器职责、组件解析与组件粒度的动态加载、命名空间与路由前缀。

## ADDED Requirements

### Requirement: 渲染器必须由 ping-ui 统一提供

系统 SHALL 由 `ping-ui` 提供唯一的渲染器 bundle 与主壳 `index.html`。散与聚两种形态 MUST 复用同一份渲染器，MUST NOT 为业务模块各自构建完整前端应用。

#### Scenario: 聚合形态使用共享渲染器

- **WHEN** 访问聚合层根 `index.html`
- **THEN** 页面加载 `ping-ui` 产出的渲染器 bundle 并渲染组件树

#### Scenario: 独立形态使用同一渲染器

- **WHEN** 访问某业务模块的 `/<module>/index.html`
- **THEN** 加载的仍是 `ping-ui` 产出的同一渲染器 bundle

### Requirement: 渲染必须完全由后端组件树驱动

渲染器 SHALL 仅依据后端返回的组件树渲染界面：按节点 `type` 解析组件、按 `order` 排序同级节点、按 `router` 建立前端路由。渲染器 MUST NOT 内置任何业务模块专属的页面结构。

#### Scenario: 后端返回即渲染

- **WHEN** 后端组件树新增一个菜单节点
- **THEN** 前端无需重新构建即可出现该菜单与对应路由

#### Scenario: 无业务硬编码

- **WHEN** 检查渲染器源码
- **THEN** 不存在以具体业务模块命名的页面或组件分支

### Requirement: 组件解析必须支持粒度动态加载

渲染器解析节点 `type` 时 SHALL 先查内置组件注册表；未命中时动态 `import` 对应模块的组件文件 `/assets/<module>/components/<type>.js`。加载对象 MUST 是单个组件，MUST NOT 加载整个子应用。

#### Scenario: 内置组件直接渲染

- **WHEN** 节点 `type` 命中内置注册表
- **THEN** 直接渲染，不发起动态加载

#### Scenario: 自定义组件动态加载

- **WHEN** 节点 `type` 未命中内置注册表且节点所属模块提供了该组件文件
- **THEN** 动态 `import` 成功并渲染该组件

#### Scenario: 组件文件缺失

- **WHEN** 节点 `type` 未命中内置注册表且组件文件不存在
- **THEN** 以占位节点渲染并输出控制台告警，页面其余部分不受影响

### Requirement: 动态加载必须允许失败且不阻断整页

组件动态加载 SHALL 为非阻断式：单个组件加载失败 MUST NOT 导致整棵组件树渲染中断，其余节点 SHALL 正常渲染。

#### Scenario: 部分失败

- **WHEN** 组件树中一个自定义组件加载失败
- **THEN** 其余菜单、布局与页面节点全部正常渲染

### Requirement: 命名空间必须由模块前缀隔离

模块的组件文件与静态资源 SHALL 落位在以模块名为前缀的路径下（`/assets/<module>/`、`/<module>/`）。组件 JS SHALL 封装在以模块名命名的作用域内，CSS 类名 SHALL 使用模块前缀。模块间 MUST NOT 共享全局符号。

#### Scenario: 路径隔离

- **WHEN** 两个模块分别提供同名组件文件
- **THEN** 二者路径互不冲突，可同时加载

#### Scenario: 样式隔离

- **WHEN** 两个模块各自定义 `.panel` 类
- **THEN** 以模块前缀区分后的选择器互不覆盖

### Requirement: 前端路由必须使用模块前缀

组件树中菜单节点的 `router` SHALL 以所属模块名为前缀。前端路由表 SHALL 由组件树推导，MUST NOT 由前端静态配置维护。

#### Scenario: 路由由数据推导

- **WHEN** 后端组件树变化
- **THEN** 前端路由表随之变化，无需前端发版

#### Scenario: 模块前缀生效

- **WHEN** 模块 `foo` 的菜单节点 `router` 为 `foo/list`
- **THEN** 浏览器地址栏路径为 `/foo/list`，且不与其他模块路由冲突

### Requirement: 权限拦截必须落在菜单层与 API 层

组件树 SHALL 按当前用户权限过滤后返回；后端接口 SHALL 沿用既有认证拦截。静态资源 MUST NOT 纳入登录拦截，以保证登录页可达。

#### Scenario: 无权限节点不返回

- **WHEN** 用户无某菜单节点的权限
- **THEN** `/v1/ui/menus` 的响应中不含该节点及其子树

#### Scenario: 登录页可达

- **WHEN** 未登录用户访问任意静态资源
- **THEN** 返回静态内容而非认证错误

#### Scenario: 未授权接口被拦截

- **WHEN** 未登录用户调用受保护的业务接口
- **THEN** 按既有规则返回未登录错误
