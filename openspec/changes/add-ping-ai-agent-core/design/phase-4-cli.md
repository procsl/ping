# 阶段 4 设计：非交互式命令行

对应 `tasks.md` 阶段 4（任务 4.1–4.8）。架构决策见 `../design.md` D1（CliAdapter）、D10（终端形态）、D13（`adapter/cli`）。

> 注：本文档待细化，当前只固定参数集合、事件消费方式与退出码语义。

## 参数表

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `--role` | String | 是 | 角色名，受 `^[a-zA-Z0-9_-]{1,36}$` 约束 |
| `--prompt` | String | 否 | 用户提示词；缺省时使用 `first.md` |
| `--image` | List | 否 | 图片路径，可重复 |
| `--tools` | List | 否 | 会话级工具子集，覆盖角色默认 |
| `--session` | String | 否 | 会话标识；缺省时新建 |
| `--model` | String | 否 | 覆盖角色与全局默认模型 |
| `--config` | String | 否 | 配置根覆盖，默认 `~/.lwl` |

- 只提供长参数（Spring Boot 原生风格），**不提供短参数**。
- `--web` 不出现在本变更（属于 `add-ping-ai-web-shell`）。

## 事件消费

```
EventStream --subscribe--> CliAdapter
  session.started    -> 静默（--verbose 时写 stderr）
  message.delta      -> stdout.write
  tool.requested     -> 静默（--verbose 时写 stderr）
  tool.finished      -> 静默（--verbose 时写 stderr）
  tool.confirm       -> 非交互形态不应出现；出现即视为策略配置错误
  error              -> stderr + 非零退出
  message.completed  -> stdout 换行
  session.ended      -> 退出 0
```

- **stdout 只放回答内容，诊断信息一律走 stderr** —— 这是「可接入管道」的实现前提。
- 退出码约定：`0` 成功；`2` 参数错误；`3` 角色不存在；`4` 配置不可读；`5` 模型调用失败。

## 验证方式

- 对 stdout 做转义序列断言（`\u001b` 不出现）。
- 启动后检查无监听端口。
- 文档中的示例命令可原样执行成功。
