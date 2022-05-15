package cn.procsl.ping.boot.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static cn.procsl.ping.boot.infra.UserAutoConfiguration.base_path;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@RequiredArgsConstructor
@EntityScan(basePackages = {base_path + "user", base_path + "account", base_path + "rbac", base_path + "conf"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {base_path + "user", base_path + "account", base_path + "rbac", base_path + "conf"}, bootstrapMode = BootstrapMode.LAZY)
public class UserAutoConfiguration {

    final static String base_path = "cn.procsl.ping.boot.infra.";


}
