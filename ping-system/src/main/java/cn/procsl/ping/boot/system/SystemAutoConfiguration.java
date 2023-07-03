package cn.procsl.ping.boot.system;

import cn.procsl.ping.boot.jpa.support.EnableDomainRepositories;
import cn.procsl.ping.boot.system.constant.SystemConfigureProperties;
import cn.procsl.ping.boot.system.domain.user.RoleSettingService;
import cn.procsl.ping.boot.system.service.ConfigFacade;
import cn.procsl.ping.boot.system.web.user.AuthenticateInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Order
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@EnableTransactionManagement
@ConditionalOnMissingBean({SystemAutoConfiguration.class})
@EntityScan(basePackages = "cn.procsl.ping.boot.system.domain")
@EnableJpaRepositories(basePackages = {"cn.procsl.ping.boot.system.domain"}, bootstrapMode = BootstrapMode.LAZY)
@ComponentScan(basePackages = {"cn.procsl.ping.boot.system.web", "cn.procsl.ping.boot.system.query", "cn.procsl.ping.boot.system.service", "cn.procsl.ping.boot.system.listener", "cn.procsl.ping.boot.system.adapter"}, basePackageClasses = {ConfigFacade.class, RoleSettingService.class,})
@EnableConfigurationProperties(SystemConfigureProperties.class)
@RequiredArgsConstructor
public class SystemAutoConfiguration implements WebMvcConfigurer {

    final SystemConfigureProperties systemConfigureProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticateInterceptor(systemConfigureProperties.getAuthenticatesPrefix())).addPathPatterns("/**");
    }


    @Configuration
    @EnableDomainRepositories(basePackages = "cn.procsl.ping.boot.system.query")
    public static class DomainRepositoryConfigurer {

    }

}
