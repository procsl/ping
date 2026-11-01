# 阶段 1 设计：启动形态与自动配置

对应 `tasks.md` 阶段 1（任务 1.1–1.5）。架构决策见 `../design.md` D10（web 部分）与 D15。

> 注：本文档待细化，当前只固定启动分支、自动配置条件与关闭开关。

## 启动分支

```
PingAiLauncher
   │
   ├─ 参数含 --web  → WebApplicationType.SERVLET  → 监听 HTTP + WS
   └─ 否则          → WebApplicationType.NONE     → 不监听
```

- `--web` 是唯一触发条件，不存在环境变量或配置文件的隐式开关。
- 终端形态的行为由核心变更负责，本变更不修改默认值。

## 自动配置

```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
   └─ cn.procsl.ping.boot.ai.adapter.web.AiWebAutoConfiguration
          @ConditionalOnWebApplication(type = SERVLET)
          @ConditionalOnProperty(...)      // 关闭开关，见任务 1.5
```

- 条件用**上下文类型**，不用「是否被依赖」（`../design.md` D15）。
- 非 web 宿主下自动配置不生效，因此不会拖入 servlet 相关组件。

## 验证方式

- 指定 `--web`：断言监听端口存在。
- 未指定：断言无监听端口（回归）。
- web 宿主测试应用：断言端点与 WS 通道可用。
- 非 web 宿主测试应用：断言 web 组件未注册。
