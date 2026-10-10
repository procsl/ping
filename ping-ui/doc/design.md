# 抽象组件设计

#### 组件设计

> 抽象组件使用`json schema`描述组件的功能骨架, 以实现布局与样式分离

1. 任意组件需要具有以下2个属性

    ```text
    String  id;     // 唯一Key,用来唯一标识一个组件
    String  name;   // 组件名称，用来标识一种组件类型
    ```

2. 组件具有事件属性,可以监听指定事件并注册

#### 组件定义

> 以下是使用`json schema`定义的组件,可以自由扩充其属性与子节点

1. 应用组件(Application)

   ```text
   Array container;   // 容器
   ```

2. 菜单组件(Menu)

    ```text
    Array  children; // 菜单子节点,菜单子节点的顺序为数组中的顺序
    ```

3. 表单输入组件(input)
    ```text

    ```

#### 后台管理框架的菜单数据与占位页

> 机制归属 `ping-ui`，业务模块只交 `ui/<page>/compose.json`；菜单数据**后端接口优先、本地声明降级**。

1. 菜单数据源

    页面加载后 `data/loader.ts` 请求 `GET /v1/ui/components`，取 `pages[0]`
    （优先为 `application` 外壳树）驱动侧栏与路由；接口不可达、非 200 或响应不含 `type` 时，
    静默降级到 `src/main/js/data/default-tree.ts` 并打 `[ping-ui]` 告警：

    - 两份声明**结构同构**，服务端同构声明见 `src/main/resources/ui/app/compose.json`；
    - 左侧菜单位于 `layout[left]` 节点下，当前为 **6 个分组、15 个菜单项**；
    - 菜单路由为两段式 `router`（如 `system/user` → `/system/user`），同级用 `order` 升序排列；
    - 侧栏菜单由 `deriveMenu` 从 `layout[left]` 推导，路由表由 `deriveRoutes` 从带 `router` 的菜单节点推导，两者都不允许前端静态配置。

2. 分组与菜单项的区分

    - `type: "nav_group"` 的节点是**分组**（服务端外壳树的写法）：侧栏渲染为可点击展开/折叠的分组标题（默认展开），侧栏整体折叠时只留一条分隔线；
    - `type: "menu"` 的节点，若其 `containers` 中仍含 `menu` 子节点，同样按**分组**渲染（本地降级树的写法，行为与 `nav_group` 一致）；
    - 其余菜单节点是**叶子菜单项**：侧栏只渲染跳转链接（含激活高亮），其 `containers` 里的页面内容（`dataset` 等）交给路由页渲染，不在侧栏出现。

3. 占位页（`placeholder`）

    - 叶子菜单项的 `containers` 可声明 `{ "type": "placeholder", "id": "...", "name": "<菜单名>" }`，由内置 `placeholder` 渲染器展示「菜单名 + 页面建设中」；
    - 没有任何子内容的菜单项（如工作台）仍落到框架默认工作台 `Dashboard`；
    - 两者刻意区分：保证切换不同菜单项时内容区呈现可辨识的差异，后续接入真实页面时替换该节点即可。

#### 组件词汇（`table` → `dataset` 等）

> 完整规范与逐类型说明见 `ping-ui/doc/abstract-component-schema.md`，此处只记本模块的实现要点。

- 数据类统一用 `dataset`（逻辑数据表，**不限定前端渲染形态**）：
  - `query` 是该表的输入参数容器，与 `dataset` 为**兄弟**关系，子节点是 `field`；
  - `column` 描述展示列，`field` 取值自出参 schema，`sortable` 表示该列可排序（点击表头本地排序）；
  - `action` 是容器级动作（工具栏），`row_action` 是**行内**动作，二者不混用；
- 表单类用 `form` / `field` / `form_action`，`field.widget` 决定控件，未实现的 widget 降级占位并告警；
- 旧词汇 `table` 与 `apis[]` 数组已废弃：节点改用单数 `api`，装配期由后端把
  `@ResourceReference:<name>` 替换为 `{method, path, request, response}`；未解析时保持引用字符串（见 `resolvedApi`）。

#### 侧栏菜单搜索

1. 数据流

    `AdminLayout` 持有搜索关键字 `query`，经 `router/derive.ts` 的纯函数
    `filterMenus(menus, query)` 过滤后再传给 `Sidebar` 渲染；过滤结果同时驱动
    `RendererContext.searching`，供菜单分组感知搜索态。

2. 过滤规则

    - 关键字先 `trim()` 再转小写，与菜单项名称（`name`，缺省回落 `id`）做包含匹配；
    - 只有命中关键字的菜单项会被保留，其所属分组连同祖先链一并保留；
    - 未命中任何菜单项的分组**整体移除**（含分组标题），当前实现不按分组名匹配；
    - 关键字为空（或全空白）时原样返回整棵菜单树，等价于「清空即恢复」。

3. 交互位置与状态

    - 搜索框位于 logo 下方、菜单列表上方；侧栏整体收起时不渲染，`query` 保存在 `AdminLayout`，展开后关键字原样恢复；
    - 过滤结果为空且处于搜索态时，菜单列表位置显示「无匹配菜单」空态提示；
    - 搜索态下（`query` 非空白）所有命中分组强制展开且暂不可点击折叠，清空关键字后各分组回到此前各自的展开/折叠状态（分组展开态是 `nav_group` / `menu` 渲染器的本地状态，默认展开、不持久化）。
