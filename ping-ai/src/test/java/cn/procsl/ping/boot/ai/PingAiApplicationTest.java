package cn.procsl.ping.boot.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PingAiApplicationTest {

    @TempDir
    Path temp;

    @Test
    void 终端形态以非web上下文启动且不注册web服务() {
        SpringApplication application = new SpringApplication(PingAiApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setBannerMode(Banner.Mode.OFF);
        application.setDefaultProperties(Map.of("logging.level.root", "ERROR"));

        try (ConfigurableApplicationContext context = application.run(
                "--role=missing", "--config=" + temp)) {
            assertThat(context.getBeanNamesForType(WebServerApplicationContext.class)).isEmpty();
        }
    }
}
