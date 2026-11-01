# ping-ai 命令行用法

只提供长参数（Spring Boot 原生风格），不提供短参数。

```bash
java -jar ping-ai.jar --role dev --prompt "帮我看看这个文件"
```

## 参数

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `--role` | String | 是 | 角色名，受 `^[a-zA-Z0-9_-]{1,36}$` 约束 |
| `--prompt` | String | 否 | 用户提示词；缺省时使用角色的 `first.md` |
| `--image` | List | 否 | 图片路径，可重复 |
| `--tools` | List | 否 | 会话级工具子集，覆盖角色默认；逗号分隔或重复传入 |
| `--session` | String | 否 | 会话标识；须匹配 `^[A-Za-z0-9]{1,64}$`，缺省时新建 |
| `--model` | String | 否 | 覆盖角色与全局默认模型 |
| `--config` | String | 否 | 配置根覆盖，默认 `~/.lwl` |
| `--verbose` | 开关 | 否 | 工具过程写标准错误 |

## 输出与退出码

- 标准输出只放回答内容；诊断信息、工具过程、错误一律走标准错误。
- 标准输出不含光标控制、清屏、颜色控制序列，可直接接入管道。

| 退出码 | 含义 |
|---|---|
| `0` | 成功 |
| `2` | 参数错误（含非法会话标识、缺少提示词） |
| `3` | 角色不存在 |
| `4` | 配置不可读（无可用模型、provider 未定义） |
| `5` | 模型调用失败 |

## 示例

```bash
# 使用角色默认提示词
java -jar ping-ai.jar --role dev

# 指定提示词与图片
java -jar ping-ai.jar --role dev --prompt "这张图里是什么" --image ./a.png

# 会话级工具子集 + 详细模式
java -jar ping-ai.jar --role dev --prompt "找一下 TODO" --tools grep --verbose

# 覆盖配置根与模型
java -jar ping-ai.jar --role dev --prompt "hi" --config /tmp/lwl --model gpt5

# 接入管道
java -jar ping-ai.jar --role dev --prompt "总结 README" | head -20
```

## 配置准备

首次使用需要：

1. `~/.lwl/config.json`：provider 凭据与模型清单（见 `configuration.md`）。
2. `~/.lwl/roles/<name>/role.json`：模型别名与工具子集。
3. 可选 `system.md`、`first.md`。

目录不可写时只降级告警，不中断启动。
