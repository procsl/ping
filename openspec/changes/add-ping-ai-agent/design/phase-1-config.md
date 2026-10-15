# 阶段 1 设计：基础设施与配置

对应 `tasks.md` 阶段 1（任务 1.1–1.10）。本文档承载实现级细节：数据模型字段、示例文件、边界用例。架构决策（配置不耦合 Spring、目录分层理由）见 `../design.md` D7。

## 1.1 依赖调整

移除 `ping-ai/pom.xml` 的 `ping-jpa` 依赖、保留 `ping-web`。会话是 JSON 文件，不需要数据库。

## 1.2 配置根初始化

一次原子操作完成三件事，不产生中间状态：

```
解析路径 (默认 ~/.lwl, 可被参数覆盖)
   → 创建骨架 bin/ logs/ nvim/ roles/
   → 不可写时降级 + 告警 (不抛异常)
```

验证分支：默认路径、覆盖路径、只读目录。

## 1.3 config.json 数据模型

### 完整示例

```json
{
  "version": 1,
  "providers": {
    "openai": {
      "api_key": "sk-...",
      "base_url": "https://api.openai.com/v1",
      "timeout_seconds": 60
    },
    "anthropic": {
      "api_key": "sk-ant-...",
      "base_url": "https://api.anthropic.com",
      "headers": { "anthropic-version": "2023-06-01" }
    }
  },
  "models": {
    "gpt5":     { "provider": "openai",    "model": "gpt-5", "max_tokens": 4096 },
    "claude_x": { "provider": "anthropic", "model": "claude-x" }
  },
  "default_model": "gpt5",
  "chat": { "max_history": 40 }
}
```

### 字段表

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `version` | int | 否 | `1` | 配置格式版本，便于后续升级识别 |
| `providers` | Map | 否 | 空 | provider 定义容器，key 为自定义名 |
| `providers.<name>.api_key` | String | 是 | - | 凭据；纯字符串，不支持环境变量引用 |
| `providers.<name>.base_url` | String | 是 | - | 连接地址 |
| `providers.<name>.headers` | Map | 否 | 空 | 透传自定义请求头 |
| `providers.<name>.timeout_seconds` | int | 否 | `60` | 请求超时（秒） |
| `models` | Map | 否 | 空 | 模型别名容器，key 为自定义名 |
| `models.<alias>.provider` | String | 是 | - | 引用 `providers` 键 |
| `models.<alias>.model` | String | 是 | - | 模型 ID，传给 provider |
| `models.<alias>.max_tokens` | int | 否 | provider 默认 | 单次请求最大 token |
| `models.<alias>.temperature` | double | 否 | provider 默认 | 采样温度 |
| `default_model` | String | 否 | 第一个 `models` 键 | 未指定模型时的回落 |
| `chat.max_history` | int | 否 | `40` | 会话历史窗口上限，全局统一，不做角色级覆盖 |

### 解析规则

- **未知字段一律忽略**（向前兼容，用户手写不易因新增字段报错）。
- **不支持环境变量引用**：`api_key` 为纯字符串。
- JSON 字段一律 `snake_case`（仓库约定）。

## 1.4 GlobalConfigLoader

- 输入：`config.json` 路径（来自 1.2 配置根初始化）。
- 产出：`GlobalConfig` 纯 Java 对象（不转 `PropertySource`、不注入 Spring `Environment`）。
- 职责：读文件 → JSON 转 Java 数据模型（record）→ 异常时降级 + 告警（不中断启动）。

### 降级行为契约

| 触发条件 | 行为 | 告警 |
|---|---|---|
| 文件缺失 | 使用内置默认值 | 告警一次 |
| JSON 语法非法 | 使用内置默认值 | 告警一次 |
| 字段缺失 | 用内置默认值补全 | 告警一次 |

## 1.5 ModelCatalog

- 按名解析模型到连接参数。
- 未命中时回落 `default_model` 并告警。
- 验证分支：命中、未命中回落、坏模型名。

## 1.6 角色名校验

正则：`^[a-zA-Z0-9_-]{1,36}$`

### 边界用例表

| 输入 | 结果 | 说明 |
|---|---|---|
| `dev` | ✅ 通过 | 常规名 |
| `my-role_2` | ✅ 通过 | 中划线/下划线/数字组合 |
| `""` (空) | ❌ 拒绝 | 至少一个字符 |
| `../etc` | ❌ 拒绝 | 含点/斜杠 → 防路径穿越 |
| `角色` | ❌ 拒绝 | 非 ASCII |
| `my.role` | ❌ 拒绝 | 含点 |
| `my role` | ❌ 拒绝 | 含空白 |
| `A` / `a` | ✅ 各自通过 | 大小写敏感（Linux 目录） |
| 37+ 字符 | ❌ 拒绝 | 上限 36 字符（UUID 长度，便于外部工具引用） |

## 1.7 角色枚举

- 列出 `roles/` 下所有合法角色名。
- **遇到非法名目录**：直接忽略，不在可用角色列表中展示；仅在日志中输出警告（含目录名与忽略原因），不中断枚举。
- 验证分支：空目录返回空列表、多个角色全部列出、非法名目录被跳过。

## 1.8 角色配置加载

### role.json 示例

```json
{
  "model": "claude_x",
  "tools": ["read_file", "grep", "shell"]
}
```

- `model` 存**名字**（引用 `config.json.models` 的键），不是配置本身 —— 换 key/url 只改全局一处。
- `tools` 是字符串数组，引用代码里写死的全局工具名；将来需要传参时升级为对象 `{"grep": {"max_results": 5}}`，字符串与对象同时接受，向后兼容。
- 加载内容：`role.json` + `system.md` + `first.md`。
- 验证分支：文件存在时内容正确、缺失时降级。

## 1.9 未知角色处理

- 输出错误 + 可用角色列表，非零退出。
- 不静默创建空角色。

## 1.10 配置目录约定文档

文档示例 JSON 必须能被 1.3、1.5 的代码原样解析。
