package cn.procsl.ping.boot.data.config;

import cn.procsl.ping.boot.data.naming.LowCasePhysicalNamingStrategy;
import cn.procsl.ping.boot.data.naming.NameImplicitNamingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Configuration
@ConditionalOnMissingBean(DataAutoConfiguration.class)
@EnableConfigurationProperties({DataProperties.class})
@RequiredArgsConstructor
public class DataAutoConfiguration {

    final DataProperties dataProperties;

    @Bean
    @ConditionalOnMissingBean
    public NameImplicitNamingStrategy nameImplicitNamingStrategy() {
        return new NameImplicitNamingStrategy(dataProperties);
    }


    @Bean
    @ConditionalOnMissingBean
    public LowCasePhysicalNamingStrategy lowCasePhysicalNamingStrategy() {
        return new LowCasePhysicalNamingStrategy(dataProperties);
    }

}
