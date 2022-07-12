package cn.procsl.ping.boot.admin;

import cn.procsl.ping.boot.admin.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.admin.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.admin.domain.user.RoleSettingService;
import cn.procsl.ping.boot.admin.domain.user.UserRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean({AdminAutoConfiguration.class})
@RequiredArgsConstructor
@EnableTransactionManagement
@EntityScan(basePackages = "cn.procsl.ping.boot.admin.domain")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.admin.domain", bootstrapMode = BootstrapMode.LAZY)
@ComponentScan(basePackages = {
        "cn.procsl.ping.boot.admin.web",
        "cn.procsl.ping.boot.admin.service",
        "cn.procsl.ping.boot.admin.listener",
        "cn.procsl.ping.boot.admin.adapter"
},
        basePackageClasses = {
                ConfigOptionService.class,
                AccessControlService.class,
                RoleSettingService.class,
                UserRegisterService.class
        }

)
public class AdminAutoConfiguration {


}
