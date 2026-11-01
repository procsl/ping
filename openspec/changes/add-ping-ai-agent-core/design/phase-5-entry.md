# 阶段 5 设计：入口与可执行打包

对应 `tasks.md` 阶段 0（任务 0.7）与阶段 5（任务 5.1–5.4）。架构决策见 `../design.md` D10。

## 入口

```
ping-ai/src/main/java/cn/procsl/ping/boot/ai/PingAiLauncher.java
```

- 启动类手写在 `src/main`，`src/main` 中不得出现第二个 `main`。
- 以 `SpringApplication` 构造器显式设置 `WebApplicationType.NONE` 后启动。
- 启动顺序：配置根初始化 → `GlobalConfigLoader` → 角色解析 → `ToolService` 查询工具 → `SessionFactory` 创建 Session（含历史重建）→ 适配器选择 → `submit` 并消费 `Completion`。
- Session 由工厂创建，**不挂在配置对象上**；工厂只接收配置、工具服务、hook 链与输出端口。

## 启动形态

| 形态 | 触发 | `WebApplicationType` | 网络监听 |
|---|---|---|---|
| 终端 | 默认 | `NONE` | 无 |
| Web | `--web` | `SERVLET` | HTTP + WebSocket |

本变更只实现第一行；第二行属于 `add-ping-ai-web-shell`。

## 打包

在 `ping-ai/pom.xml` 中直接配置：

```xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <version>${springboot.version}</version>
  <configuration>
    <mainClass>cn.procsl.ping.boot.ai.PingAiLauncher</mainClass>
  </configuration>
</plugin>
```

- 不引入 `ping.standalone` 开关（该属性当前无任何插件消费）。
- 验证：`./mvnw -Ph2 -pl ping-ai -DskipTests package` 后 `java -jar` 可启动。

## 已知债务

| 约定（AGENTS.md） | 现状 | 后续处置 |
|---|---|---|
| 业务模块 `src/main` 不得自带 main | `PingAiLauncher` 手写在 `src/main` | `ping-apt` 实现 Launcher 生成后替换 |
| `mainClass` 由 `ping-apt` 生成 | pom 中显式写死 | 同上 |
| `ping.standalone` 控制 repackage | 未引入 | 与 Launcher 生成一起做成基础设施变更 |

**为什么不在本变更偿还**：该机制影响所有业务模块的打包方式，属于仓库级基础设施变更；塞进本变更会让核心能力被构建改造拖住。
