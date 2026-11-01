# 阶段 4 设计：非交互式命令行

对应 `tasks.md` 阶段 0（任务 0.6）与阶段 4（任务 4.1–4.8）。架构决策见 `../design.md` D1（CliAdapter）、D10（终端形态）、D13（`adapter/cli`）。

## 参数表

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `--role` | String | 是 | 角色名，受 `^[a-zA-Z0-9_-]{1,36}$` 约束 |
| `--prompt` | String | 否 | 用户提示词；缺省时使用 `first.md` |
| `--image` | List | 否 | 图片路径，可重复 |
| `--tools` | List | 否 | 会话级工具子集，覆盖角色默认 |
| `--session` | String | 否 | 会话标识；缺省时新建。须匹配 `^[A-Za-z0-9]{1,64}$` |
| `--model` | String | 否 | 覆盖角色与全局默认模型 |
| `--config` | String | 否 | 配置根覆盖，默认 `~/.lwl` |

- 只提供长参数（Spring Boot 原生风格），**不提供短参数**。
- `--web` 不出现在本变更（属于 `add-ping-ai-web-shell`）。
- `--prompt` 与 `--image` 组装成一条 `type=user` 的上游消息提交；未提供 `--prompt` 时使用 `first.md` 的内容。

## box 消费

```
Session.submit(user) -> Completion
CliAdapter: completion.subscribe(render) + completion.await(timeout)
```

按对象 id 归组，按 id 升序渲染：

```
user / text   -> stdout
tool          -> 静默（--verbose 时写 stderr 一行）
confirm       -> 非交互形态不应出现；出现即按拒绝处理并告警
error         -> stderr + 非零退出
未知 type     -> 忽略
```

- CLI 只实现自己认识的 `type` 分派表，未知类型忽略 —— 不依赖核心新增的类型。
- 对象结束（`seq` 为非负以外）时，`user` / `text` 对象补一个换行。
- CLI **不实现** `ack` / `pull` / 断线恢复：进程内直连，没有丢包与重连。
- **stdout 只放回答内容，诊断信息一律走 stderr** —— 这是「可接入管道」的实现前提。
- 退出码约定：`0` 成功；`2` 参数错误（含 `--session` 非法）；`3` 角色不存在；`4` 配置不可读；`5` 模型调用失败。

## 验证方式

- 对 stdout 做转义序列断言（`\u001b` 不出现）。
- 启动后检查无监听端口。
- 文档中的示例命令可原样执行成功。
