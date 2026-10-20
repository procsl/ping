# Design

## Context

上一版抽象 UI 框架（`add-ping-ui-web-sdk`）与本变更早期草案采用自定义组件类型系统 + `$ref`/UUID 字段引用：compose.json 显式列出 `input`/`output` 子节点，每个节点用 `openapi://<operationId>/<request|response>/<path>` 字段路径引用 operation 字段，编译器再把字段 `$ref` 物化为 UUID 挂回 schema。

核心痛点是**表达力天花板**：自造的字段路径 DSL（`openapi://.../request/query/name` + `items` 段语义）是比 JSON Schema 更弱的子集。遇到嵌套对象、array of arrays、oneOf、discriminator、multipart 等 OpenAPI 已支持的形态，就要回头扩语法。同时字段级声明量大、新增接口成本高。

本次彻底重构为「功能树 + openapi 内联」模型：compose.json 只声明 `app > pages > function` 与 `operationId` 引用，字段细节全部从 openapi.json 内联进 function，前端据自包含的 function 调用与渲染。表达力天花板直接等于 OpenAPI 全集。

## Goals / Non-Goals

**Goals:**
- compose.json 退化为功能树：统一节点树（`type: app > page > function`），开发者只声明 page/function 节点 + `operationId` 引用，零字段声明
- `function` = openapi operation 的**自包含镜像**（method/path/parameters/requestBody/responses 全内联，无 `$ref` 残留）
- 表达力天花板 = OpenAPI 全集（JSON Schema），不自造字段 DSL
- 编译器 `Transformer` 开闭原则：使用意图（裁剪/默认/可排序/分页模式）未来可扩展，往 `function.properties` 塞字段
- 后端 `GET /v1/ui/components` 返回编译产物
- 前端据产物生成菜单 + page + 调用；布局前端自定，轻量 client 后续自实现

**Non-Goals:**
- 不实现前端渲染器与轻量 client（后续，前端据产物自行调用）
- 不做使用意图（字段裁剪、默认值、可排序、分页模式）——留 `Transformer` 扩展点
- 不做 platform 过滤
- 不涉及权限/角色过滤（无权限体系，不做钩子）
- 不支持运行时动态加载组件描述

## Decisions

### D1: 统一节点模型，type 字段标识节点类型

**决策**：组件树由**统一节点**构成，每个节点 `{ type, id, properties, children }`，靠 `type` 字段区分三种类型，不为 app/page/function 各建类：

```
{ type: "app",      id, properties, children: [page...] }        // 根, properties 可扩展 version/source
  { type: "page",   id, properties, children: [function...] }    // id 全局唯一
    { type: "function", id, properties }                          // id 在 page 内唯一, properties=openapi 描述
```

源 compose.json 为节点数组（`type` 标识，page/function），编译合并为 `type: "app"` 根。domain 用**一个**组合类型（type + id + properties + children）承载，不同类型只是 `type` 值不同——组合优于继承。

**理由**：旧的五层（function/view/form/input/output）把"功能入口"和"读/写"和"字段"分开，粒度过细、字段声明量大。新模型 page 承担导航、function 承担接口，字段细节全来自 openapi，开发者零字段声明。

**备选方案（被否决）**：保留 `view`/`form`/`input`/`output` 分层 + `$ref` 字段声明 → 被拒绝，自造字段 DSL 表达力受限、声明量大（本次重构的起因）；domain 建 `App`/`Page`/`Function` 三个类 → 被拒绝，违背组合优于继承，扩展需改类结构，改用统一节点 + `type` 分派。

### D2: function = openapi operation 的自包含镜像

**决策**：`function` 就是 openapi 中的接口描述。compose.json 源里开发者只写 `{ id, operationId }`（引用），编译期 ping-ui 按 `operationId` 找到 openapi operation，把 method/path/parameters/requestBody/responses **内联**进 `function.properties`，展开所有 `$ref`，无残留。

`function.properties` 是 openapi operation 的镜像结构（保留 `parameters`/`requestBody`/`responses` 的 openapi 形态，前端据标准语义解析：`parameters[].in` 分组 path/query、`requestBody` 为表单、`responses` 为展示）。字段角色来自 openapi 的天然锚点，无需 compose 重新分组。

**理由**：直接复用 OpenAPI 的表达，不重新组织字段结构，编译器只需"复制 operation + 内联 schema"。前端据 openapi 标准语义渲染控件（query 参数 → 查询条件、body → 表单、response → 展示），轻量 client 可据标准结构实现。

**备选方案（被否决）**：function.properties 重新分组为 `request.{path,query,body}` + `response` → 被拒绝，那是又自造一层结构，违背"直接使用 openapi 表达"。

### D3: openapi schema 全内联，接受冗余

**决策**：function.properties 中所有 `$ref`（`#/components/schemas/...`）展开为内联 schema。同一 schema（如 `UserRecord`）被多个 function 引用时**重复内联，接受冗余**，换取 function 完全自包含、前端零 `$ref` resolve、零完整 openapi 依赖。

**理由**：function 自包含后，前端拿到单个 function 就能渲染 + 调用，不需要回查 openapi 全文或维护 components 池。冗余 schema 经 gzip 压缩率高，web 环境可接受。

**备选方案（被否决）**：
- app/page 级放一份 components，function 用 `$ref` 指回 → 被拒绝，前端又要做 `$ref` resolve，违背自包含目标
- 前端另拉完整 openapi.json + 用 swagger-client → 被拒绝，swagger-client 太重，不适合 web 环境（用户明确决策）

### D4: operationId 引用语义——可多引用，冲突启动报错

**决策**：`operationId` 是 function 引用 openapi operation 的唯一锚点。**同一 operationId 可被多个 page 的多个 function 引用**（openapi operation 是唯一源，function 是它的使用实例）。openapi.json 内 operationId 冲突（不同 path/method 同 operationId）→ **启动阶段直接报错**，不兜底（不追加类型+方法名）。

**理由**：openapi 规范要求 operationId 全局唯一，冲突本身就是后端定义错误，应尽早暴露。多实例引用同一接口是正常场景（如"用户列表"页和"仪表盘"页都用 `findUsers`）。

**备选方案（被否决）**：冲突时追加类型+方法名生成唯一名 → 被拒绝，掩盖了后端命名错误，且生成名不稳定。

### D5: function.id 路由规则

**决策**：function 的全局标识 / 前端路由 = `page.id + "/" + function.id`。`function.id` 只需在 page 内唯一（开发者写短名，如 `list`），全局唯一性靠 page 前缀。

**理由**：与前端路由天然对齐（`user/list`），开发者不必手写带前缀的长 id。与旧 design 的全局唯一路径式 function id（`system/user`）等价，但分工清晰——page 给分组前缀，function 给短名。

### D6: 编译器 Transformer 开闭原则（使用意图扩展点）

**决策**：编译器是一组可扩展阶段（`Transformer` 接口），本次只有一个核心阶段「openapi 内联」。未来加使用意图（裁剪/默认/可排序/分页模式）= 新增一个 `Transformer` 实现，往 `function.properties` 添加字段，**不修改已有编译器代码**。

`Transformer` 是纯变换（`List<Component> process(List<Component>, CompileContext)`），支持过滤/增删/改字段；`Component` 不可变（改字段=重建节点）；`ComposeCompiler` 链式传递 `nodes = t.process(nodes, ctx)`。`assemble`（合并 app 根）为固定方法——app 根结构是不变量，不做扩展点。

**理由**：使用意图本次明确不做，但代码架构要为未来留口子。`properties` 是天然的扩展载体（加字段不破坏结构）。这继承旧 design 的开闭原则。

**备注**：使用意图用标准机制表达（未来考虑 OpenAPI `x-` 扩展或 function.properties 字段），不自造字段 DSL——延续"表达力=OpenAPI 全集"的初衷。

### D7: 编译输入 openapi-<module>.json

**决策**：编译输入为打包期导出的 OpenAPI 文档：`META-INF/ping/openapi-<module>.json`（如 `openapi-ping-distribute.json`），运行时扫描合并进 operation 索引。存在 compose.json 却无任何 openapi 文件 → 启动报错。多文件 operationId 重复 → 启动报错。

**理由**：与 AGENTS"构建期产物统一落 `META-INF/ping/`、运行时只读该固定路径"对齐。本次只有 distribute 导出（全量 API），分模块导出留后续提案。

### D8: 编译校验与启动失败

**决策**：编译在启动阶段完成（Spring Bean 初始化），失败即上下文启动失败、进程非 0 退出。校验对象大幅简化（compose 侧 + openapi 引用侧）：

| 情况 | 处理 |
|---|---|
| `page.id` 重复 | 报错，启动失败 |
| `function.id` 在 page 内重复 | 报错，启动失败 |
| `operationId` 在 openapi 中找不到（引用断链） | 报错，启动失败 |
| openapi 内 operationId 冲突 | 报错，启动失败 |
| 未知属性（未来使用意图扩展的字段） | 忽略 + debug 日志 |
| 未知类型（page/function 之外） | 忽略 + debug 日志 |

全有或全无：任何一个 compose.json 编译失败，整个应用启动失败。报错信息带具体引用路径与声明来源。

**理由**：断链与 id 冲突是结构性错误，应启动即暴露；未知属性/类型走忽略是版本升级兼容（延续上一版结论）。不再校验"空 schema 被引用"——字段不声明、schema 全来自 openapi，openapi schema 质量是后端职责，不归 compose 编译器管。

### D9: DDD 包结构

**决策**：包结构沿用上一版思路，职责域按新模型调整：

```
cn.procsl.ping.boot.ui
├── domain/       # 统一节点组合类型（type + id + properties + children）
├── compiler/     # ComposeCompiler（主编排）+ Transformer（阶段接口）
│                  + CompileContext（上下文）+ OpenApiInliner（openapi 内联阶段）
├── repository/   # ComposeRepository（扫 compose.json）+ OpenApiRepository（扫 openapi-*.json）
└── exception/    # ComposeException（基类，编译错误）+ ComposeParseException（文件解析错误）
```

**理由**：组合优于继承，一个统一节点组合类型靠 `type` 分派，不为 app/page/function 各建类；Compiler 按阶段扩展（D6）；Repository 只做数据访问；Exception 精简到 2 个。

### D10: 后端端点

**决策**：`GET /v1/ui/components` 返回编译产物（app 根，pages 包含自包含 function）。编译结果组装到内存只读快照，启动编译一次，运行期不更新直到重启。

### D11: 打包导出模式跳过编译

**决策**：`openapi.export=true`（打包期生成 openapi 的进程）跳过组件编译与端点注册，否则首次构建时输入文档尚未生成会自锁。除此模式外无任何豁免路径。

### D12: 前端只做减法（client 后续）

**决策**：本次不做前端渲染器与轻量 client。前端改动仅限移除 SDK 机制（registry/message-bus/error-handler/handlers），保留 `createPingUi` 占位 entry，薄壳不变，`npm run build` 通过。轻量 client（据 function.properties 拼 fetch）后续自实现。

### D13: 端点鉴权不作特殊处理

**决策**：`GET /v1/ui/components` 跟随现有全局登录拦截器，不加 `@PermitAll`。权限策略不在本次范围。

### D14: platform 过滤不做

**决策**：本次彻底不做，spec 与 tasks 中该条目已移除。

### D15: query 参数形态纠正（让 openapi 描述准确）

**决策**：后端给 query POJO 参数（如 `UserQuery query`）加 `@ParameterObject`（springdoc 注解）或自定义组合注解统一标注，使 openapi 把该 POJO 展开为多个独立 query 参数，与 Spring MVC 实际的平铺绑定一致。

**理由**：springdoc 默认把无注解 POJO 参数描述为单个 `name=query schema=UserQuery` 的参数（`?query={...}`），而 Spring MVC 实际平铺绑定（`?name=x&state=y`）——描述与实际 HTTP 不符，前端照 openapi 发请求会失配（收不到参数）。`@ParameterObject` 从源头纠正，前端零特殊处理，编译器无需归一化逻辑。

**备选方案（被否决）**：
- 编译器归一化（识别对象参数展开）→ 被拒绝，为 Spring 一个生成失真加编译逻辑，治标不治本
- 前端硬扛（自己知道要平铺）→ 被拒绝，把失真留给前端反复踩

## 跨阶段约束（Invariants）

1. **表达力 = OpenAPI 全集**：任何字段/参数/响应的表达都来自 openapi，不自造字段 DSL。将来加使用意图走 `function.properties` 标准字段 + `Transformer` 扩展，不引入新引用语法。
2. **function 自包含**：产物中 function 不含任何 `$ref` 残留，前端拿到单个 function 即可渲染 + 调用。
3. **开闭原则**：新增编译阶段只实现 `Transformer`，不改已有代码。
4. **启动失败**：结构性错误启动即失败，全有或全无，报错带引用路径。
5. **导出豁免唯一**：`openapi.export=true` 是唯一跳过编译的场景。
6. **响应只保留 2xx**：`function.properties.responses` 只序列化 2xx（200/201/204），非 2xx 不序列化；错误处理归轻量 client（据 HTTP 状态码自判）。
7. **菜单扁平**：各 function 以路由标识（`page.id/function.id`）平铺为一级菜单，`page.id` 仅作路由前缀，不做分组标签。菜单显示名由前端自定，compose 不声明显示名。
8. **分页/排序前端识别**：编译器不物化分页容器/排序 input，前端据固定 schema 字段（request `offset`/`size`/`sort`、response `content`/`total`）识别。
9. **本期无字段裁剪**：v1 显示 operation 全部 response 字段，裁剪留 `Transformer` 扩展。
10. **无状态**：编译器组件不依赖 Spring，直接 new 创建、用完即释放，每次编译新实例（AGENTS DDD 无状态原则）。

## 阶段设计文档索引

| 阶段 | 文档 | 状态 |
|---|---|---|
| 1. 编译器核心 | design/phase-1-compiler-core.md | 已细化（含 compose/产物 JSON 示例） |
| 2. 后端聚合接口 | design/phase-2-backend-api.md | 已细化 |
| 3. 旧代码清理 | design/phase-3-cleanup.md | 已细化 |

> 阶段文档承载实现级细节（字段表、compose/产物 JSON 示例、边界与降级契约、逐任务实现细节、验证方式）；架构决策在本文件 D 条目，跨阶段约束在下方 Invariants。

## Risks / Trade-offs

- **[风险]** compose.json 格式彻底变更，不做迁移——存量旧文件删除，业务侧按新规范声明
- **[风险]** function.properties 内联冗余 schema → bundle 变大；靠 gzip 缓解，web 环境可接受
- **[风险]** openapi schema 质量直接决定前端渲染能力（空 schema、缺 required 等）→ 归后端职责，compose 编译器不校验，需后端保证 openapi 完整
- **[权衡]** 不做使用意图 → 本期前端无法表达字段裁剪/默认值/排序；留 Transformer 扩展点，未来补充
- **[风险]** 未来第一个新 compose.json 落地时，ping-system 的 `@SpringBootTest` 会因缺 openapi 文件启动失败 → 与散装问题同案，归后续提案
- **[风险]** 轻量 client 自实现（不复用 swagger-client）→ 参数序列化、multipart、auth 等需自己处理，工作量在前端后续

## Migration Plan

1. 重写 `ping-ui` 编译器为「openapi 内联」+ `Transformer` 骨架，DDD 包结构（D9）
2. 实现后端 `GET /v1/ui/components`（D10）
3. 删除存量旧格式文件 `ping-system/src/main/resources/ui/user-center/compose.json`
4. 前端只做减法（D12）
5. 移除旧 compiler/exception 代码（$ref/UUID/pagination/排序/默认值那套全删）

**不做回滚兜底**：不保留旧 compose.json 解析逻辑、不设 feature flag——旧格式本就无法加载成功，回滚对象不存在。

## Open Questions

- ⏸️ 使用意图（字段裁剪/默认值/可排序/分页模式）的具体表达机制（未来 `Transformer` 阶段 + `function.properties` 字段，或 OpenAPI `x-` 扩展）——本次不做，仅留扩展点
- ⏸️ 轻量 client 的错误处理（非 2xx 不序列化 schema，client 据 HTTP 状态码自判错误与错误体）——后续 client 实现
- ⏸️ 分模块 openapi 导出的互斥规则（全量/部分文件并存会 operationId 冲突）——后续提案
- ⏸️ 散装启动问题（有 compose.json、无 openapi 文件 → 启动失败）——后续提案
- ⏸️ 测试影响（未来第一个新 compose.json 落地时 ping-system 测试启动失败）——与散装问题同案
