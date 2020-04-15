package cn.procsl.ping.boot.rbac.config;

import cn.procsl.ping.boot.rbac.domain.facade.Initialization;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author procsl
 * @date 2020/04/10
 */
@Configuration
@ConditionalOnMissingBean(RbacAutoConfiguration.class)
@EnableConfigurationProperties({RbacProperties.class})
@RequiredArgsConstructor
@ComponentScan("cn.procsl.ping.boot.rbac.domain.repository")
@EnableJpaAuditing
public class RbacAutoConfiguration {

    final RbacProperties properties;

    @Bean
    @ConditionalOnMissingBean
//    @ConditionalOnProperty(prefix = "ping.business.rbac", name = "enableInitDefaultData", havingValue = "true")
    public Initialization initializationRbacData() {
        return new Initialization(this.properties);
    }

    @Bean
    @ConditionalOnBean(Initialization.class)
    public DefaultInitialization defaultInitialization() {
        return new DefaultInitialization();
    }

}
