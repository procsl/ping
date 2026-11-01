package cn.procsl.ping.boot.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 唯一入口：非 web 形态启动，执行一次命令后退出。
 *
 * <p>已知债务：手写 main 与 AGENTS.md「业务模块 src/main 不得自带 main」冲突，
 * 待 {@code ping-apt} 生成 {@code <Module>Launcher} 后替换（见 design.md D10）。</p>
 */
public final class PingAiLauncher {

    private PingAiLauncher() {
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(PingAiApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext context = application.run(args);
        int code;
        try {
            code = context.getBean(CliRunner.class).run();
        } catch (RuntimeException e) {
            context.close();
            throw e;
        }
        System.exit(SpringApplication.exit(context, () -> code));
    }
}
