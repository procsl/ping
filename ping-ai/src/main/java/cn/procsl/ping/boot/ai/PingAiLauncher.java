package cn.procsl.ping.boot.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 启动入口：以 servlet web 形态启动。
 *
 * <p>独立运行端口见 {@code application.properties} 的 {@code server.port}；
 * 当前仅为 Spring Boot 空壳，AI 能力待重新设计。</p>
 */
public final class PingAiLauncher {

    private static final Logger log = LoggerFactory.getLogger(PingAiLauncher.class);

    private PingAiLauncher() {
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(PingAiApplication.class);
        application.setWebApplicationType(WebApplicationType.SERVLET);
        ConfigurableApplicationContext context = application.run(args);
        String port = context.getEnvironment().getProperty("server.port", "8080");
        log.info("Ping AI 已启动: http://localhost:{}", port);
    }
}
