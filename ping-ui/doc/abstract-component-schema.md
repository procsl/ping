# 抽象组件描述规范

> 模块：`ping-ui`（基础层）｜ 消费方：各业务模块
> 权威行为规范见 `openspec/specs/ui-component-schema/spec.md`，技术方案见
> `openspec/changes/add-ui-component-schema/design.md`。本文件是面向**组件编写者**的参考手册，
> 只解释「怎么写」，不重复「系统为什么这样做」。

---

## 1. 概览

抽象组件用 JSON 描述**功能骨架**，与布局、样式分离。四段链路：

```
业务模块                 构建期                 启动期                前端
ui/<page>/compose.json ──▶ 聚合索引 ──▶ 内存注册表 ──▶ GET /v1/ui/components ──▶ 按 type 查渲染器
                                                    + ping-api-doc/openapi.json
```

**谁写什么：**

| 角色 | 交付物 |
|---|---|
| 业务模块 | `src/main/resources/ui/<page>/compose.json` |
| `ping-ui` | 18 个组件类型定义（`META-INF/ping/ui/types/*.json`）、应用外壳树、前端渲染器 |
| 构建期 | `META-INF/ping/ui/components.json`（单一聚合索引，运行时唯一读取点） |
| 打包期 | `ping-api-doc/openapi.json`（由 `ping-distribute` 导出） |

**三条不变量：**

1. 描述只表达**语义**，不限制前端渲染形态 —— 同一个 `dataset` 可被渲染成表格、卡片或看板。
2. 运行时**只读固定路径**，禁止 `classpath*:` 通配扫描；放在约定路径之外的描述不生效（也不报错）。
3. 任何描述问题（缺字段、非法 JSON、引用未命中）都**降级 + 告警，启动永不失败**。

**命名约定**：所有类型名与 JSON 属性名一律 **snake_case**（AGENTS.md：JSON SNAKE_CASE），
与既有的 `user_info_panel` / `main_container` 一致 —— 用 `nav_group`、`row_action`、`form_action`、
`date_picker`，不用连字符。

---

## 2. 节点模型与字段字典

### 2.1 通用字段（所有节点可用）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `type` | string | **是** | 组件类型，前端按此查渲染注册表。缺失时该节点及子树不进入注册表 |
| `id` | string | 建议 | 全树唯一 Key，用于 React key、去重、元素定位 |
| `name` | string | 否 | 展示文案 / 标题；菜单、列、按钮的显示名 |
| `order` | number | 否 | 同级排序，缺省视为 `0`，按升序 |
| `router` | string | 否 | 前端路由。两段式相对路径 `system/user` → `/system/user`；`https://` 开头视为外链，不进本地路由表 |
| `description` | string | 否 | 面向用户或维护者的描述 |
| `api` | string \| object | 否 | 后端接口引用，见 §2.3 |
| `layout` | string | 否 | 布局方位：`left` \| `right` \| `top` \| `bottom` \| `center`，缺省按 `center` |
| `containers` | array | 否 | 子节点数组 |

除上表外，每个类型还有自己的**专有属性**（见 §5），以及类型定义中声明的其它属性。

### 2.2 字段约束

- `containers` 中的子节点**顺序不作为语义**，同级顺序一律由 `order` 决定。
- `id` 在同一棵装配树内重复 → 后装配者覆盖，构建期记 WARN。
- `name` 缺失时前端回落 `id`，`id` 也缺失时回落 `type`。
- 组件可**任意嵌套**：容器套容器、动作套数据表、表单套页签，均合法；嵌套不改变子节点自身的类型与数据契约。

### 2.3 `api` 字段：引用与解析

**写的时候**是引用标记：

```json
"api": "@ResourceReference:获取用户列表"
```

**装配后**被就地替换为完整描述（依据打包期导出的 `ping-api-doc/openapi.json`）：

```json
"api": {
  "method": "GET",
  "path": "/v1/system/users",
  "request":  { "content_type": "application/json", "schema": { "...": "入参结构" } },
  "response": { "status": "200", "content_type": "application/json", "schema": { "...": "出参结构" } }
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
| 其它任意类型 | **页面片段** | 用顶层节点的 `router` 作挂载键，挂到外壳树中 `router` 相同的 `menu` 节点的 `containers` 末尾；**无匹配时不报错**，原样进入 `pages`，前端按其 `router` 自行路由 |

> 所以业务模块**通常只写页面片段**，不需要重复整棵应用树。

### 3.3 与本地降级的关系

前端 `data/default-tree.ts` 是接口不可用时的降级声明。改 `compose.json` 后若要让降级数据同步，
需一并改它；两者结构同构。

---

## 4. 组件类型总览

内置 **16 个常用后台管理抽象组件类型** + **2 个框架壳体类型**，共 18 个。

| 分类 | 类型 | 职责 | 允许的子节点 |
|---|---|---|---|
| 容器 | `application` | 应用根，全树入口 | `layout` `nav_group` `menu` `tabs` `breadcrumb` `user_info_panel` `main_container` |
| 容器 | `layout` | 布局方位槽 | 任意容器与内容类型 |
| 导航 | `nav_group` | 可展开/折叠的菜单分组 | `menu` |
| 导航 | `menu` | 菜单项 / 页面入口 | 分组形态：`menu`；叶子形态：页面内容节点 |
| 导航 | `tabs` | 页签容器 | 任意组件（页签标题取 `name`） |
| 导航 | `breadcrumb` | 面包屑 | 无（用 `items` 属性） |
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
- `query` 与 `dataset` 是**兄弟**关系，不是内嵌属性 —— 查询参数是数据表的输入，独立声明。

### 4.1 类型定义文件的结构

每个类型一份定义，位于 `ping-ui/src/main/resources/META-INF/ping/ui/types/<type>.json`：

```json
{
  "type": "dataset",
  "name": "数据表",
  "category": "data",
  "description": "逻辑数据表：描述取数接口、输入参数与展示列；只表达语义，不限定前端渲染形态",
  "children": ["query", "column", "action", "row_action"],
  "properties": [
    { "name": "row_key", "type": "string", "required": false, "description": "行唯一键，默认 id" }
  ]
}
```

| 字段 | 含义 |
|---|---|
| `type` | 类型标识，与文件名一致，也与节点的 `type` 值一致 |
| `name` | 展示名 |
| `category` | 分类：`container` \| `navigation` \| `data` \| `form` \| `action` \| `state` \| `shell` |
| `description` | 语义说明 |
| `children` | 允许的子节点类型：枚举数组；`[]` 表示无子节点；`["*"]` 表示任意类型 |
| `properties` | 该类型的专有属性表，`required` 标出必填项 |

`category` 为 `shell` 的两个类型（`user_info_panel`、`main_container`）是框架壳体，不计入 16 个常用类型。


---

## 5. 分类详解

每个类型的格式统一为：**职责 / 属性 / 子节点 / 渲染 / 错误处理**。

### 5.1 应用与布局

#### `application` — 应用根

- **职责**：整棵组件树的根，声明应用标识与标题。
- **属性**：`id`（如 `ping.ui.app`）、`name`（应用名，显示在侧栏 logo 位）。
- **子节点**：`layout`、`nav_group`、`menu`、`tabs`、`breadcrumb`、`user_info_panel`、`main_container`。
- **渲染**：不直接渲染，向 `AdminLayout` 透出 `name` 与子树。
- **错误处理**：缺失 `application` 根 → 前端降级本地声明并 WARN。

#### `layout` — 布局方位槽

- **职责**：把内容切分为方位区域，是外壳结构的骨架。
- **属性**：`layout`（`left` \| `right` \| `top` \| `bottom` \| `center`，缺省按 `center`）、`name`、`order`。
- **子节点**：任意容器与内容类型。
- **渲染**：`left` → 侧栏；`top` → 顶栏；装 `main_container` 的方位 → 内容区。
- **错误处理**：`layout` 缺失 → 视为 `center` 并 WARN；`layout[left]` 缺失 → 侧栏为空但不崩溃。

### 5.2 导航

#### `nav_group` — 菜单分组

- **职责**：可展开/折叠的菜单分组标题（对应侧栏「系统管理」这类小标题）。
- **属性**：`id`、`name`（**必填**，分组标题）、`order`、`icon`。
- **子节点**：`menu`。子节点不含 `menu` 时该分组不渲染。
- **渲染**：点击标题切换展开/折叠，**默认展开**；侧栏整体收起时只留一条分隔线；
  搜索态下强制展开且不可点击折叠。
- **错误处理**：`name` 缺失回落 `id`；`id` 也缺失 → 该分组跳过并 WARN。

#### `menu` — 菜单项

- **职责**：一个可跳转的功能入口，同时是路由与页面内容的挂载点。
- **属性**：`id`、`name`、`order`、`icon`、`router`（叶子必填）、`external`（布尔，强制外链）。
- **子节点**：
  - **分组形态**（`containers` 内含 `menu`）→ 渲染为分组标题，不生成路由；
  - **叶子形态**（含 `router`）→ `containers` 是该页面的内容（`dataset` / `form` / `placeholder` …）。
- **渲染**：侧栏只渲染跳转链接 + 激活态高亮，**同一时刻至多一个菜单项激活**，
  激活态与地址栏路由保持一致（刷新后不丢）；侧栏折叠时收敛为单字符图标按钮。
- **错误处理**：`router` 为 `https://` 外链 → 新窗口打开，不进本地路由表；
  `router` 缺失且无子 `menu` → 侧栏不渲染该项，WARN。

#### `tabs` — 页签容器

- **职责**：把多个平级内容放进同一页签区，用于详情页的多面板。
- **属性**：`id`、`name`、`active`（激活页签的 `id`，缺省取第一个子节点）。
- **子节点**：任意组件，**页签标题取子节点的 `name`**。
- **渲染**：页签条 + 内容面板；未激活页签默认不渲染其内容（避免无谓的数据请求）。
- **错误处理**：子节点缺 `name` → 标题回落 `id`，再缺则用 `type`；`active` 指向不存在的 `id` → 回落第一个。

#### `breadcrumb` — 面包屑

- **职责**：页面级位置指示，不承载内容。
- **属性**：`id`、`name`、`items`（数组，每项 `{ "name": ..., "router": ... }`，末项无 `router`）。
- **子节点**：无（`containers` 会被忽略）。
- **渲染**：分隔的路径链，末项高亮；有 `router` 的项可点击。
- **错误处理**：`items` 缺失或非数组 → 不渲染面包屑，WARN；数组为空同理。

### 5.3 数据

#### `dataset` — 逻辑数据表

- **职责**：描述一张**逻辑表**：它从哪取数（`api`）、有哪些列（`column`）、怎么查（`query`）、
  有哪些动作（`action` / `row_action`）。**不指定渲染形态。**
- **属性**：

  | 属性 | 类型 | 说明 |
  |---|---|---|
  | `api` | object | 查询接口（装配后为完整描述）。`request.schema` = 查询参数，`response.schema` = 行数据 |
  | `title` | string | 表标题，缺省回落 `name` |
  | `row_key` | string | 行唯一键，默认 `id` |
  | `selectable` | boolean | 是否允许多选行 |

- **子节点**：`query`（至多一个）、`column`（≥1）、`action`（0..n，工具栏）、`row_action`（0..n，行内）。
- **渲染**：可为表格、卡片、看板、图表等。**同一 `dataset` 描述在不同前端实现下可呈现完全不同，
  描述与接口返回不因此改变。**
- **错误处理**：
  - 无 `column` → 渲染「表格未声明 column：{id}」的提示框，不崩溃；
  - 无 `api` → 用本地演示行渲染并标注「未绑定」；
  - `response.schema` 与列 `field` 对不上 → 该列留空并 WARN（不抛错）。

#### `query` — 数据表的输入参数容器

- **职责**：声明该数据表**可被什么条件查询**，即表的「入参」。
- **属性**：`id`、`name`（表单区标题）、`collapse`（是否可折叠）、`collapsed`（初始折叠）。
- **子节点**：`field`（0..n，每个对应一个查询条件）。
- **渲染**：搜索/筛选区，与 `dataset` 头部同行或独立一行；
  提交时把 `field` 值拼进查询参数调 `dataset.api`。
- **错误处理**：无 `field` → 不渲染筛选区（而非渲染空表单）；
  `field.name` 与 `dataset.api.request.schema` 无对应 → 该条件提交后被后端忽略，前端 WARN。

#### `column` — 展示列

- **职责**：描述数据表的一列：取哪个字段、怎么显示、有哪些列级能力。
- **属性**：

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

- **子节点**：无。
- **渲染**：由 `dataset` 的渲染器决定呈现为表头单元格、卡片字段或看板列。
- **错误处理**：`field` 缺失 → 该列整体不渲染并 WARN；
  `sortable: true` 但 `dataset.api` 未声明排序参数 → 前端禁用排序入口并 WARN。

#### `row_action` — 行内动作

- **职责**：数据表**每一行**上的操作（编辑、删除、重置密码、审核…）。
- **属性**：`id`、`name`（按钮文案）、`api`（动作接口，`request.schema` 至少含行主键）、
  `confirm`（二次确认文案，空则不确认）、`danger`（危险动作标红）、
  `disabled_when`（依行字段禁用的提示表达式）。
- **子节点**：无。
- **渲染**：行尾动作列，或收进「更多」下拉；接口出参成功后按契约刷新当前行/整表。
- **错误处理**：缺 `api` → 按钮禁用并 WARN；`confirm` 存在时必须先确认再调接口。

### 5.4 表单

#### `form` — 表单容器

- **职责**：一组字段 + 一组动作的收集与提交单元。
- **属性**：

  | 属性 | 类型 | 说明 |
  |---|---|---|
  | `api` | object | 提交接口；`request.schema` = 表单字段集合，`response.schema` = 提交结果 |
  | `mode` | `create` \| `edit` | 创建 / 编辑；`edit` 时前端按行主键回填 |
  | `cols` | `1` \| `2` \| `3` | 字段栅格列数，默认 `1` |
  | `label_width` | string | 标签宽度 |

- **子节点**：`field`（0..n）、`form_action`（0..n）。
- **渲染**：普通表单、抽屉、弹窗、分步表单均由前端选择；描述不变。
- **错误处理**：`mode: edit` 而 `api` 缺 `response` → 不回填，按空表单渲染并 WARN；
  必填校验不通过时不发请求。

#### `field` — 表单字段

- **职责**：一个可输入/可选择的数据项，是入参 schema 到控件的映射单元。
- **属性**：

  | 属性 | 类型 | 说明 |
  |---|---|---|
  | `name` | string | **字段名**，对应 `api.request.schema` / `response.schema` 中的键（必填） |
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

- **子节点**：无。
- **渲染**：`widget` → `ping-ui/components/ui/*` 的具体控件；
  在 `dataset`/`query` 上下文中同一 `field` 渲染为查询条件控件。
- **错误处理**：
  - `name` 缺失 → 该字段跳过并 WARN（不渲染无名字段）；
  - `widget` 未实现 / 未映射 → 渲染占位输入框 + `console.warn`，不阻断整表；
  - `select`/`radio` 缺 `options` → 渲染为空下拉并 WARN。

#### `form_action` — 表单动作

- **职责**：表单提交区的按钮。
- **属性**：`id`、`name`（文案）、`kind`（`submit` \| `reset` \| `cancel`）、
  `api`（`kind: submit` 时通常复用 `form.api`，单独覆盖时才需要）、`danger`、`loading`（提交中态文案）。
- **子节点**：无。
- **渲染**：表单底部右侧；`submit` 触发校验→提交，`reset` 清空，`cancel` 返回。
- **错误处理**：一个 `form` 内至多一个 `kind: submit` 作为默认主按钮，多余的记 WARN；
  无 `submit` 时表单不可提交并提示。

### 5.5 动作与状态

#### `action` — 通用动作

- **职责**：容器级别的动作（工具栏按钮、页头按钮、卡片操作）。**不在行内**（行内用 `row_action`）。
- **属性**：`id`、`name`、`api`、`confirm`、`danger`、
  `variant`（`primary` \| `outline` \| `ghost`）、`placement`（`toolbar` \| `header`，默认 `toolbar`）。
- **子节点**：无。
- **渲染**：所属容器头部按钮区。
- **错误处理**：缺 `api` → 按钮可渲染但点击时提示「未绑定接口」并 WARN。

#### `placeholder` — 页面占位

- **职责**：明确告知「这个页面在建设中」，保证菜单切换有可辨识的内容差异。
- **属性**：`id`、`name`（页面名）。
- **子节点**：无。
- **渲染**：居中显示 `{name} · 页面建设中`。
- **错误处理**：`name` 缺失回落 `id`，再缺用「该页面」。

#### `empty` — 空态

- **职责**：数据为空时的提示与引导。
- **属性**：`id`、`name`（主文案）、`description`（次文案）、`action`（引导按钮文案）。
- **子节点**：无。
- **渲染**：图标 + 主文案 + 次文案 + 可选引导按钮。
- **错误处理**：无 `name` 时用「暂无数据」。

### 5.6 框架壳体类型

> 这两个不属于「常用后台管理抽象组件」的 16 个，由 `ping-ui` 外壳树使用，业务模块一般不写。

#### `user_info_panel` — 用户信息面板

- **职责**：顶栏右侧的当前登录用户展示与操作。
- **属性**：`id`、`api`（获取用户信息的接口）。
- **子节点**：无。
- **渲染**：头像 + 用户名 + 下拉（个人中心 / 退出）。
- **错误处理**：`api` 不可用 → 显示占位头像与「未登录」，不阻断页面。

#### `main_container` — 路由出口

- **职责**：内容区的路由出口，所有页面片段渲染到这里。
- **属性**：`id`（固定 `main_container`）。
- **子节点**：无（声明的 `containers` 会被忽略）。
- **渲染**：`<RouteOutlet />`，按当前路由渲染对应页面。
- **错误处理**：路由无匹配 → 渲染 404 页。

---

## 6. 接口契约

```
GET /v1/ui/components
Accept: application/json
```

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
    { "type": "application", "id": "ping.ui.app", "name": "Ping Admin", "containers": [ ] }
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

## 7. 完整示例

### 7.1 列表页：`ping-system/src/main/resources/ui/system-user/compose.json`

顶层是 `dataset` 且带 `router` → 作为**页面片段**，装配期挂到外壳树
`router == "system/user"` 的 `menu` 节点下。

```json
{
  "type": "dataset",
  "id": "table.system.user",
  "name": "用户列表",
  "router": "system/user",
  "order": 1,
  "description": "系统用户管理",
  "api": "@ResourceReference:获取用户列表",
  "row_key": "id",
  "selectable": true,
  "containers": [
    {
      "type": "query",
      "id": "query.system.user",
      "name": "查询条件",
      "collapse": true,
      "collapsed": false,
      "containers": [
        { "type": "field", "id": "f.account", "name": "account", "label": "账号",
          "widget": "input", "placeholder": "请输入账号", "max_length": 64 },
        { "type": "field", "id": "f.status", "name": "status", "label": "状态",
          "widget": "select", "options": [
            { "value": "1", "label": "启用" },
            { "value": "0", "label": "停用" }
          ] }
      ]
    },
    { "type": "column", "id": "col.account", "field": "account", "name": "账号", "sortable": true },
    { "type": "column", "id": "col.nickname", "field": "nickname", "name": "昵称" },
    { "type": "column", "id": "col.status", "field": "status", "name": "状态",
      "sortable": true, "format": "enum",
      "enum": { "1": "启用", "0": "停用" } },
    { "type": "column", "id": "col.create_time", "field": "create_time", "name": "创建时间",
      "sortable": true, "format": "date", "width": 180 },
    { "type": "action", "id": "action.create", "name": "新建用户",
      "api": "@ResourceReference:创建配置项", "variant": "primary", "placement": "toolbar" },
    { "type": "row_action", "id": "ra.edit", "name": "编辑",
      "api": "@ResourceReference:编辑配置项" },
    { "type": "row_action", "id": "ra.reset", "name": "重置密码",
      "api": "@ResourceReference:删除配置项",
      "confirm": "确认重置该用户的登录密码？", "danger": true }
  ]
}
```

装配后 `api` 被替换，前端据此得到：查询参数（`request.schema`）→ 渲染 `query`；
行数据（`response.schema`）→ 渲染列与行。

### 7.2 表单页：`ping-system/src/main/resources/ui/system-user-edit/compose.json`

顶层是 `form` 且带 `router` → 同样按页面片段挂载。

```json
{
  "type": "form",
  "id": "form.system.user.edit",
  "name": "编辑用户",
  "router": "system/user/edit",
  "api": "@ResourceReference:编辑配置项",
  "mode": "edit",
  "cols": 2,
  "label_width": "96px",
  "containers": [
    { "type": "field", "id": "f.account", "name": "account", "label": "账号",
      "widget": "input", "required": true, "disabled": true, "max_length": 64 },
    { "type": "field", "id": "f.nickname", "name": "nickname", "label": "昵称",
      "widget": "input", "required": true, "min_length": 2, "max_length": 32 },
    { "type": "field", "id": "f.email", "name": "email", "label": "邮箱",
      "widget": "input", "pattern": "^[^@]+@[^@]+\\.[^@]+$" },
    { "type": "field", "id": "f.status", "name": "status", "label": "状态",
      "widget": "switch", "default": "1" },
    { "type": "field", "id": "f.role", "name": "role_ids", "label": "角色",
      "widget": "select", "multiple": true, "required": true, "options": [] },
    { "type": "field", "id": "f.remark", "name": "remark", "label": "备注",
      "widget": "textarea", "col_span": 2, "max_length": 255 },
    { "type": "form_action", "id": "fa.cancel", "name": "取消", "kind": "cancel" },
    { "type": "form_action", "id": "fa.submit", "name": "保存", "kind": "submit",
      "loading": "保存中…" }
  ]
}
```

### 7.3 外壳树与挂载结果（示意）

`ping-ui` 自带 `ui/app/compose.json`：

```json
{
  "type": "application", "id": "ping.ui.app", "name": "Ping Admin",
  "containers": [
    { "type": "layout", "layout": "left", "id": "layout.left", "containers": [
      { "type": "nav_group", "id": "nav.system", "name": "系统管理", "order": 2, "containers": [
        { "type": "menu", "id": "menu.system.user", "name": "用户管理",
          "router": "system/user", "order": 1 }
      ]}
    ]},
    { "type": "layout", "layout": "right", "id": "layout.right", "containers": [
      { "type": "layout", "layout": "top", "id": "layout.top", "containers": [
        { "type": "user_info_panel", "id": "user_info_panel",
          "api": "@ResourceReference:用户菜单列表" }
      ]},
      { "type": "layout", "layout": "bottom", "id": "layout.bottom", "containers": [
        { "type": "main_container", "id": "main_container" }
      ]}
    ]}
  ]
}
```

装配后：7.1 的 `dataset` 挂进 `menu.system.user.containers`，于是侧栏「用户管理」点击 →
内容区渲染该 `dataset`。

---

## 8. 校验规则与常见错误对照表

| # | 条件 | 发生时机 | 系统行为 | 观察方式 |
|---|---|---|---|---|
| 1 | 节点缺 `type` | 启动装配 | 该节点及子树跳过 | WARN 日志 |
| 2 | `compose.json` 非法 JSON | 启动装配 | 跳过该文件，其余照常 | WARN 日志，启动不失败 |
| 3 | `META-INF/ping/ui/components.json` 缺失 | 启动装配 | 注册表为空 | 接口 200 + 空集合 |
| 4 | `api` 引用未命中 / 目标未标注 | 启动装配 | `api` 保持引用原样 | WARN 日志，组件照常渲染 |
| 5 | `ping-api-doc/openapi.json` 缺失 | 启动装配 | 全部引用保持原样 | WARN 日志，启动不失败 |
| 6 | 描述放在 `META-INF/ping/` 之外 | 构建期 | 不被聚合、不被加载 | **不报错**，需自行检查路径 |
| 7 | 同 `id` 或同页面键重复 | 构建聚合 | 后者覆盖前者 | WARN 日志 |
| 8 | `order` 缺省 | 装配 | 视为 `0` | — |
| 9 | `router` 为 `https://` 外链 | 路由推导 | 新窗口打开，不进本地路由表 | — |
| 10 | 前端遇到未知 `type` | 渲染 | 占位节点 + `console.warn`，**不丢弃 `containers`** | 页面可见「未知组件：xxx」 |
| 11 | `dataset` 无 `column` | 渲染 | 提示「表格未声明 column」，不崩溃 | 页面可见提示框 |
| 12 | `field.widget` 未映射 | 渲染 | 占位输入框 + warn | 控件外观异常 |
| 13 | `select`/`radio` 缺 `options` | 渲染 | 空下拉 + warn | 下拉无项 |
| 14 | 接口不可达 / 响应无 `type` | 页面加载 | 降级本地 `default-tree.ts` | 控制台 `[ping-ui]` 告警 |
| 15 | 注册表为空 | 页面加载 | 降级本地声明，导航仍完整 | 页面正常显示 |

**定位顺序建议**：先看页面有没有告警（10–14）→ 再看启动日志（1–5、7）→ 最后检查构建产物（6）。

---

## 9. 新增一个组件类型的清单

1. 在 `ping-ui/src/main/resources/META-INF/ping/ui/types/<type>.json` 添加类型定义（snake_case 命名）。
2. 在 `ping-ui/src/main/js/components/renderer/registry.tsx` 注册渲染器（`type` 键与 1 完全一致）。
3. 在 `ping-ui/src/main/js/schema/types.ts` 如需新增节点属性则补充（`type: string` 无需改）。
4. 补本文件对应小节与 §4 总览表。
5. 运行 `ComponentTypeDefinitionTest` 与 `AggregatedIndexTest`，`cd ping-ui && npm run build`。
6. 若该类型需要数据，定义它如何消费 `api.request.schema` / `api.response.schema`。
