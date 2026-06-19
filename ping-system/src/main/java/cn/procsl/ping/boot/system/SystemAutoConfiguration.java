package cn.procsl.ping.boot.system;

import cn.procsl.ping.boot.jpa.support.EnableDomainRepositories;
import cn.procsl.ping.boot.system.api.user.AuthenticateInterceptor;
import cn.procsl.ping.boot.system.constant.SystemConfigureProperties;
import cn.procsl.ping.boot.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static cn.procsl.ping.boot.web.RestWebAutoConfiguration.PUBLIC_STATIC_RESOURCES;

@Order
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@EnableTransactionManagement
@EntityScan(basePackages = "cn.procsl.ping.boot.system.domain")
@EnableJpaRepositories(basePackages = {"cn.procsl.ping.boot.system.domain"}, bootstrapMode = BootstrapMode.LAZY)
@ComponentScan(basePackages = {"cn.procsl.ping.boot.system.api", "cn.procsl.ping.boot.system.query", "cn.procsl.ping.boot.system.service", "cn.procsl.ping.boot.system.adapter"}, basePackageClasses = {ConfigService.class})
@EnableConfigurationProperties(SystemConfigureProperties.class)
@RequiredArgsConstructor
public class SystemAutoConfiguration implements WebMvcConfigurer {

    final SystemConfigureProperties systemConfigureProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticateInterceptor(systemConfigureProperties.getAuthenticatesPrefix()))
            .excludePathPatterns(PUBLIC_STATIC_RESOURCES).addPathPatterns("/**");
    }


    @Configuration(proxyBeanMethods = false)
    @EnableDomainRepositories(basePackages = "cn.procsl.ping.boot.system.query")
    public static class DomainRepositoryConfigurer {

    }

}
