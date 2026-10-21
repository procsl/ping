package cn.procsl.ping.boot.ui;

import cn.procsl.ping.boot.ui.api.UIComponentsController;
import cn.procsl.ping.boot.ui.compiler.UiComponentTreeFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * ping-ui 自动配置（design phase-2）：只做装配，不含编译逻辑——
 * <ul>
 *   <li>注册 {@code GET /v1/ui/components} 端点（controller bean）；
 *       controller 无状态：构造时经 factory 验证输入（失败即启动失败），
 *       请求时现场编译，产物不进容器
 *       （避免 {@code Component} 这类通用 domain 类型成为全局注入点）</li>
 *   <li>{@code openapi.export=true} 导出进程跳过注册（design D11，唯一豁免，
 *       否则首次构建 openapi 文档尚未生成会自锁）</li>
 * </ul>
 *
 * <p>编译流程（扫描 → 输入完整性校验 → Transformer 链）内聚于
 * {@link UiComponentTreeFactory}（compiler 包，无 Spring 依赖，可直测）。
 */
@AutoConfiguration
@ConditionalOnProperty(name = "openapi.export", havingValue = "false", matchIfMissing = true)
public class UIAutoConfiguration {

    @Bean
    public UIComponentsController uiComponentsController() {
        return new UIComponentsController();
    }
}
