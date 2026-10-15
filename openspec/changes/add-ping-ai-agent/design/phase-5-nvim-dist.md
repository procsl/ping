# 阶段 5 设计：Neovim 分发与进程编排

对应 `tasks.md` 阶段 5（任务 5.1–5.14）。本文档承载实现级细节。架构决策见 `../design.md` D2（进程模型）、D11（分发）。

> 注：本文档由 design.md 迁移而来，尚未按细化节奏展开；后续细化时补充下载地址解析规则与 rpc 编解码格式。

## 交互式启动时序

```
$ ping-ai --role dev
  │
  ├─ 1. 解析长参数 (--role/--tools/--session/--model/--web)
  ├─ 2. 读 ~/.lwl/config.json -> GlobalConfig 对象
  ├─ 3. 读 roles/dev/{system.md,first.md,role.json}   (校验 role-name)
  ├─ 4. 确认 Neovim 存在, 缺失则按 D11 下载到 ~/.lwl/bin/
  ├─ 5. 生成唯一 addr (~/.lwl/nvim/<pid>.sock)
  ├─ 6. spawn  nvim --clean --listen <addr>
  ├─ 7. 注入 runtimepath -> ~/.lwl/nvim/  (lua 组件层 + 布局 + syntax 脚本)
  ├─ 8. 连接 <addr>, 建立双向 msgpack-rpc
  ├─ 9. lua 组件层初始化布局, 上报 ready
  ├─10. first.md 内容 -> 预填输入框
  ├─11. 进入事件循环
  └─12. nvim 退出 -> 停事件流, 收尾, 进程结束
        (java 全程不写 stdout, 日志在 ~/.lwl/logs/)
```

## 双向调用形态

```
java -> lua    请求/响应:  经 nvim 的 API 转到 lua 组件层
                          nvim_exec_lua("return require('agent').call(...)", {json})
                          用于 create/patch/append 等下行组件操作

lua  -> java   通知/请求:  vim.rpcnotify(chan_id, "agent.event", payload)
                          用于 submit / cancel / tool.approve 等上行事件
                          chan_id 即 --listen 建立的那个 channel
```

- Java 侧视角的 rpc 编解码在本阶段（任务 5.10–5.12）。
- lua 侧 `call(event, payload)` 的处理器在 `phase-6-nvim-ui.md`。

## Neovim 分发步骤

```
识别平台 -> 解析版本与 URL -> 校验和 -> 落位 ~/.lwl/bin/
        -> 置可执行位 -> 后续启动直接复用 (已存在则跳过)
```

### 平台矩阵（第一版）

```
linux-x64   linux-arm64   darwin-x64   darwin-arm64
windows-* (兜底: 优先 named pipe, 不可用则 TCP loopback)
```

- 版本锁定，不跟随系统或最新版。
- `--clean` 下通过显式 `runtimepath` 指向 `~/.lwl/nvim/`。
- ImageMagick 等可选外部工具若将来需要，复用同一套分发机制，不另建管线。

## 进程编排要求

- `spawn nvim --clean --listen <addr>`：继承 java 的 stdin/stdout/stderr → nvim 直接在真终端全屏渲染。
- java 全程不碰 stdio，日志落 `~/.lwl/logs/`。
- Neovim 退出时（正常与异常）均触发收尾。
