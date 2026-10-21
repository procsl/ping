package cn.procsl.ping.boot.ui;

import cn.procsl.ping.boot.ui.api.UIComponentsController;
import cn.procsl.ping.boot.ui.domain.Component;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 自动配置装配测试（任务 3.2 / 3.3）：端点 bean 注册、openapi.export 导出模式跳过。
 * 编译边界与失败语义见 {@code UiComponentTreeFactoryTest}（编译流程内聚于 compiler 包）。
 */
class UIAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(UIAutoConfiguration.class);

    // --- 3.2 注册 ---

    @Test
    void defaultModeRegistersEndpointBeanAndCompiles() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(UIComponentsController.class);
            assertThat(context).doesNotHaveBean(Component.class); // 产物不进容器，是 controller 私有状态

            Component app = context.getBean(UIComponentsController.class).components();
            assertThat(app.getType()).isEqualTo("app");
            assertThat(app.getChildren()).isNotEmpty(); // 测试 classpath 存在 compose.json
        });
    }

    @Test
    void explicitFalseExportPropertyAlsoRegisters() {
        contextRunner.withPropertyValues("openapi.export=false")
            .run(context -> assertThat(context).hasSingleBean(UIComponentsController.class));
    }

    // --- 3.3 导出模式跳过编译与端点注册（design D11 唯一豁免） ---

    @Test
    void exportModeSkipsCompilationAndEndpoint() {
        contextRunner.withPropertyValues("openapi.export=true")
            .run(context -> assertThat(context).doesNotHaveBean(UIComponentsController.class));
    }
}
