package cn.procsl.ping.boot.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static cn.procsl.ping.boot.user.config.UserProperties.*;


/**
 * 用户模块自动配置项
 *
 * @author procsl
 * @date 2020/04/10
 */
@Configuration
@EnableConfigurationProperties({UserProperties.class})
@RequiredArgsConstructor
@ComponentScan({COMPONENT_SCAN})
@ConditionalOnMissingBean({UserAutoConfiguration.class, UserAutoConfiguration.class})
@EnableJpaRepositories(basePackages = REPOSITORY_PATH)
@EntityScan(basePackages = ENTITY_SCAN)
public class UserAutoConfiguration {

    final UserProperties properties;

}
