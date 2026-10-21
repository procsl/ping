# Tasks

## 1. 测试基座恢复

- [ ] 1.1 恢复 vitest：`ping-ui/package.json` 添加 `vitest` devDependency 与 `test` script（`vitest run`），新建独立 `vitest.config.ts`（`environment: node`），安装后验证 `npm test` 可运行（0 用例通过）
- [ ] 1.2 `ping-ui/pom.xml` 的 exec-maven-plugin 增加 test 阶段 `npm run test` 执行（复用 `ping.npm.skip`），验证 `./mvnw -Ph2 -pl ping-ui test` 触发 npm test 且 BUILD SUCCESS
- [ ] 1.3 生成真实树 fixture：启动 ping-system 导出 `GET /v1/ui/components` 响应存为测试 fixture（真实编译产物形态，锁定树形状），验证 fixture 文件存在且含 app > page > function 结构

## 2. 类型与错误契约

- [ ] 2.1 `client/types.ts`：功能树节点、内联 operation（method/path/parameters/requestBody/responses）、TreeSource/ComposeClientOptions（treeSource 与 fetch 双注入）/FetchInit 类型定义，验证 `npm run build`（tsc --noEmit）通过
- [ ] 2.2 `client/error.ts`：`ApiError`（status/operationId/message/fieldErrors，网络失败 status=0，message 自响应体提取回落状态文本），验证单测：从 MessageVO 形态提取、从 ParameterErrorVO 形态透出 fieldErrors、缺 message 回落

## 3. 树源与索引

- [ ] 3.1 `client/source.ts`：默认树源（fetch `/v1/ui/components`，非 2xx/网络失败 reject 带原因）+ 输入嗅探（树特征优先、`paths` 对象判裸 openapi 文档），验证单测：树输入、裸文档输入、非法输入拒绝
- [ ] 3.2 `client/client.ts`：`ComposeClient.create()` 静态工厂 + treeSource 注入 + operationId 索引 + `tree` 只读暴露（与索引同源、消费方不可改内部状态），验证单测：默认/注入树源创建、创建失败即 reject、已知键定位、未知键 ApiError 消息含键名、树暴露只读

## 4. 参数自动绑定

- [ ] 4.1 `client/bind.ts`：params 按 `parameters[].in` 分拣——path 模板替换、query 平铺（数组重复键）、body JSON 序列化（仅声明 requestBody 时发送）、缺 path 参数拒绝不发请求、未声明参数忽略，验证单测逐场景覆盖（对应 spec「参数按内联声明自动绑定」5 场景）

## 5. 洋葱管道

- [ ] 5.1 `client/pipeline.ts`：`Handler` 接口、ctx（operation/请求描述）、链执行器（注册顺序=执行顺序、短路=不调 next、请求/响应双向改写），验证单测：顺序语义、下行改写抵达最终请求、短路不发 HTTP、管道兑现值恒为数据

## 6. 调用组装与集成

- [ ] 6.1 `client/client.ts` 组装完整调用链（绑定 → 洋葱链 → fetch → 2xx 解析兑现 / 失败转 ApiError reject），验证单测：2xx JSON 兑现纯数据、400 携带 message 与 fieldErrors、网络失败 status=0、AbortSignal 透传取消
- [ ] 6.2 零魔法语义验证：401 在未注册鉴权 Handler 时与普通失败无差别（ApiError status=401）、snake_case 字段名原文兑现，验证单测两场景
- [ ] 6.3 Node 环境验证：注入树源与 mock HTTP 层在 Node 下完整跑通创建/调用/管道/错误契约（vitest 默认 Node 环境即天然覆盖），验证 `npm test` 全绿

## 7. 导出、清理与全量验证

- [ ] 7.1 `sdk/index.ts` 导出 ComposeClient（entry 与 renderer.js 产物名不变），验证 `npm run build` 通过且产物含 client 导出
- [ ] 7.2 删除过时文档 `ping-ui/doc/abstract-component-schema.md` 与 `ping-ui/doc/sdk-design.md`，并清理薄壳页 `META-INF/ping/entry/index.html` 中失效的 `/ui/demo/index.html` 演示链接，验证仓库 grep 无残留
- [ ] 7.3 单元与集成验证：`./mvnw -Ph2 test`（含 ping-ui npm test）BUILD SUCCESS；`openspec-cn validate add-compose-client --strict` 通过
- [ ] 7.4 真启动端到端验收：`spring-boot:run` 启动后浏览器 console 执行 `await ComposeClient.create()` 并实调 `findUsers`（200 兑现数据），非法参数调用 reject `ApiError`（status/fieldErrors 可见），验证行为与 spec 一致

## Workflow follow-up

- 实现完成后按用户审查节奏归档该变更（`/opsx:archive`）
