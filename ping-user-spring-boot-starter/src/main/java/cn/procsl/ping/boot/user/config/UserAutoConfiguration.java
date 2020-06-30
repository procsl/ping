package cn.procsl.ping.boot.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


/**
 * 用户模块自动配置项
 *
 * @author procsl
 * @date 2020/04/10
 */
@Configuration
@EnableConfigurationProperties({UserProperties.class})
@RequiredArgsConstructor
@ComponentScan({
        "cn.procsl.ping.boot.user.facade",
        "cn.procsl.ping.boot.user.domain.rbac.service",
        "cn.procsl.ping.boot.user.domain.resource.service",
        "cn.procsl.ping.boot.user.domain.user.service",
})
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@EnableJpaRepositories(
        basePackages = {
                "cn.procsl.ping.boot.user.domain.rbac.repository",
                "cn.procsl.ping.boot.user.domain.resource.repository",
                "cn.procsl.ping.boot.user.domain.user.repository"
        })
@EntityScan(basePackages = {
        "cn.procsl.ping.boot.user.domain.rbac.entity",
        "cn.procsl.ping.boot.user.domain.resource.entity",
        "cn.procsl.ping.boot.user.domain.user.entity",
})
public class UserAutoConfiguration {

    final UserProperties properties;

}
