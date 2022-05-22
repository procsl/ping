package cn.procsl.ping.boot.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static cn.procsl.ping.boot.infra.UserAutoConfiguration.domain_path;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@RequiredArgsConstructor
@EntityScan(basePackages = domain_path)
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = domain_path, bootstrapMode = BootstrapMode.LAZY)
public class UserAutoConfiguration {

    final static String domain_path = "cn.procsl.ping.boot.infra.domain";

}