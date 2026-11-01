# 阶段 3 设计：进程编排与 RPC

对应 `tasks.md` 阶段 3（任务 3.1–3.9）。架构决策见 `../design.md` D2。

> 注：本文档待细化，当前只固定启动时序、双向调用形态与进程编排要求。

## 交互式启动时序

```
$ ping-ai --role dev
  │
  ├─ 1. 解析长参数 (--role/--tools/--session/--model)
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

- Java 侧视角的 rpc 编解码在任务 3.5–3.7。
- lua 侧 `call(event, payload)` 的处理器在 `phase-4-nvim-ui.md`。
- 下行调用按批发送：一次 `exec_lua` 携带一批增量，而不是一个 token 一次调用。

## 进程编排要求

- `spawn nvim --clean --listen <addr>`：继承 java 的 stdin/stdout/stderr → nvim 直接在真终端全屏渲染。
- java 全程不碰 stdio，日志落 `~/.lwl/logs/`。
- Neovim 退出时（正常与异常）均触发收尾：停事件流、落盘未完成会话、释放 socket。
- socket 文件在退出时清理；异常退出后残留的 socket 在下次启动时按 pid 判定可回收。

## 背压

- Java → lua 方向是通知（fire-and-forget）：若 nvim 侧处理不过来，事件会堆积。**因此下行必须攒批**，并由探针测得的往返延迟决定批大小上限。
- lua → Java 方向是低频事件（提交、取消、确认），不需要额外背压。
