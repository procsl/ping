# Tasks

## 1. 测试与 SDK 模块骨架

- [ ] 1.1 在 `ping-ui/package.json` 新增 `vitest` devDependency 与 `test` 脚本（`vitest run`），不改动 `build` 脚本，并验证 `cd ping-ui && npm test` 能发现并跑过一个空测试文件
- [ ] 1.2 新建 `ping-ui/src/main/js/sdk/` 目录骨架与 `index.ts`，导出 `createPingUi` 占位，并验证 `npm run build`（`tsc --noEmit && vite build`）通过、`target/dist/renderer.js` 仍存在
- [ ] 1.3 新增 `ping-ui/vitest.config.ts`（复用 `@` 别名解析与 `src` include 规则），并验证 `npm test` 能解析 `@/sdk` 路径导入且不扫描到被删除的旧渲染器

## 2. 节点模型与消息总线

- [ ] 2.1 在 `sdk/` 内实现目标态 `ComponentNode`（`type` / `id` / `name` / `children` / `properties` 5 通用字段 + 索引签名），文件头注释标明对应 `abstract-component-schema.md` §2.1 与 §2.2 的两层 `properties` 区别，并用 `npx tsc --noEmit` 验证通过
- [ ] 2.2 实现 `MessageBus`（`on` / `off` / `emit`，`off` 不传回调时移除该事件全部回调），并用 vitest 验证「注册后收到通知并拿到数据」「注销后不再收到且其它回调不受影响」「同事件多回调全部被调用」「销毁后已移除订阅不再回调」四个场景
- [ ] 2.3 为 `ComponentNode` 与 `MessageBus` 补 JSDoc（`properties` 两层含义、`off` 的缺省语义），并验证 `npm run build` 通过

## 3. 处理器注册表

- [ ] 3.1 实现 `Map<namespace, Map<type, handler>>` 二维注册表与 `register` / `unregister` / `has` / `get`，`register` 内部统一包装为单一内部签名（调用时补 `messageBus`、`sdk`），并用 vitest 验证「首次注册成功」「重复注册被拒绝、原处理器不变、`console.warn` 被调用（`vi.spyOn` 断言）」「显式覆盖生效」「当前命名空间未命中时回退 `default` 命中」四个场景
- [ ] 3.2 为注册表补 JSDoc（`RegisterOptions` 两级键语义、冲突告警而非抛错的理由），并验证 `npm run build` 通过且注释引用的 `sdk-design.md` §2.3 路径存在

## 4. 生命周期与钩子

- [ ] 4.1 实现 `created → initialized → running → destroyed` 状态机、各阶段事件（`sdk:created` / `sdk:initialized` / `sdk:run` / `component:created` / `sdk:updated` / `sdk:destroyed`）与 `SdkHooks`，并用 vitest 验证「按创建、初始化、运行顺序触发不乱序」「处理器抛错时错误钩子收到封装错误且生命周期不中断」「销毁幂等（二次调用无副作用不抛错）」三个场景
- [ ] 4.2 实现 `destroy()` 清理注册表与消息订阅、`update(tree)` 等价重跑但触发 `sdk:updated`，并用 vitest 验证「销毁后 `get` 任意 type 均为 `undefined`」「销毁后 emit 不回调」「`update` 与 `run` 触发的事件名不同」三个场景
- [ ] 4.3 实现 `run` 在 `created` 态自动补 `init()` 并告警，为生命周期补 JSDoc 标明该行为（design D8），并用 vitest 验证「未初始化即运行不抛未捕获异常、`console.warn` 被调用、随后处于 running 态」这一场景

## 5. 运行入口与 create 派发

- [ ] 5.1 实现 `run(tree, messageBus)`：按 `tree.type` 命中则以 `(tree, node, messageBus, sdk)` 执行且无返回值，未命中走降级路径，并用 vitest 验证「命中处理器」「处理器无返回值」「未初始化即运行按 4.3 的既定策略处理」三个场景
- [ ] 5.2 实现 `create(tree, messageBus)` 返回 `CreatedHandler(node)`，创建时触发 `component:created`，并用 vitest 验证「预绑定后只传 node 即可执行」「执行拿到的是创建时绑定的那份 tree 与 messageBus」「创建阶段钩子收到该处理器」三个场景
- [ ] 5.3 在 `create` 返回的处理器内部实现错误包装：经它派发的节点异常被捕获为 `SdkError` 交 `error_handler`，**不冒泡进处理器的遍历循环**（design D3），并用 vitest 验证 spec 场景「派发的某子节点抛错后，同一处理器后续要派发的子节点照常执行」「整棵树不因单点失败中断」
- [ ] 5.4 用 vitest 验证「处理器只派发部分子节点时被跳过者不被处理且不报错」「处理器不派发任何子节点时运行照常继续」两个场景（spec：子节点派发由处理器驱动）
- [ ] 5.5 为 `run` / `create` 补 JSDoc，注明子节点派发由处理器驱动、错误包装在 `create` 路径内、SDK 不自动遍历树（design D3，理由见 `tabs` 不渲染未激活页签），并验证 `npm run build` 通过

## 6. 错误封装与降级

- [ ] 6.1 实现 `SdkError`（`nodeId` / `type` / `phase: run|update|destroy` / `message` / `stack` / `cause`）与捕获包装，并用 vitest 验证「处理器抛错被封装且字段齐全」「错误钩子被调用并收到该错误」两个场景
- [ ] 6.2 实现默认 `error_handler`（`init()` 时注册、输出错误并返回占位降级形态），且该保留名不可被当作可渲染 type 命中（design D6），并用 vitest 验证「未注册 type 走降级且不抛未捕获异常」「被降级节点的子节点仍被继续处理」「`register("error_handler", custom, { override: true })` 后由自定义实现接管」「`run` 不会把 `error_handler` 当作节点处理器执行」四个场景
- [ ] 6.3 为错误模块补 JSDoc，注明告警不抛错与仓库「降级 + 告警，永不失败」不变量的对应关系，并验证 `npm run build` 通过

## 7. 原生 demo

- [ ] 7.1 新建 `ping-ui/src/main/js/sdk/demo/`（`index.html` + `demo.ts` + `demo.css`），**不引入任何 UI 框架**，用原生 `document.createElement` 构建 DOM，注册自造 type 的处理器并跑通一份 `children` + `properties` 组件树，并验证源码中无 React / shadcn 导入
- [ ] 7.2 demo 中覆盖三条可见路径：正常命中渲染、未注册 type 降级、处理器抛错后同级继续，并把组件树与处理器定义抽成可被 vitest 导入的模块，验证测试能断言三条路径各自的输出
- [ ] 7.3 新增 `demo` npm 脚本（起 vite dev server 打开 demo 页面），并验证 `npm run demo` 启动后请求 demo 页面返回 200 且包含 demo 标记内容
- [ ] 7.4 验证 demo 不进 build 产物：`npm run build` 后 `target/dist` 下无 demo 文件，`renderer.js` 与 `chunks/` 结构与改动前一致

## 8. 删除旧渲染器与扁平模型

- [ ] 8.1 按 design D9 清单删除旧实现：`components/renderer/*`、`RendererRoot.tsx`、`main.tsx`、`layout/*`、`router/*`、`pages/*`、`data/loader.ts`、`data/default-tree.ts`、`schema/types.ts`、`mock/*`，并用 `grep -rn "containers\|schema/types" ping-ui/src/main/js` 验证无残留引用
- [ ] 8.2 改 `ping-ui/vite.config.ts` 的 lib 入口为 `src/main/js/sdk/index.ts`（产物名保持 `renderer.js`），并验证 `npm run build` 通过且 `target/dist/renderer.js` 导出 `createPingUi`
- [ ] 8.3 改写薄壳 `src/main/resources/META-INF/ping/entry/index.html` 为原生占位页（加载 `renderer.js`、显示「渲染器重建中」、附 demo 入口），并验证 `npm run build` 后 `target/classes/META-INF/resources/ui/index.html` 内容已更新、不含旧的渲染器挂载脚本
- [ ] 8.4 全量类型与构建检查：`cd ping-ui && npm run build` 通过，且 `tsc --noEmit` 对已删路径零报错

## 9. npm test 接入 Maven 测试周期

- [ ] 9.1 在 `ping-ui/pom.xml` 新增 `exec-maven-plugin` execution：`<phase>test</phase>` 跑 `npm test`，`<skip>${ping.npm.skip}</skip>` 复用既有开关，不新增 `npm install` 步骤（design D10），并验证 `./mvnw -pl ping-ui -Ph2 test` 日志中出现 `npm test` 执行记录
- [ ] 9.2 验证拦截有效：临时把某个测试改为失败，跑 `./mvnw -pl ping-ui -Ph2 test` 确认非零退出，随后恢复为通过
- [ ] 9.3 验证跳过开关：`./mvnw -pl ping-ui -Ph2 test -Dping.npm.skip=true` 时跳过 `npm test` 且构建不受影响
- [ ] 9.4 确认与既有 `prepare-package` 的 `npm run build` 不冲突：`./mvnw -pl ping-ui -Ph2 package -DskipTests` 仍正常产出 `target/dist/renderer.js` 并落位 `META-INF/resources/assets/renderer/`

## 10. 集成验收与文档同步

- [ ] 10.1 `cd ping-ui && npm test` 全绿
- [ ] 10.2 `./mvnw -Ph2 test` 全绿（此时已包含 `ping-ui` 的前端测试）
- [ ] 10.3 `npm run build` 与 `npm run demo` 均可成功，薄壳 `/ui/index.html` 可访问且显示占位页而非报错
- [ ] 10.4 确认删除范围与 proposal 清单一致：`git status` / `git diff --stat` 中删除项均在 proposal「删除代码」清单内，无计划外改动
- [ ] 10.5 更新 `ping-ui/doc/sdk-design.md`：状态行由「设计稿（待评审）」改为已实现，§9 待协商问题 A–G 标注落定结论，并验证 §7 公共 API 示例可直接运行
- [ ] 10.6 重写 `ping-ui/doc/abstract-component-schema.md` §11 迁移对照表：删除已完成迁移的行，补记「渲染器待重建、SDK 已就绪」现状，并验证表中描述与 `git diff` 实际改动一致

## Workflow follow-up

- 在项目的审查要求满足后归档该变更（`openspec archive`）。
- 验证归档结果：`openspec/specs/ui-component-sdk/spec.md` 已生成且 Purpose 非占位符；`openspec/specs/admin-navigation/spec.md` 中 4 条需求已按 delta 移除。
