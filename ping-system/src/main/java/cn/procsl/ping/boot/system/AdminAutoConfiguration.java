package cn.procsl.ping.boot.system;

import cn.procsl.ping.boot.system.auth.login.AuthenticateInterceptor;
import cn.procsl.ping.boot.system.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.system.domain.user.RoleSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@RequiredArgsConstructor
@EnableTransactionManagement
@ConditionalOnMissingBean({AdminAutoConfiguration.class})
@EntityScan(basePackages = "cn.procsl.ping.boot.system.domain")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.system.domain", bootstrapMode = BootstrapMode.LAZY)
@ComponentScan(basePackages = {
        "cn.procsl.ping.boot.system.web",
        "cn.procsl.ping.boot.system.service",
        "cn.procsl.ping.boot.system.listener",
        "cn.procsl.ping.boot.system.adapter",
        "cn.procsl.ping.boot.system.auth"
},
        basePackageClasses = {
                ConfigOptionService.class,
                RoleSettingService.class,
        })
@Order
public class AdminAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticateInterceptor());
    }

}
