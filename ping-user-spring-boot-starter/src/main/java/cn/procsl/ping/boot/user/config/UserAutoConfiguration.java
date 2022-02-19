package cn.procsl.ping.boot.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@RequiredArgsConstructor
@EntityScan(basePackages = "cn.procsl.ping.boot.user")
@EnableJpaRepositories(basePackages = {"cn.procsl.ping.boot.user"})
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class UserAutoConfiguration {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> Optional.ofNullable(10000000L);
    }

}
