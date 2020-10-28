package cn.procsl.ping.boot.doc.config;

import cn.procsl.ping.boot.rest.config.RestWebAutoConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.webmvc.core.SpringDocWebMvcConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * @author procsl
 * @date 2020/02/23
 */
@Slf4j
@Configuration
@AutoConfigureAfter({RestWebAutoConfiguration.class, SpringDocWebMvcConfiguration.class})
@ConditionalOnMissingBean(OpenApiAutoConfiguration.class)
@ConditionalOnBean(RestWebAutoConfiguration.class)
public class OpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI restOpenAPI() {
        OpenAPI api = new OpenAPI();
        Components components = new Components();

        api.setComponents(components);
        api.setServers(Collections.singletonList(new Server().url("http://localhost:8081").description("测试链接")));
        api.info(new Info()
            .title("接口文档")
            .version("1.0")
            .description("这是测试用的接口文档")
            .termsOfService("http://localhost:8081/index.html")
            .license(new License().name("Apache 2.0").url("http://api.procsl.cn")));
        return api;
    }


}
