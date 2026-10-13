# 菜单组件定义

> 模块：`ping-ui`（基础层）｜ 消费方：各业务模块
> 本文档定义**菜单（导航）相关抽象组件**的类型定义。抽象组件用 JSON 描述**功能骨架**，
> 与布局、样式分离，不绑定任何具体业务 —— 同一个 `menu` 在不同前端实现下可呈现为
> 侧栏链接、顶部导航或移动端抽屉。

---

## 1. 节点模型

每个组件节点**只保留 5 个字段**，组件特有字段一律放入 `properties` 对象：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | string | 建议 | 全树唯一 Key，用于 React key、去重、元素定位 |
| `type` | string | **是** | 组件类型，前端按此查渲染注册表 |
| `name` | string | 否 | 展示文案 / 标题 |
| `children` | array | 否 | 子节点数组 |
| `properties` | object | 否 | 组件特有字段集合（见 §3 各类型定义） |

**字段约束：**

- `children` 中的子节点**顺序不作为语义**，同级顺序一律由 `properties.order` 决定（缺省视为 `0`，升序）。
- `id` 在同一棵装配树内重复 → 后装配者覆盖，构建期记 WARN。
- `name` 缺失时前端回落 `id`，`id` 也缺失时回落 `type`。
- 组件可**任意嵌套**：容器套容器、分组套菜单，均合法。
- `properties` 中常见的跨类型字段：`order`（同级排序）、`router`（前端路由）、
  `description`（描述）、`icon`（图标标识）。各类型专有字段见 §3。

---

## 2. 组件类型清单

| 分类 | 类型 | 职责 | 允许的子节点 |
|---|---|---|---|
| 容器 | `application` | 应用根，全树入口 | `layout` `nav_group` `menu` |
| 容器 | `layout` | 布局方位槽 | 任意容器与内容类型 |
| 导航 | `nav_group` | 可展开/折叠的菜单分组 | `menu` |
| 导航 | `menu` | 菜单项 / 页面入口 | 分组形态：`menu`；叶子形态：页面内容节点 |

---

## 3. 类型定义

每个类型一份定义，位于 `ping-ui/src/main/resources/META-INF/ping/ui/types/<type>.json`：

```json
{
  "type": "nav_group",
  "name": "菜单分组",
  "category": "navigation",
  "description": "可展开与折叠的菜单分组标题，默认展开",
  "children": ["menu"],
  "properties": {
    "name": { "type": "string", "required": true, "description": "分组标题" }
  }
}
```

| 字段 | 含义 |
|---|---|
| `type` | 类型标识，与文件名一致，也与节点的 `type` 值一致 |
| `name` | 展示名 |
| `category` | 分类：`container` \| `navigation` |
| `description` | 语义说明 |
| `children` | 允许的子节点类型：枚举数组；`[]` 表示无子节点；`["*"]` 表示任意类型 |
| `properties` | 该类型的专有属性表，`required` 标出必填项 |

### 3.1 `application` — 应用根

- **职责**：整棵组件树的根，声明应用标识与标题。
- **properties**：`name`（应用名，显示在侧栏 logo 位）。
- **children**：`layout`、`nav_group`、`menu`。
- **渲染**：不直接渲染，向主框架透出 `name` 与子树。
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

### 3.2 `layout` — 布局方位槽

- **职责**：把内容切分为方位区域，是外壳结构的骨架。
- **properties**：`layout`（`left` \| `right` \| `top` \| `bottom` \| `center`，缺省按 `center`）。
- **children**：任意容器与内容类型。
- **渲染**：`left` → 侧栏；`top` → 顶栏；其余方位 → 内容区。
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

### 3.3 `nav_group` — 菜单分组

- **职责**：可展开/折叠的菜单分组标题。
- **properties**：`name`（**必填**，分组标题）、`icon`、`order`。
- **children**：`menu`。子节点不含 `menu` 时该分组不渲染。
- **渲染**：点击标题切换展开/折叠，**默认展开**；侧栏整体收起时只留一条分隔线。
- **错误处理**：`properties.name` 缺失回落 `id`；`id` 也缺失 → 该分组跳过并 WARN。

```json
{
  "type": "nav_group",
  "name": "菜单分组",
  "category": "navigation",
  "description": "可展开与折叠的菜单分组标题，默认展开",
  "children": ["menu"],
  "properties": {
    "name": { "type": "string", "required": true, "description": "分组标题" },
    "icon": { "type": "string", "required": false, "description": "图标标识" },
    "order": { "type": "integer", "required": false, "description": "同级排序，缺省 0" }
  }
}
```

### 3.4 `menu` — 菜单项

- **职责**：一个可跳转的功能入口，同时是路由与页面内容的挂载点。
- **properties**：`router`（叶子必填）、`icon`、`order`、`external`（布尔，强制外链）。
- **children**：
  - **分组形态**（`children` 内含 `menu`）→ 渲染为分组标题，不生成路由；
  - **叶子形态**（含 `properties.router`）→ `children` 是该页面的内容节点。
- **渲染**：侧栏只渲染跳转链接 + 激活态高亮，**同一时刻至多一个菜单项激活**，
  激活态与地址栏路由保持一致（刷新后不丢）；侧栏折叠时收敛为单字符图标按钮。
- **错误处理**：`properties.router` 为 `https://` 外链 → 新窗口打开，不进本地路由表；
  `properties.router` 缺失且无子 `menu` → 侧栏不渲染该项，WARN。

```json
{
  "type": "menu",
  "name": "菜单项",
  "category": "navigation",
  "description": "可跳转的功能入口，同时是路由与页面内容的挂载点",
  "children": ["*"],
  "properties": {
    "router": { "type": "string", "required": false, "description": "前端路由，两段式相对路径；https:// 开头视为外链" },
    "icon": { "type": "string", "required": false, "description": "图标标识" },
    "order": { "type": "integer", "required": false, "description": "同级排序，缺省 0" },
    "external": { "type": "boolean", "required": false, "description": "强制外链，新开窗口" }
  }
}
```

---

## 4. 新增一个组件类型的清单

1. 在 `ping-ui/src/main/resources/META-INF/ping/ui/types/<type>.json` 添加类型定义（snake_case 命名）。
2. 在 `ping-ui/src/main/js/components/renderer/registry.tsx` 注册渲染器（`type` 键与 1 完全一致）。
3. 在 `ping-ui/src/main/js/schema/types.ts` 如需新增节点属性则补充到 `properties` 类型（`type: string` 无需改）。
4. 补本文件对应小节与 §2 总览表。
5. 运行 `npm run build` 验证前端构建。
