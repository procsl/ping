# 抽象组件描述规范

> 模块：`ping-ui`（基础层）｜ 消费方：各业务模块
> 本文是面向**组件编写者**的参考手册，只解释「怎么写」。SDK 运行机制
> （处理器注册、`run`/`create`、生命周期、错误降级）见 `ping-ui/doc/sdk-design.md`。

> **状态：目标态规范，代码尚未迁移。**
> 节点模型以本文为准（`children` + `properties`）。前端当前实现
> （`schema/types.ts`、`components/renderer/registry.tsx`、`router/derive.ts`）仍是
> **扁平模型**（类型专有字段平铺在节点上，子节点键为 `containers`），迁移工作见 §11。
> 后端构建期聚合链路已随 `4fb05681` 移除，标注见 §7。

---

## 1. 概览

抽象组件用 JSON 描述**功能骨架**，与布局、样式分离。

**三条不变量：**

1. 描述只表达**语义**，不限制前端渲染形态 —— 同一个 `dataset` 可被渲染成表格、卡片或看板。
2. 运行时**只读固定路径**，禁止 `classpath*:` 通配扫描；放在约定路径之外的描述不生效（也不报错）。
3. 任何描述问题（缺字段、非法 JSON、引用未命中）都**降级 + 告警，启动永不失败**。

**命名约定**：所有类型名与 JSON 属性名一律 **snake_case**（AGENTS.md：JSON SNAKE_CASE），
与既有的 `user_info_panel` / `main_container` 一致 —— 用 `nav_group`、`row_action`、
`form_action`、`date_picker`，不用连字符。

---

## 2. 节点模型与字段字典

### 2.1 通用字段（所有节点可用）

每个组件节点**只保留 5 个通用字段**，组件特有字段一律放入 `properties` 对象：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `type` | string | **是** | 组件类型，前端按此查渲染注册表。缺失时该节点及子树不进入注册表 |
| `id` | string | 建议 | 全树唯一 Key，用于 React key、去重、元素定位 |
| `name` | string | 否 | 展示文案 / 标题 |
| `children` | array | 否 | 子节点数组 |
| `properties` | object | 否 | 组件特有字段集合（见 §5 各类型定义） |

节点上**不出现** `type` / `id` / `name` / `children` / `properties` 以外的字段 ——
`router`、`order`、`icon`、`api`、`field`、`widget` 等一律进 `properties`。

**字段约束：**

- `children` 中的子节点**顺序不作为语义**，同级顺序一律由 `properties.order` 决定（缺省视为 `0`，升序）。
- `id` 在同一棵装配树内重复 → 后装配者覆盖，构建期记 WARN。
- `name` 缺失时前端回落 `id`，`id` 也缺失时回落 `type`。
- 组件可**任意嵌套**：容器套容器、动作套数据表、表单套页签，均合法；嵌套不改变子节点自身的类型与数据契约。

### 2.2 `properties`：两个不同层次的同名字段

`properties` 在**类型定义**与**组件实例**中都出现，但含义不同，不要混淆：

| 位置 | 含义 | 形态 |
|---|---|---|
| `types/<type>.json` 的 `properties` | 该类型的**属性模式声明**：有哪些属性、什么类型、是否必填 | 键值对象，值是属性描述符（§4.1） |
| 组件实例节点的 `properties` | 该节点**实际取值** | 键值对象，值是任意 JSON 值 |

即：类型定义里声明 `properties.router` 是 string，实例里就写 `"properties": { "router": "system/user" }`。

`properties` 中常见的跨类型字段：`order`（同级排序）、`router`（前端路由）、
`description`（描述）、`icon`（图标标识）、`api`（接口引用，§2.3）。各类型专有字段见 §5。

### 2.3 `api` 字段：引用与解析

`api` 是 `properties` 中的一个字段。**写的时候**是引用标记：

```json
"properties": { "api": "@ResourceReference:获取用户列表" }
```

**装配后**被就地替换为完整描述（依据打包期导出的 `ping-api-doc/openapi.json`）：

```json
"properties": {
  "api": {
    "method": "GET",
    "path": "/v1/system/users",
    "request":  { "content_type": "application/json", "schema": { "...": "入参结构" } },
    "response": { "status": "200", "content_type": "application/json", "schema": { "...": "出参结构" } }
  }
}
```

- `request.schema` → 该组件的**输入参数定义**（前端据此渲染查询表单 / 表单字段）
- `response.schema` → 该组件的**渲染数据契约**（前端据此渲染数据）

**引用生效的前提**：目标接口的处理方法必须标注
`cn.procsl.ping.boot.ui.component.api.ResourceReference`。未标注的接口不可被引用。

**解析失败时 `api` 保持引用原样 + WARN**，组件照常装配与渲染，只是没有数据契约。

---

## 3. `compose.json` 文件约定

### 3.1 路径与格式

```
ping-system/src/main/resources/ui/user-center/compose.json
                    └─ 源位置固定；构建期复制并聚合到 META-INF/ping/ui/
```

- 一个文件 = 一个声明单元，UTF-8，必须是合法 JSON（**不允许注释、不允许尾逗号**）。
- 该目录与 `META-INF/ping/ui/**` **不参与 `@...@` 资源过滤**，因此可以安全地写
  `@ResourceReference:xxx` 而不会被改写。

### 3.2 顶层类型决定该文件的装配归宿

| 顶层 `type` | 角色 | 装配行为 |
|---|---|---|
| `application` | 应用外壳树 | 进入 `pages`；按 `id` 去重，**约定仅 `ping-ui` 提供这一棵** |
| `layout` / `nav_group` / `menu` / `breadcrumb` / `tabs` | **导航片段** | 按 `order` 合并进 `pages` 中应用外壳树的对应位置（`layout` 按方位并入，其余并入 `layout[left]`） |
| 其它任意类型 | **页面片段** | 用顶层节点 `properties.router` 作挂载键，挂到外壳树中 `router` 相同的 `menu` 节点的 `children` 末尾；**无匹配时不报错**，原样进入 `pages`，前端按其 `router` 自行路由 |

> 所以业务模块**通常只写页面片段**，不需要重复整棵应用树。

---

## 4. 组件类型总览与类型定义文件

内置 **16 个常用后台管理抽象组件类型** + **2 个框架壳体类型**，共 18 个。

| 分类 | 类型 | 职责 | 允许的子节点 |
|---|---|---|---|
| 容器 | `application` | 应用根，全树入口 | `layout` `nav_group` `menu` `tabs` `breadcrumb` `user_info_panel` `main_container` |
| 容器 | `layout` | 布局方位槽 | 任意容器与内容类型 |
| 导航 | `nav_group` | 可展开/折叠的菜单分组 | `menu` |
| 导航 | `menu` | 菜单项 / 页面入口 | 分组形态：`menu`；叶子形态：页面内容节点 |
| 导航 | `tabs` | 页签容器 | 任意组件（页签标题取 `name`） |
| 导航 | `breadcrumb` | 面包屑 | 无（用 `properties.items` 属性） |
| 数据 | `dataset` | **逻辑数据表**，可被前端任意渲染 | `query` `column` `action` `row_action` |
| 数据 | `query` | 数据表的输入参数容器 | `field` |
| 数据 | `column` | 展示列，附带排序等能力 | 无 |
| 数据 | `row_action` | 行内动作 | 无 |
| 表单 | `form` | 表单容器 | `field` `form_action` |
| 表单 | `field` | 表单字段 | 无 |
| 表单 | `form_action` | 表单底部动作 | 无 |
| 动作 | `action` | 工具栏 / 页面级动作 | 无 |
| 状态 | `placeholder` | 页面建设中占位 | 无 |
| 状态 | `empty` | 空态提示 | 无 |
| 壳体 | `user_info_panel` | 头部用户信息面板 | 无 |
| 壳体 | `main_container` | 路由出口（内容区） | 无 |

**语义分工要点：**

- `dataset` 是「**数据库的表**」：它描述的是逻辑数据、查询参数与列能力，**不是 UI 表格**。
- `action` vs `row_action`：`action` 在容器头部（工具栏 / 页头），`row_action` 在每一行数据内。
- `query` 与 `dataset` 是**兄弟**关系，不是 `children` 内嵌属性 —— 查询参数是数据表的输入，独立声明。

### 4.1 类型定义文件的结构

每个类型一份定义，位于 `ping-ui/src/main/resources/META-INF/ping/ui/types/<type>.json`：

```json
{
  "type": "nav_group",
  "name": "菜单分组",
  "category": "navigation",
  "description": "可展开与折叠的菜单分组标题，默认展开",
  "children": ["menu"],
  "properties": {
    "name":  { "type": "string",  "required": true,  "description": "分组标题" },
    "icon":  { "type": "string",  "required": false, "description": "图标标识" },
    "order": { "type": "integer", "required": false, "description": "同级排序，缺省 0" }
  }
}
```

| 字段 | 含义 |
|---|---|
| `type` | 类型标识，与文件名一致，也与节点的 `type` 值一致 |
| `name` | 展示名 |
| `category` | 分类：`container` \| `navigation` \| `data` \| `form` \| `action` \| `state` \| `shell` |
| `description` | 语义说明 |
| `children` | 允许的子节点类型：枚举数组；`[]` 表示无子节点；`["*"]` 表示任意类型 |
| `properties` | 该类型的**专有属性声明**，键为属性名，值为属性描述符；`required: true` 标出必填项 |

`category` 为 `shell` 的两个类型（`user_info_panel`、`main_container`）是框架壳体，不计入 16 个常用类型。

> **待重构**：这 18 个类型定义文件已随 `4fb05681` 从源码树移除，当前不存在于
> `src/main/resources`（`target/classes` 下的副本是旧构建残留）。恢复时按本文结构重建。

---

## 5. 分类详解

每个类型的格式统一为：**职责 / properties / children / 渲染 / 错误处理**。

### 5.1 应用与布局

#### `application` — 应用根

- **职责**：整棵组件树的根，声明应用标识与标题。
- **properties**：`name`（应用名，显示在侧栏 logo 位）。
- **children**：`layout`、`nav_group`、`menu`、`tabs`、`breadcrumb`、`user_info_panel`、`main_container`。
- **渲染**：不直接渲染，向 `AdminLayout` 透出 `name` 与子树。
- **错误处理**：缺失 `application` 根 → 前端降级本地声明并 WARN。

```json
{
  "type": "application",
  "name": "应用根",
  "category": "container",
  "description": "整棵组件树的根，声明应用标识与标题",
  "children": ["layout", "nav_group", "menu"],
  "properties": {
    "name": { "type": "string", "required": false, "description": "应用名，显示在侧栏 logo 位" }
  }
}
```

#### `layout` — 布局方位槽

- **职责**：把内容切分为方位区域，是外壳结构的骨架。
- **properties**：`layout`（`left` \| `right` \| `top` \| `bottom` \| `center`，缺省按 `center`）、`name`、`order`。
- **children**：任意容器与内容类型。
- **渲染**：`left` → 侧栏；`top` → 顶栏；装 `main_container` 的方位 → 内容区。
- **错误处理**：`properties.layout` 缺失 → 视为 `center` 并 WARN；`layout[left]` 缺失 → 侧栏为空但不崩溃。

```json
{
  "type": "layout",
  "name": "布局方位槽",
  "category": "container",
  "description": "把内容切分为方位区域，是外壳结构的骨架",
  "children": ["*"],
  "properties": {
    "layout": { "type": "string", "required": false, "description": "方位：left | right | top | bottom | center，缺省 center" }
  }
}
```

### 5.2 导航

#### `nav_group` — 菜单分组

- **职责**：可展开/折叠的菜单分组标题（对应侧栏「系统管理」这类小标题）。
- **properties**：`name`（**必填**，分组标题）、`order`、`icon`。
- **children**：`menu`。子节点不含 `menu` 时该分组不渲染。
- **渲染**：点击标题切换展开/折叠，**默认展开**；侧栏整体收起时只留一条分隔线；
  搜索态下强制展开且不可点击折叠。
- **错误处理**：`properties.name` 缺失回落 `id`；`id` 也缺失 → 该分组跳过并 WARN。

```json
{
  "type": "nav_group",
  "name": "菜单分组",
  "category": "navigation",
  "description": "可展开与折叠的菜单分组标题，默认展开",
  "children": ["menu"],
  "properties": {
    "name":  { "type": "string",  "required": true,  "description": "分组标题" },
    "icon":  { "type": "string",  "required": false, "description": "图标标识" },
    "order": { "type": "integer", "required": false, "description": "同级排序，缺省 0" }
  }
}
```

#### `menu` — 菜单项

- **职责**：一个可跳转的功能入口，同时是路由与页面内容的挂载点。
- **properties**：`name`、`order`、`icon`、`router`（叶子必填）、`external`（布尔，强制外链）。
- **children**：
  - **分组形态**（`children` 内含 `menu`）→ 渲染为分组标题，不生成路由；
  - **叶子形态**（含 `properties.router`）→ `children` 是该页面的内容（`dataset` / `form` / `placeholder` …）。
- **渲染**：侧栏只渲染跳转链接 + 激活态高亮，**同一时刻至多一个菜单项激活**，
  激活态与地址栏路由保持一致（刷新后不丢）；侧栏折叠时收敛为单字符图标按钮。
- **错误处理**：`properties.router` 为 `https://` 外链 → 新窗口打开，不进本地路由表；
  `router` 缺失且无子 `menu` → 侧栏不渲染该项，WARN。

```json
{
  "type": "menu",
  "name": "菜单项",
  "category": "navigation",
  "description": "可跳转的功能入口，同时是路由与页面内容的挂载点",
  "children": ["*"],
  "properties": {
    "router":   { "type": "string",  "required": false, "description": "前端路由，两段式相对路径；https:// 开头视为外链" },
    "icon":     { "type": "string",  "required": false, "description": "图标标识" },
    "order":    { "type": "integer", "required": false, "description": "同级排序，缺省 0" },
    "external": { "type": "boolean", "required": false, "description": "强制外链，新开窗口" }
  }
}
```

#### `tabs` — 页签容器

- **职责**：把多个平级内容放进同一页签区，用于详情页的多面板。
- **properties**：`name`、`active`（激活页签的 `id`，缺省取第一个子节点）。
- **children**：任意组件，**页签标题取子节点的 `name`**。
- **渲染**：页签条 + 内容面板；未激活页签默认不渲染其内容（避免无谓的数据请求）。
- **错误处理**：子节点缺 `name` → 标题回落 `id`，再缺则用 `type`；`properties.active` 指向不存在的 `id` → 回落第一个。

#### `breadcrumb` — 面包屑

- **职责**：页面级位置指示，不承载内容。
- **properties**：`name`、`items`（数组，每项 `{ "name": ..., "router": ... }`，末项无 `router`）。
- **children**：无（`children` 会被忽略）。
- **渲染**：分隔的路径链，末项高亮；有 `router` 的项可点击。
- **错误处理**：`properties.items` 缺失或非数组 → 不渲染面包屑，WARN；数组为空同理。

### 5.3 数据

#### `dataset` — 逻辑数据表

- **职责**：描述一张**逻辑表**：它从哪取数（`api`）、有哪些列（`column`）、怎么查（`query`）、
  有哪些动作（`action` / `row_action`）。**不指定渲染形态。**
- **properties**：

  | 属性 | 类型 | 说明 |
  |---|---|---|
  | `api` | object | 查询接口（装配后为完整描述，§2.3）。`request.schema` = 查询参数，`response.schema` = 行数据 |
  | `title` | string | 表标题，缺省回落 `name` |
  | `row_key` | string | 行唯一键，默认 `id` |
  | `selectable` | boolean | 是否允许多选行 |

- **children**：`query`（至多一个）、`column`（≥1）、`action`（0..n，工具栏）、`row_action`（0..n，行内）。
- **渲染**：可为表格、卡片、看板、图表等。**同一 `dataset` 描述在不同前端实现下可呈现完全不同，
  描述与接口返回不因此改变。**
- **错误处理**：
  - 无 `column` → 渲染「表格未声明 column：{id}」的提示框，不崩溃；
  - 无 `api` → 用本地演示行渲染并标注「未绑定」；
  - `response.schema` 与列 `field` 对不上 → 该列留空并 WARN（不抛错）。

#### `query` — 数据表的输入参数容器

- **职责**：声明该数据表**可被什么条件查询**，即表的「入参」。
- **properties**：`name`（表单区标题）、`collapse`（是否可折叠）、`collapsed`（初始折叠）。
- **children**：`field`（0..n，每个对应一个查询条件）。
- **渲染**：搜索/筛选区，与 `dataset` 头部同行或独立一行；
  提交时把 `field` 值拼进查询参数调 `dataset` 的 `api`。
- **错误处理**：无 `field` → 不渲染筛选区（而非渲染空表单）；
  `field` 的 `name` 与 `dataset.api.request.schema` 无对应 → 该条件提交后被后端忽略，前端 WARN。

#### `column` — 展示列

- **职责**：描述数据表的一列：取哪个字段、怎么显示、有哪些列级能力。
- **properties**：

  | 属性 | 类型 | 说明 |
  |---|---|---|
  | `field` | string | 对应 `response.schema` 中的字段名（**列的取值来源**） |
  | `title` | string | 列头，缺省回落 `name` |
  | `sortable` | boolean | 该列可排序（能力随列交付，由前端决定如何呈现） |
  | `width` | string \| number | 列宽 |
  | `align` | `left` \| `center` \| `right` | 对齐 |
  | `format` | string | 展示格式化提示（如 `date`、`money`、`enum`） |
  | `enum` | object | 字段值 → 展示文案映射（如 `{ "1": "启用", "0": "停用" }`） |
  | `fixed` | `left` \| `right` | 冻结列 |

- **children**：无。
- **渲染**：由 `dataset` 的渲染器决定呈现为表头单元格、卡片字段或看板列。
- **错误处理**：`field` 缺失 → 该列整体不渲染并 WARN；
  `sortable: true` 但 `dataset.api` 未声明排序参数 → 前端禁用排序入口并 WARN。

#### `row_action` — 行内动作

- **职责**：数据表**每一行**上的操作（编辑、删除、重置密码、审核…）。
- **properties**：`name`（按钮文案）、`api`（动作接口，`request.schema` 至少含行主键）、
  `confirm`（二次确认文案，空则不确认）、`danger`（危险动作标红）、
  `disabled_when`（依行字段禁用的提示表达式）。
- **children**：无。
- **渲染**：行尾动作列，或收进「更多」下拉；接口出参成功后按契约刷新当前行/整表。
- **错误处理**：缺 `api` → 按钮禁用并 WARN；`confirm` 存在时必须先确认再调接口。

### 5.4 表单

#### `form` — 表单容器

- **职责**：一组字段 + 一组动作的收集与提交单元。
- **properties**：

  | 属性 | 类型 | 说明 |
  |---|---|---|
  | `api` | object | 提交接口；`request.schema` = 表单字段集合，`response.schema` = 提交结果 |
  | `mode` | `create` \| `edit` | 创建 / 编辑；`edit` 时前端按行主键回填 |
  | `cols` | `1` \| `2` \| `3` | 字段栅格列数，默认 `1` |
  | `label_width` | string | 标签宽度 |

- **children**：`field`（0..n）、`form_action`（0..n）。
- **渲染**：普通表单、抽屉、弹窗、分步表单均由前端选择；描述不变。
- **错误处理**：`mode: edit` 而 `api` 缺 `response` → 不回填，按空表单渲染并 WARN；
  必填校验不通过时不发请求。

#### `field` — 表单字段

- **职责**：一个可输入/可选择的数据项，是入参 schema 到控件的映射单元。
- **properties**：

  | 属性 | 类型 | 说明 |
  |---|---|---|
  | `name` | string | **字段名**，对应 `api.request.schema` / `response.schema` 中的键（必填）。注意与节点的展示 `name` 是两回事 |
  | `label` | string | 标签，缺省回落 `name` |
  | `widget` | string | 控件类型：`input` `textarea` `select` `radio` `checkbox` `switch` `date_picker` `date_range` `upload` `number` `rate` `slider` |
  | `required` | boolean | 必填 |
  | `disabled` | boolean | 只读/禁用 |
  | `placeholder` | string | 占位提示 |
  | `default` | any | 默认值 |
  | `options` | array | 选项 `[{ "value": ..., "label": ... }]`，`select`/`radio`/`checkbox` 必需 |
  | `multiple` | boolean | 多选（`select`/`upload`） |
  | `pattern` / `min_length` / `max_length` / `min` / `max` | — | 校验约束 |
  | `col_span` | `1` \| `2` \| `3` | 跨越的栅格列数 |

- **children**：无。
- **渲染**：`widget` → `ping-ui/components/ui/*` 的具体控件；
  在 `dataset`/`query` 上下文中同一 `field` 渲染为查询条件控件。
- **错误处理**：
  - `name` 缺失 → 该字段跳过并 WARN（不渲染无名字段）；
  - `widget` 未实现 / 未映射 → 渲染占位输入框 + `console.warn`，不阻断整表；
  - `select`/`radio` 缺 `options` → 渲染为空下拉并 WARN。

#### `form_action` — 表单动作

- **职责**：表单提交区的按钮。
- **properties**：`name`（文案）、`kind`（`submit` \| `reset` \| `cancel`）、
  `api`（`kind: submit` 时通常复用 `form` 的 `api`，单独覆盖时才需要）、
  `danger`、`loading`（提交中态文案）。
- **children**：无。
- **渲染**：表单底部右侧；`submit` 触发校验→提交，`reset` 清空，`cancel` 返回。
- **错误处理**：一个 `form` 内至多一个 `kind: submit` 作为默认主按钮，多余的记 WARN；
  无 `submit` 时表单不可提交并提示。

### 5.5 动作与状态

#### `action` — 通用动作

- **职责**：容器级别的动作（工具栏按钮、页头按钮、卡片操作）。**不在行内**（行内用 `row_action`）。
- **properties**：`name`、`api`、`confirm`、`danger`、
  `variant`（`primary` \| `outline` \| `ghost`）、`placement`（`toolbar` \| `header`，默认 `toolbar`）。
- **children**：无。
- **渲染**：所属容器头部按钮区。
- **错误处理**：缺 `api` → 按钮可渲染但点击时提示「未绑定接口」并 WARN。

#### `placeholder` — 页面占位

- **职责**：明确告知「这个页面在建设中」，保证菜单切换有可辨识的内容差异。
- **properties**：`name`（页面名）。
- **children**：无。
- **渲染**：居中显示 `{name} · 页面建设中`。
- **错误处理**：`name` 缺失回落 `id`，再缺用「该页面」。

> 占位页与「无内容菜单项」**刻意区分**：后者落到框架默认工作台 `Dashboard`，
> 前者显示建设中提示。两者保证切换不同菜单项时内容区呈现可辨识的差异，
> 后续接入真实页面时替换该节点即可。

#### `empty` — 空态

- **职责**：数据为空时的提示与引导。
- **properties**：`name`（主文案）、`description`（次文案）、`action`（引导按钮文案）。
- **children**：无。
- **渲染**：图标 + 主文案 + 次文案 + 可选引导按钮。
- **错误处理**：无 `name` 时用「暂无数据」。

### 5.6 框架壳体类型

> 这两个不属于「常用后台管理抽象组件」的 16 个，由 `ping-ui` 外壳树使用，业务模块一般不写。

#### `user_info_panel` — 用户信息面板

- **职责**：顶栏右侧的当前登录用户展示与操作。
- **properties**：`api`（获取用户信息的接口）。
- **children**：无。
- **渲染**：头像 + 用户名 + 下拉（个人中心 / 退出）。
- **错误处理**：`api` 不可用 → 显示占位头像与「未登录」，不阻断页面。

#### `main_container` — 路由出口

- **职责**：内容区的路由出口，所有页面片段渲染到这里。
- **properties**：无（`id` 固定 `main_container`）。
- **children**：无（声明的 `children` 会被忽略）。
- **渲染**：`<RouteOutlet />`，按当前路由渲染对应页面。
- **错误处理**：路由无匹配 → 渲染 404 页。

---

## 6. 前端渲染与菜单推导

机制归属 `ping-ui`，业务模块只交 `ui/<page>/compose.json`；菜单数据**后端接口优先、本地声明降级**。

### 6.1 菜单数据源

页面加载后 `data/loader.ts` 请求 `GET /v1/ui/components`，取 `pages[0]`
（优先为 `application` 外壳树）驱动侧栏与路由；接口不可达、非 200 或响应不含 `type` 时，
静默降级到 `src/main/js/data/default-tree.ts` 并打 `[ping-ui]` 告警：

- 两份声明**结构同构**，服务端同构声明见 `src/main/resources/ui/app/compose.json`；
- 菜单路由为两段式 `properties.router`（如 `system/user` → `/system/user`），同级用 `properties.order` 升序排列；
- 侧栏菜单由 `deriveMenu` 从 `layout[left]` 推导，路由表由 `deriveRoutes` 从带 `router` 的菜单节点推导，
  两者都**不允许前端静态配置**。

### 6.2 分组与菜单项的区分

- `type: "nav_group"` 的节点是**分组**（服务端外壳树的写法）：侧栏渲染为可点击展开/折叠的分组标题（默认展开），侧栏整体折叠时只留一条分隔线；
- `type: "menu"` 的节点，若其 `children` 中仍含 `menu` 子节点，同样按**分组**渲染（本地降级树的写法，行为与 `nav_group` 一致）；
- 其余菜单节点是**叶子菜单项**：侧栏只渲染跳转链接（含激活高亮），其 `children` 里的页面内容（`dataset` 等）交给路由页渲染，不在侧栏出现。

### 6.3 侧栏菜单搜索

**数据流**：`AdminLayout` 持有搜索关键字 `query`，经 `router/derive.ts` 的纯函数
`filterMenus(menus, query)` 过滤后再传给 `Sidebar` 渲染；过滤结果同时驱动
`RendererContext.searching`，供菜单分组感知搜索态。

**过滤规则**：

- 关键字先 `trim()` 再转小写，与菜单项 `name`（缺省回落 `id`）做包含匹配；
- 只有命中关键字的菜单项会被保留，其所属分组连同祖先链一并保留；
- 未命中任何菜单项的分组**整体移除**（含分组标题），当前实现不按分组名匹配；
- 关键字为空（或全空白）时原样返回整棵菜单树，等价于「清空即恢复」。

**交互位置与状态**：

- 搜索框位于 logo 下方、菜单列表上方；侧栏整体收起时不渲染，`query` 保存在 `AdminLayout`，展开后关键字原样恢复；
- 过滤结果为空且处于搜索态时，菜单列表位置显示「无匹配菜单」空态提示；
- 搜索态下（`query` 非空白）所有命中分组强制展开且暂不可点击折叠，清空关键字后各分组回到此前各自的展开/折叠状态（分组展开态是 `nav_group` / `menu` 渲染器的本地状态，默认展开、不持久化）。

### 6.4 词汇演进（旧 → 新）

| 旧写法 | 现行写法 | 说明 |
|---|---|---|
| `table` | `dataset` | 数据类统一用逻辑数据表，不限定渲染形态 |
| `apis[]` 数组 | 单数 `api` | 装配期由后端把 `@ResourceReference:<name>` 替换为 `{method, path, request, response}`；未解析时保持引用字符串（前端用 `resolvedApi` 判断） |
| `name: "application"` | `type: "application"` | 类型标识必须用 `type`，`name` 是展示文案 |
| `containers` | `children` | 子节点键名统一为 `children` |
| 类型专有字段平铺在节点 | 收进 `properties` | 通用字段仅 5 个（§2.1） |

---

## 7. 接口契约

```
GET /v1/ui/components
Accept: application/json
```

> **待重构**：该接口的实现（`ComponentController`、`UIComponentRegistry`）已随
> `4fb05681` 移除，当前后端不提供此端点。前端 `loader.ts` 请求失败后自动降级到
> `data/default-tree.ts`，页面照常渲染。恢复时按下述契约实现。

| 项 | 约定 |
|---|---|
| 状态码 | `200 OK`（**任何情况下都不返回 404 / 500**） |
| `Content-Type` | `application/json` |
| 幂等性 | 注册表为启动期只读快照，连续多次请求响应内容逐字一致 |
| 数据来源 | 仅内存注册表 —— 不查数据库，不读描述文件 |

**200 OK**

```json
{
  "types": [
    { "type": "dataset", "name": "数据表", "description": "逻辑数据表，不限定渲染形态",
      "children": ["query", "column", "action", "row_action"] }
  ],
  "pages": [
    { "type": "application", "id": "ping.ui.app", "name": "Ping Admin", "children": [] }
  ]
}
```

| 字段 | 说明 |
|---|---|
| `types` | 全部内置 + 扩展的组件类型定义，顺序不保证；内置集合为 16 个常用类型 + 2 个壳体类型，共 18 |
| `pages` | 装配后的组件树；`pages[0]` 优先是 `application` 外壳树，无挂载目标的页面片段按序排在其后 |

**空注册表**：`200 OK` + `{"types":[],"pages":[]}`（不是 404/500）——
对应聚合索引缺失、或全部装配描述损坏的形态。

**本接口在 OpenAPI 中的描述**：控制器上标注

```java
@Tag(name = "Component", description = "抽象组件描述接口")
@Operation(summary = "获取抽象组件类型定义与组件树", description = "返回 ...")
```

`description` 必须说明 `types` / `pages` 两字段、空态与降级语义。
注意：`@Operation(summary)` **只用于文档展示，不作为 API 关联键** ——
装配描述引用接口一律使用 `@ResourceReference` 的 `name`（见 §2.3）。

**前端降级**：接口不可达、非 200、或响应不含 `type` 字段时，前端静默降级到
`data/default-tree.ts` 并打 `[ping-ui]` 告警，导航与切换能力保持完整。

**BREAKING**：旧版返回 `components/ui.json` 的原始文本，现已废弃；
`GET /v1/system/menus` 已移除，由本接口承载。

---

## 8. 完整示例

### 8.1 列表页：`ping-system/src/main/resources/ui/user-center/compose.json`

顶层是 `dataset` 且带 `properties.router` → 作为**页面片段**，装配期挂到外壳树
`router == "system/user"` 的 `menu` 节点下。

```json
{
  "type": "dataset",
  "id": "dataset.user-center",
  "name": "用户列表",
  "properties": {
    "router": "system/user",
    "order": 1,
    "description": "系统用户管理",
    "api": "@ResourceReference:获取用户列表",
    "row_key": "id",
    "selectable": true
  },
  "children": [
    {
      "type": "query",
      "id": "query.user-center",
      "properties": { "name": "查询条件", "collapse": true, "collapsed": false },
      "children": [
        { "type": "field", "id": "f.account", "name": "账号",
          "properties": { "name": "account", "widget": "input",
                          "placeholder": "请输入账号", "max_length": 64 } },
        { "type": "field", "id": "f.status", "name": "状态",
          "properties": { "name": "status", "widget": "select",
                          "options": [
                            { "value": "1", "label": "启用" },
                            { "value": "0", "label": "停用" }
                          ] } }
      ]
    },
    { "type": "column", "id": "col.account", "name": "账号",
      "properties": { "field": "account", "sortable": true } },
    { "type": "column", "id": "col.nickname", "name": "昵称",
      "properties": { "field": "nickname" } },
    { "type": "column", "id": "col.status", "name": "状态",
      "properties": { "field": "status", "sortable": true, "format": "enum",
                      "enum": { "1": "启用", "0": "停用" } } },
    { "type": "column", "id": "col.create_time", "name": "创建时间",
      "properties": { "field": "create_time", "sortable": true, "format": "date", "width": 180 } },
    { "type": "action", "id": "action.create", "name": "新建用户",
      "properties": { "variant": "primary", "placement": "toolbar" } },
    { "type": "row_action", "id": "ra.reset_password", "name": "重置密码",
      "properties": { "api": "@ResourceReference:重置用户密码",
                      "confirm": "确认重置该用户的登录密码？", "danger": true } },
    { "type": "row_action", "id": "ra.toggle_status", "name": "切换状态",
      "properties": { "api": "@ResourceReference:修改用户账户状态",
                      "confirm": "确认切换该用户的状态？" } }
  ]
}
```

装配后 `api` 被替换，前端据此得到：查询参数（`request.schema`）→ 渲染 `query`；
行数据（`response.schema`）→ 渲染列与行。

### 8.2 表单页：`ping-system/src/main/resources/ui/system-user-edit/compose.json`

顶层是 `form` 且带 `properties.router` → 同样按页面片段挂载。

```json
{
  "type": "form",
  "id": "form.system.user.edit",
  "name": "编辑用户",
  "properties": {
    "router": "system/user/edit",
    "api": "@ResourceReference:编辑用户",
    "mode": "edit",
    "cols": 2,
    "label_width": "96px"
  },
  "children": [
    { "type": "field", "id": "f.account", "name": "账号",
      "properties": { "name": "account", "widget": "input", "required": true,
                      "disabled": true, "max_length": 64 } },
    { "type": "field", "id": "f.nickname", "name": "昵称",
      "properties": { "name": "nickname", "widget": "input", "required": true,
                      "min_length": 2, "max_length": 32 } },
    { "type": "field", "id": "f.email", "name": "邮箱",
      "properties": { "name": "email", "widget": "input",
                      "pattern": "^[^@]+@[^@]+\\.[^@]+$" } },
    { "type": "field", "id": "f.status", "name": "状态",
      "properties": { "name": "status", "widget": "switch", "default": "1" } },
    { "type": "field", "id": "f.role", "name": "角色",
      "properties": { "name": "role_ids", "widget": "select", "multiple": true,
                      "required": true, "options": [] } },
    { "type": "field", "id": "f.remark", "name": "备注",
      "properties": { "name": "remark", "widget": "textarea", "col_span": 2, "max_length": 255 } },
    { "type": "form_action", "id": "fa.cancel", "name": "取消",
      "properties": { "kind": "cancel" } },
    { "type": "form_action", "id": "fa.submit", "name": "保存",
      "properties": { "kind": "submit", "loading": "保存中…" } }
  ]
}
```

### 8.3 外壳树与挂载结果（示意）

`ping-ui` 自带 `ui/app/compose.json`：

```json
{
  "type": "application",
  "id": "ping.ui.app",
  "name": "Ping Admin",
  "children": [
    { "type": "layout", "id": "layout.left",
      "properties": { "layout": "left" },
      "children": [
        { "type": "nav_group", "id": "nav.system", "name": "系统管理",
          "properties": { "order": 2 },
          "children": [
            { "type": "menu", "id": "menu.system.user", "name": "用户管理",
              "properties": { "router": "system/user", "order": 1 } }
          ] }
      ] },
    { "type": "layout", "id": "layout.right",
      "properties": { "layout": "right" },
      "children": [
        { "type": "layout", "id": "layout.top",
          "properties": { "layout": "top" },
          "children": [
            { "type": "user_info_panel", "id": "user_info_panel",
              "properties": { "api": "@ResourceReference:当前用户信息" } }
          ] },
        { "type": "layout", "id": "layout.bottom",
          "properties": { "layout": "bottom" },
          "children": [
            { "type": "main_container", "id": "main_container" }
          ] }
      ] }
  ]
}
```

装配后：8.1 的 `dataset` 挂进 `menu.system.user.children`，于是侧栏「用户管理」点击 →
内容区渲染该 `dataset`。

---

## 9. 校验规则与常见错误对照表

| # | 条件 | 发生时机 | 系统行为 | 观察方式 |
|---|---|---|---|---|
| 1 | 节点缺 `type` | 启动装配 | 该节点及子树跳过 | WARN 日志 |
| 2 | `compose.json` 非法 JSON | 启动装配 | 跳过该文件，其余照常 | WARN 日志，启动不失败 |
| 3 | `META-INF/ping/ui/components.json` 缺失 | 启动装配 | 注册表为空 | 接口 200 + 空集合 |
| 4 | `api` 引用未命中 / 目标未标注 | 启动装配 | `api` 保持引用原样 | WARN 日志，组件照常渲染 |
| 5 | `ping-api-doc/openapi.json` 缺失 | 启动装配 | 全部引用保持原样 | WARN 日志，启动不失败 |
| 6 | 描述放在 `META-INF/ping/` 之外 | 构建期 | 不被聚合、不被加载 | **不报错**，需自行检查路径 |
| 7 | 同 `id` 或同页面键重复 | 构建聚合 | 后者覆盖前者 | WARN 日志 |
| 8 | `properties.order` 缺省 | 装配 | 视为 `0` | — |
| 9 | `properties.router` 为 `https://` 外链 | 路由推导 | 新窗口打开，不进本地路由表 | — |
| 10 | 前端遇到未知 `type` | 渲染 | 由 `error_handler` 决定降级（见 `sdk-design.md` §6.2），**不丢弃 `children`** | 页面可见「未知组件：xxx」 |
| 11 | `dataset` 无 `column` | 渲染 | 提示「表格未声明 column」，不崩溃 | 页面可见提示框 |
| 12 | `field` 的 `widget` 未映射 | 渲染 | 占位输入框 + warn | 控件外观异常 |
| 13 | `select`/`radio` 缺 `options` | 渲染 | 空下拉 + warn | 下拉无项 |
| 14 | 接口不可达 / 响应无 `type` | 页面加载 | 降级本地 `default-tree.ts` | 控制台 `[ping-ui]` 告警 |
| 15 | 注册表为空 | 页面加载 | 降级本地声明，导航仍完整 | 页面正常显示 |

**定位顺序建议**：先看页面有没有告警（10–14）→ 再看启动日志（1–5、7）→ 最后检查构建产物（6）。

---

## 10. 新增一个组件类型的清单

1. 在 `ping-ui/src/main/resources/META-INF/ping/ui/types/<type>.json` 添加类型定义（snake_case 命名，结构见 §4.1）。
2. 在 `ping-ui/src/main/js/components/renderer/registry.tsx` 注册渲染器（`type` 键与 1 完全一致）。
3. 在 `ping-ui/src/main/js/schema/types.ts` 的 `properties` 类型中补充该类型的专有属性。
4. 若该类型需要数据，定义它如何消费 `properties.api` 的 `request.schema` / `response.schema`。
5. 补本文对应小节与 §4 总览表。
6. 运行 `ComponentTypeDefinitionTest` 与 `AggregatedIndexTest`，`cd ping-ui && npm run build`。

---

## 11. 代码迁移对照

节点模型变更后，以下代码位置需要同步迁移（当前仍是扁平模型）：

| 文件 | 现状 | 目标 |
|---|---|---|
| `src/main/js/schema/types.ts` | `ComponentNode` 扁平声明所有属性 | 收敛为 5 个通用字段 + `properties: Record<string, unknown>` |
| `src/main/js/components/renderer/registry.tsx` | 直接读 `node.router` / `node.order` | 改读 `node.properties?.router` 等 |
| `src/main/js/router/derive.ts` | `node.containers`、`node.router` | 改为 `node.children`、`node.properties?.router` |
| `src/main/js/data/default-tree.ts` | 扁平树 | 改写为 `children` + `properties` |
| `src/main/resources/ui/app/compose.json` | 扁平树（已移除，待重建） | 按新模型重建 |
| `ping-system/.../ui/user-center/compose.json` | 扁平树 | 改写为 `children` + `properties` |
