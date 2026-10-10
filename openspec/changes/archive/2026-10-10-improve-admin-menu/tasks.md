# Tasks

## 1. 菜单假数据与占位内容

- [x] 1.1 扩充 `ping-ui/src/main/js/data/default-tree.ts` 的 `layout[left]` 菜单树为多个分组、多个菜单项（两段式 `router`、连续 `order`），并核对每个菜单项都能被 `deriveMenu`/`deriveRoutes` 正确收集（验证：`npm run build` 的 `tsc --noEmit` 通过，且新增路由都出现在推导结果中）
- [x] 1.2 新增占位页组件并在 `registry.tsx` 注册内置 `placeholder` 类型，为新增菜单项的 `containers` 声明占位节点；工作台菜单保持无子内容以继续落到 `Dashboard`（验证：`npm run build` 通过，开发服中逐个切换新菜单项，内容区显示各自占位内容而非同一个工作台）
- [x] 1.3 更新 `ping-ui/doc/design.md` 中与菜单数据/占位页相关的说明，使文档与新的假数据结构一致（验证：文档描述与 `default-tree.ts` 实际结构逐条对得上）

## 2. 菜单分组展开与折叠

- [x] 2.1 在 `registry.tsx` 的 `menu` 渲染器中为分组标题增加点击展开/折叠（默认展开，折叠时隐藏子项但保留标题；侧栏整体 `collapsed` 时仍只渲染分隔线）（验证：`npm run build` 通过；开发服中点击分组标题可反复展开/折叠，收起侧栏再展开后分组状态与此前一致）
- [x] 2.2 核对分组折叠不影响激活菜单项的高亮与多标签切换（验证：折叠包含当前路由的分组后，`TagsView` 标签切换与地址栏路由仍一致，无报错）

## 3. 侧栏菜单搜索

- [x] 3.1 在 `ping-ui/src/main/js/router/derive.ts` 实现纯函数 `filterMenus(menus, query)`：忽略关键字首尾空白与大小写、按菜单项名称匹配、保留命中项的祖先分组、空关键字返回原树（验证：`npm run build` 通过，并按 spec「命中过滤」「清空恢复」场景逐条手工核对）
- [x] 3.2 在 `Sidebar.tsx` 增加搜索输入（logo 下方、菜单列表上方），`collapsed` 时不渲染；`AdminLayout` 持有 `query` state 并把过滤结果传入 `Sidebar`（验证：`npm run build` 通过；开发服中输入关键字实时过滤，无结果显示空态提示，清空恢复完整菜单，收起侧栏无搜索框且恢复后关键字保留）
- [x] 3.3 搜索态下命中项的父分组强制展开（含此前被折叠的分组），未命中分组整体不展示（验证：先折叠某分组再输入其子项关键字，命中项可见；未命中分组与其标题均不出现）
- [x] 3.4 更新 `ping-ui/doc/design.md` 的侧栏/搜索相关章节（验证：文档与 `Sidebar.tsx`、`derive.ts` 的实际行为一致）

## 4. 集成验证

- [x] 4.1 `cd ping-ui && npm run build` 全量通过（`tsc --noEmit` + vite build），无类型错误与新增告警（验证：命令退出码为 0）
- [x] 4.2 `./mvnw -Ph2 test` 全量通过，含 `ping-ui` 的 `StaticAssetsTest`（验证：测试全部通过；如需 JDK 25 先 `export JAVA_HOME=$HOME/.jdks/graalvm-jdk-25`）
- [x] 4.3 按 `specs/admin-navigation/spec.md` 的全部场景逐条核对可观察行为（多菜单展示、菜单项切换与激活唯一性、刷新后激活态一致、分组展开/折叠、搜索过滤/空态/清空/命中展开/收起侧栏隐藏搜索框）（验证：每条场景都可复现且与 spec 一致）

## Workflow follow-up

- 实现完成后运行 `openspec-cn validate --change "improve-admin-menu"` 并按需审查产出物。
- 审查通过后再归档该变更（`/opsx-archive`）。
