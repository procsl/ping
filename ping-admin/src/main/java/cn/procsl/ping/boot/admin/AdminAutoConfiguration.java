package cn.procsl.ping.boot.admin;

import cn.procsl.ping.boot.admin.auth.DynamicAuthorizationManager;
import cn.procsl.ping.boot.admin.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.admin.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.admin.domain.user.RoleSettingService;
import cn.procsl.ping.boot.admin.domain.user.UserRegisterService;
import cn.procsl.ping.boot.common.web.ResponseUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableTransactionManagement
@ConditionalOnMissingBean({AdminAutoConfiguration.class})
@EntityScan(basePackages = "cn.procsl.ping.boot.admin.domain")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.admin.domain", bootstrapMode = BootstrapMode.LAZY)
@ComponentScan(basePackages = {
        "cn.procsl.ping.boot.admin.web",
        "cn.procsl.ping.boot.admin.service",
        "cn.procsl.ping.boot.admin.listener",
        "cn.procsl.ping.boot.admin.adapter"},
        basePackageClasses = {
                ConfigOptionService.class,
                AccessControlService.class,
                RoleSettingService.class,
                UserRegisterService.class
        }
)
public class AdminAutoConfiguration implements ApplicationContextAware {

    protected ApplicationContext context;

    @Value("${server.error.path:/error}")
    String error;

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        val dynamicAuthorizationManager = this.context
                .getBean(DynamicAuthorizationManager.class);

        http.csrf().disable();

        // 动态权限相关
        http.authorizeHttpRequests()
            .antMatchers("/v1/session")
            .permitAll()
            .antMatchers("/v1/*")
            .access(dynamicAuthorizationManager);

        // 无权限处理
        http.exceptionHandling()
            .authenticationEntryPoint(
                    (request, response, authException) ->
                            ResponseUtils.unauthorizedError(request, response,
                                    error, "401001", "你尚未登录,请登录"))
            .accessDeniedHandler(
                    (request, response, accessDeniedException) -> ResponseUtils.forbiddenError(request, response, error,
                            "403001", "无权限,拒绝访问")
            );

        http.formLogin().disable();
        http.httpBasic().disable();
        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder,
                                                               UserDetailsService userDetailsService) {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
        dao.setPasswordEncoder(passwordEncoder);
        dao.setUserDetailsService(userDetailsService);
        dao.setHideUserNotFoundExceptions(true);
        return dao;
    }

}
