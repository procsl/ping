package cn.procsl.ping.boot.admin;

import cn.procsl.ping.boot.admin.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.admin.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.admin.domain.user.UserRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static cn.procsl.ping.boot.admin.AdminAutoConfiguration.domain_path;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean({AdminAutoConfiguration.class})
@RequiredArgsConstructor
@EntityScan(basePackages = domain_path)
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = domain_path, bootstrapMode = BootstrapMode.LAZY)
@ComponentScan(
        basePackages = {"cn.procsl.ping.boot.base.adapter"},
        basePackageClasses = {
                AccessControlService.class,
                UserRegisterService.class,
                ConfigOptionService.class
        })
public class AdminAutoConfiguration {

    final static String domain_path = "cn.procsl.ping.boot.base.domain";

}
