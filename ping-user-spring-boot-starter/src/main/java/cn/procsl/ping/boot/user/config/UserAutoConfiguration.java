package cn.procsl.ping.boot.user.config;

import cn.procsl.ping.boot.user.domain.rbac.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author procsl
 * @date 2020/04/10
 */
@Configuration
@ConditionalOnMissingBean(UserAutoConfiguration.class)
@EnableConfigurationProperties({UserProperties.class})
@RequiredArgsConstructor
@ComponentScan({
        "cn.procsl.ping.boot.user.domain.rbac.repository",
        "cn.procsl.ping.boot.user.domain.rbac.service"
})
@EnableJpaAuditing
public class UserAutoConfiguration {

    final UserProperties properties;
}
