package cn.procsl.ping.boot.admin;

import cn.procsl.ping.boot.admin.auth.access.FailureAuthenticationHandler;
import cn.procsl.ping.boot.admin.auth.access.HttpAuthorizationManager;
import cn.procsl.ping.boot.admin.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.admin.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.admin.domain.user.RoleSettingService;
import cn.procsl.ping.boot.admin.domain.user.UserRegisterService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
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
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableTransactionManagement
@ConditionalOnMissingBean({AdminAutoConfiguration.class})
@EntityScan(basePackages = "cn.procsl.ping.boot.admin.domain")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.admin.domain", bootstrapMode = BootstrapMode.LAZY)
@ComponentScan(basePackages = {"cn.procsl.ping.boot.admin.web", "cn.procsl.ping.boot.admin.service", "cn.procsl.ping" +
        ".boot.admin.listener", "cn.procsl.ping.boot.admin.adapter"}, basePackageClasses = {ConfigOptionService.class
        , AccessControlService.class, RoleSettingService.class, UserRegisterService.class})
public class AdminAutoConfiguration implements ApplicationContextAware {

    protected ApplicationContext context;


    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 以下链接放行
        http.authorizeHttpRequests()
            .antMatchers(POST, "/v1/session")
            .permitAll();

        http.authorizeHttpRequests()
            .antMatchers(GET, "/v1/session")
            .authenticated()
            .antMatchers(DELETE, "/v1/session")
            .authenticated();

        // 以下链接校验URL权限
        http.authorizeHttpRequests()
            .antMatchers("/v1/**")
            .access(new HttpAuthorizationManager());

        // 无权限处理
        FailureAuthenticationHandler handler = this.context.getBean(FailureAuthenticationHandler.class);
        http.exceptionHandling().authenticationEntryPoint(handler).accessDeniedHandler(handler);

        http.rememberMe().useSecureCookie(true)
            .authenticationSuccessHandler((request, response, authentication) -> log.info("RememberMe 登录"));

        http.logout().disable();
        http.csrf().disable();
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

    @Bean
    @ConditionalOnMissingBean
    public CompositeLogoutHandler compositeLogoutHandler() {
        return new CompositeLogoutHandler(List.of(new SecurityContextLogoutHandler()));
    }

}
