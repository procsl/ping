package cn.procsl.ping.boot.admin;

import cn.procsl.ping.boot.admin.auth.access.FailureAuthenticationHandler;
import cn.procsl.ping.boot.admin.auth.access.HttpAuthorizationManager;
import cn.procsl.ping.boot.admin.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.admin.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.admin.domain.user.RoleSettingService;
import cn.procsl.ping.boot.admin.domain.user.UserRegisterService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.Map;

@Slf4j
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   WebApplicationContext webApplicationContext)
            throws Exception {

        val requests = http.authorizeHttpRequests();
        this.permitAllConfig(webApplicationContext, requests);
        requests.antMatchers("/h2/**", "**.js", "**.css", "**.html", "**.ico").permitAll();

        // 以下链接校验URL权限
        requests.antMatchers("/v1/**")
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
        http.headers().frameOptions().disable();
        return http.build();
    }

    void permitAllConfig(WebApplicationContext webApplicationContext,
                         AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry http) {
        Map<String, RequestMappingHandlerMapping> mappings = webApplicationContext.getBeansOfType(
                RequestMappingHandlerMapping.class);
        mappings.forEach((name, mapping) -> mapping.getHandlerMethods().forEach((k, v) -> {
            PermitAll permitAll = v.getMethodAnnotation(PermitAll.class);
            if (permitAll == null) {
                return;
            }
            this.permitAllProcess(k, v, http);
        }));
    }

    @SneakyThrows
    void permitAllProcess(RequestMappingInfo k, HandlerMethod v,
                          AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry http) {
        log.debug("Permit URL:{} :{}", k.getMethodsCondition().getMethods(), k.getDirectPaths());
        for (RequestMethod method : k.getMethodsCondition().getMethods()) {
            for (String path : k.getDirectPaths()) {
                // 这里需要判断动态链接
                http.antMatchers(HttpMethod.resolve(method.name()), path).permitAll();
            }
        }
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
