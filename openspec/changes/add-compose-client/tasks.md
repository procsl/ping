# Tasks

## 1. 测试基座恢复

- [x] 1.1 恢复 vitest：`ping-ui/package.json` 添加 `vitest` devDependency 与 `test` script（`vitest run`），新建独立 `vitest.config.ts`（`environment: node`），安装后验证 `npm test` 可运行（0 用例通过）
- [x] 1.2 `ping-ui/pom.xml` 的 exec-maven-plugin 增加 test 阶段 `npm run test` 执行（复用 `ping.npm.skip`），验证 `./mvnw -Ph2 -pl ping-ui test` 触发 npm test 且 BUILD SUCCESS
- [x] 1.3 生成真实树 fixture：启动 ping-system 导出 `GET /v1/ui/components` 响应存为测试 fixture（真实编译产物形态，锁定树形状），验证 fixture 文件存在且含 app > page > function 结构

## 2. 框架层：管道与契约

- [x] 2.1 `client/types.ts`：`Context` 协议接口（字段分组注释：fetch 入口 / bootstrap 链 / 调用链，标明谁填谁读）+ OperationDescriptor/ComponentNode/ApiRequest/ResponseMeta/OperationIndex/TreeSource/ComposeClientOptions/FetchInit 结构类型 + `requireOperation`/`requireRequest` helper（缺字段 throw 明确错误），验证 `npm run build`（tsc --noEmit）通过
- [x] 2.2 `client/pipeline.ts`：`Handler`/`Next` 函数类型 + `compose` 洋葱执行器（数组顺序执行、不调 next 即短路、return 值/异常逐层冒泡、next() 重复调用 throw、链尽头无人 return 兑现 undefined），验证单测覆盖 Test Requirements「框架」行全部语义（含 ≥5 层深链）
- [x] 2.3 `client/error.ts`：`ApiError`（status/operationId/message/fieldErrors，网络失败 status=0，message 自响应体提取回落状态文本），验证单测：从 MessageVO 形态提取、从 ParameterErrorVO 形态透出 fieldErrors、缺 message 回落

## 3. 架子环（handlers/）

- [x] 3.1 `client/handlers/source.ts` + `indexer.ts`：source 环取树（treeSource 注入，默认 fetch `/v1/ui/components`，非 2xx/网络失败带原因 reject）；indexer 环嗅探（树特征优先、`paths` 对象判裸 openapi 文档）+ app > page > function 抽取建 operationId 索引，验证单测：树输入、裸文档输入、非法输入拒绝、索引键完整
- [x] 3.2 `client/handlers/locate.ts`：查表定位 → `ctx.operation`；未知键 throw ApiError（消息含键名），验证单测：已知键定位、未知键拒绝
- [x] 3.3 `client/handlers/bind.ts`：params 按 `parameters[].in` 分拣纯函数——path 模板替换、query 平铺（数组重复键）、body JSON 序列化（仅声明 requestBody 时发送）、缺 path 参数拒绝不发请求、未声明参数忽略，验证单测逐场景覆盖（对应 spec「参数按内联声明自动绑定」5 场景）
- [x] 3.4 `client/handlers/http.ts`：终点环（不调 next）——2xx JSON `return` 解析数据 + `ctx.response` 填 {status, headers}、空体（204/零长度）兑现 undefined、非 2xx throw ApiError（message 提取回落、fieldErrors 透出）、网络失败 status=0、AbortError 原样透传，验证单测 5 场景

## 4. 门面组装（client.ts）

- [x] 4.1 `client/client.ts`：`ComposeClient`——create 静态工厂（跑 bootstrap 链 [source, indexer]，失败 reject 无半初始化，`tree` 只读暴露与索引同源）+ fetch（seed ctx = {operationId, params, signal} → 组链 [locate, bind, ...业务hooks, http] → 兑现冒泡值/ reject）+ `addHandler`（插槽固定 bind 之后 http 之前，多次调用顺序 = 执行顺序），验证单测：默认/注入树源创建、插槽位置、注册顺序语义、tree 只读
- [x] 4.2 集成语义验证：真实 fixture 全链路（Node）、2xx 兑现纯数据、400 携带 message 与 fieldErrors、网络失败 status=0、AbortSignal 取消原样、401 未注册鉴权环与普通失败无差别、snake_case 字段原文兑现、业务环请求改写抵达最终请求、短路不发 HTTP，验证单测逐场景
- [x] 4.3 Node 环境验证：注入树源与 mock HTTP 层在 Node 下完整跑通创建/调用/管道/错误契约（vitest 默认 Node 环境即天然覆盖），验证 `npm test` 全绿

## 5. 导出与清理

- [x] 5.1 `sdk/index.ts` 导出 ComposeClient 及业务所需面（`Handler`/`Context` 类型、`compose`、`require*` helper、`ApiError`），验证 `npm run build` 通过且产物含 client 导出
- [x] 5.2 删除过时文档 `ping-ui/doc/abstract-component-schema.md` 与 `ping-ui/doc/sdk-design.md`，并清理薄壳页 `META-INF/ping/entry/index.html` 中失效的 `/ui/demo/index.html` 演示链接，验证仓库 grep 无残留

## 6. 全量验证

- [x] 6.1 单元与集成验证：`./mvnw -Ph2 test`（含 ping-ui npm test，spec 26 场景对应测试全绿）BUILD SUCCESS；`openspec-cn validate add-compose-client --strict` 通过
- [x] 6.2 真启动端到端验收：`spring-boot:run` 启动后浏览器 console 执行 `await ComposeClient.create()` 并实调 `findUsers`（200 兑现数据），非法参数调用 reject `ApiError`（status/fieldErrors 可见），验证行为与 spec 一致

## Workflow follow-up

- 实现完成后按用户审查节奏归档该变更（`/opsx:archive`）
