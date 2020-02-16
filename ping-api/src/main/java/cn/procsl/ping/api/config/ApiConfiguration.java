package cn.procsl.ping.api.config;

import com.google.common.collect.ImmutableSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author procsl
 * @date 2020/02/15
 */
@EnableSwagger2
@Configuration
public class ApiConfiguration {

    @Bean
    public Docket docket(ApiInfo apiInfo) {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .produces(ImmutableSet.of("application/xml", "application/json", "text/plain"))
                .select()
                //controller匹配规则
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("开放接口API")
                .termsOfServiceUrl("http://localhost:8080/v2/api-docs")
                .description("开放API")
                .contact(new Contact("朝闻道", "http://www.procsl.cn/", "program_chen@foxmail.com"))
                .version("1.0")
                .build();
    }
}
