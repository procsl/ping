# ping-ai 配置目录约定

配置根默认 `~/.lwl`，可用 `--config <path>` 覆盖。启动时会一次性创建骨架目录；目录不可写时只降级告警，不中断启动。

```
~/.lwl/
  config.json                 全局配置
  logs/                       java 日志
  images/                     图片输入落盘（按内容哈希命名）
  roles/<name>/
    role.json                 模型引用 + 工具子集
    system.md                 系统提示词
    first.md                  默认用户提示词
    sessions/<session-id>.json
```

## config.json

```json
{
  "version": 1,
  "providers": {
    "openai": {
      "api_key": "sk-...",
      "base_url": "https://api.openai.com/v1",
      "timeout_seconds": 60
    }
  },
  "models": {
    "gpt5": { "provider": "openai", "model": "gpt-5", "max_tokens": 4096, "temperature": 0.2 }
  },
  "default_model": "gpt5",
  "chat": { "max_history": 40 }
}
```

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `version` | int | 否 | `1` | 配置格式版本 |
| `providers.<name>.api_key` | String | 是 | - | 凭据；纯字符串，不支持环境变量引用 |
| `providers.<name>.base_url` | String | 是 | - | 连接地址 |
| `providers.<name>.headers` | Map | 否 | 空 | 透传请求头 |
| `providers.<name>.timeout_seconds` | int | 否 | `60` | 请求超时（秒） |
| `models.<alias>.provider` | String | 是 | - | 引用 `providers` 的键 |
| `models.<alias>.model` | String | 是 | - | 传给 provider 的模型 ID |
| `models.<alias>.max_tokens` | int | 否 | provider 默认 | 单次请求最大 token |
| `models.<alias>.temperature` | double | 否 | provider 默认 | 采样温度 |
| `default_model` | String | 否 | 第一个模型别名 | 未指定模型时的回落 |
| `chat.max_history` | int | 否 | `40` | 送入模型的历史窗口上限 |

- 未知字段一律忽略（向前兼容）。
- JSON 字段统一 `snake_case`。
- 文件缺失、语法非法、字段缺失都只告警并降级，不中断启动。

## role.json

```json
{
  "model": "gpt5",
  "tools": ["read_file", "grep"]
}
```

- `model` 存**模型别名**，引用 `config.json.models` 的键；省略时使用全局默认。
- `tools` 引用工具服务里的工具名；省略时为空。
- 角色名限定 `^[a-zA-Z0-9_-]{1,36}$`；非法名目录在枚举时忽略并告警。

## 提示词

- `system.md`：系统提示词；缺失时降级继续。
- `first.md`：默认用户提示词；非交互式未提供 `--prompt` 时使用，缺失则报错退出（不发送空消息）。

## 会话文件

```json
{
  "session_id": "20261101153045123ABCDEFGHIJ",
  "role": "dev",
  "model": "gpt5",
  "messages": [
    { "type": "system", "text": "..." },
    { "type": "user", "text": "..." },
    { "type": "assistant", "text": "...", "tool_calls": [], "interrupted": false }
  ]
}
```

- 只有消息表落盘；对象表由消息表重建，不落盘。
- 会话标识：`yyyyMMddHHmmssSSS` + 10 位大写随机字母；`--session` 传入值须匹配 `^[A-Za-z0-9]{1,64}$`。
