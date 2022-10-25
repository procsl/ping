package cn.procsl.ping.boot.admin;

import cn.procsl.ping.boot.admin.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.admin.domain.user.RoleSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@RequiredArgsConstructor
@EnableTransactionManagement
@ConditionalOnMissingBean({AdminAutoConfiguration.class})
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
                RoleSettingService.class,
        })
public class AdminAutoConfiguration {

}
