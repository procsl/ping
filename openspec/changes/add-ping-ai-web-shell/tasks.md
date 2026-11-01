# Tasks

## 1. 启动形态与自动配置
来源：原 7.3–7.4。依赖：`add-ping-ai-agent-core` 的入口与 box 契约。

- [ ] 1.1 实现 `--web` 切换到 `SERVLET` 形态并监听，验证测试：指定后正常监听
- [ ] 1.2 验证未指定 `--web` 时不监听（回归核心变更的行为），验证测试：无监听端口
- [ ] 1.3 通过 `AutoConfiguration.imports` 登记自动配置，验证测试：测试应用引入本模块依赖后能力可用
- [ ] 1.4 以 `@ConditionalOnWebApplication(type = SERVLET)` 限定生效条件，验证测试：非 web 宿主下不注册 web 组件
- [ ] 1.5 提供关闭自动配置的开关（`@ConditionalOnProperty`），验证测试：关闭后 web 组件不注册

## 2. HTTP 与 WebSocket
来源：原 7.5–7.6。依赖：阶段 1。

- [ ] 2.1 实现 HTTP 端点（遵循 `/v1/...` 与 SNAKE_CASE），验证测试：请求返回预期响应
- [ ] 2.2 实现 WebSocket 消息通道（建立连接），验证测试：能建立连接
- [ ] 2.3 实现 WebSocket 双向收发（box 下行、命令上行），验证测试：box 四字段完整、对象 id 连续、命令可被处理
- [ ] 2.4 实现慢客户端处理（超阈值断开、重连后按 `pull(from_id)` 恢复），验证测试：慢消费被断开且重连后状态一致

## 3. 语义一致与验收
来源：原 7.7–7.8。依赖：全部。

- [ ] 3.1 编写对照测试：同一组 box 在 web 与终端交互形态下产生相同渲染状态，验证：断言对象归组、结束判定与缺口恢复一致
- [ ] 3.2 编写双形态说明文档（启动方式、端点、box 字段、上行命令、关闭开关），验证：文档描述与 1.1、1.3、2.1 的测试行为一致
- [ ] 3.3 运行全量回归 `JAVA_HOME=$HOME/.jdks/graalvm-jdk-25 ./mvnw -Ph2 test`，验证：构建通过且核心变更的测试不受影响

## Workflow follow-up

- 在 `add-ping-ai-agent-core` 归档后推进本变更。
- 端点路径与 box 承载方式确定后，回写 `design.md` 的 Open Questions。
