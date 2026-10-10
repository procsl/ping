package cn.procsl.ping.boot.ui;

import cn.procsl.ping.boot.ui.component.UIComponentRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
// 控制器与 @ResourceReference 标注需要被注册到宿主上下文（ping-system 等模块不扫描 ping-ui 包）
@ComponentScan(basePackages = "cn.procsl.ping.boot.ui.component.api")
public class UIAutoConfiguration {

    /**
     * 抽象组件内存注册表：上下文刷新期一次性读取固定索引与 OpenAPI 文档，
     * 装配成不可变快照，之后只读。
     */
    @Bean
    UIComponentRegistry uiComponentRegistry(
        ObjectProvider<RequestMappingHandlerMapping> handlerMappings) {
        return new UIComponentRegistry(handlerMappings);
    }

}
