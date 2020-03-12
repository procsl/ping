package cn.procsl.ping.boot.api.config;

import cn.procsl.ping.boot.api.core.OpenApiCustom;
import cn.procsl.ping.boot.api.core.OpenApiRequestBuilder;
import cn.procsl.ping.boot.api.core.RestVisitor;
import cn.procsl.ping.boot.api.core.Visitor;
import cn.procsl.ping.boot.rest.config.RestWebAutoConfiguration;
import cn.procsl.ping.boot.rest.config.RestWebProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GenericParameterBuilder;
import org.springdoc.core.OperationBuilder;
import org.springdoc.core.RequestBodyBuilder;
import org.springdoc.core.SpringDocWebMvcConfiguration;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    /**
     * 替换组件
     *
     * @param parameterBuilder
     * @param requestBodyBuilder
     * @param operationBuilder
     * @param operationCustomizers
     * @param parameterCustomizers
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenApiRequestBuilder openRequestBuilder(GenericParameterBuilder parameterBuilder,
                                                    RequestBodyBuilder requestBodyBuilder,
                                                    OperationBuilder operationBuilder,
                                                    Optional<List<OperationCustomizer>> operationCustomizers,
                                                    Optional<List<ParameterCustomizer>> parameterCustomizers) {
        return new OpenApiRequestBuilder(parameterBuilder,
                requestBodyBuilder,
                operationBuilder,
                operationCustomizers,
                parameterCustomizers);
    }

    @Bean
    public OpenApiCustom apiCustom(@Autowired RequestMappingHandlerMapping mpping,
                                   @Autowired List<Visitor> functions,
                                   @Autowired RestWebProperties properties) {
        return new OpenApiCustom(properties, mpping, functions);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestVisitor restVisitor(RestWebProperties properties) {
        if (ObjectUtils.isEmpty(properties.getMimeSubtype())) {
            return new RestVisitor(null, null);
        }
        MediaType xml = new MediaType("application", properties.getMimeSubtype() + "+xml");
        MediaType json = new MediaType("application", properties.getMimeSubtype() + "+json");
        return new RestVisitor(xml.toString(), json.toString());
    }


}
