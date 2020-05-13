package cn.procsl.ping.boot.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author procsl
 * @date 2020/04/10
 */
@Configuration
@EnableConfigurationProperties({UserProperties.class})
@RequiredArgsConstructor
@ComponentScan({"cn.procsl.ping.boot.user.domain.rbac.service"})
@ConditionalOnMissingBean({UserAutoConfiguration.class, UserAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.user.domain.rbac.repository")
@EntityScan(basePackages = "cn.procsl.ping.boot.user.domain.rbac.model")
public class UserAutoConfiguration {

    final UserProperties properties;

}
