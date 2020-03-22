package cn.procsl.ping.business.user.config;

import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@ComponentScan("cn.procsl.ping.business.user.impl")
@Configuration
@EnableLoadTimeWeaving
@EnableSpringConfigured
public class UserAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ImplicitNamingStrategyJpaCompliantImpl nameImplicitNamingStrategy() {
        return new ImplicitNamingStrategyJpaCompliantImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public LowCasePhysicalNamingStrategy lowCasePhysicalNamingStrategy() {
        return new LowCasePhysicalNamingStrategy();
    }
}
