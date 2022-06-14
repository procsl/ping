package cn.procsl.ping.admin.config;

import cn.procsl.ping.admin.config.security.*;
import com.querydsl.jpa.Hibernate5Templates;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.hibernate.HibernateQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.List;

import static cn.procsl.ping.admin.config.AdminConfigurer.*;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL})
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
@OpenAPIDefinition(info = @Info(title = "接口文档", version = "1.0", license = @License(url = apache, name = name), description = desc))
public class AdminConfigurer extends WebSecurityConfigurerAdapter {

    final static String name = "Apache License 2.0";

    final static String desc = "Ping接口参考文档, 可用于指导生成测试用例, HTTP Client SDK";

    final static String apache = "https://www.apache.org/licenses/LICENSE-2.0";

    final static String[] apis = new String[]{"/v3/api-docs*.**", "/v3/api-docs/**", "/v1/**", "/swagger-ui/index.html"};

    final ApplicationContext context;

    @Bean
    public JPQLQueryFactory jpaQueryFactory(@Autowired EntityManager entityManager) {

        try {
            return new HibernateQueryFactory(Hibernate5Templates.DEFAULT, () -> entityManager.unwrap(Session.class));
        } catch (PersistenceException e) {
            return new JPAQueryFactory(entityManager);
        }

    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/h2/**", "/static/**", "/public/**", "/favicon.ico");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        val provider = this.context.getBean(UserAuthenticationProvider.class);
        auth.authenticationProvider(provider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        val dynamicAuthorizationManager = context.getBean(DynamicAuthorizationManager.class);

        http.csrf().disable();

        // 动态权限相关
        http.authorizeHttpRequests().antMatchers(apis).access(dynamicAuthorizationManager);

        // 需要认证的接口
        http.authorizeRequests().antMatchers(apis).authenticated();

        // 无权限处理
        http.exceptionHandling().accessDeniedHandler(context.getBean(PermissionAccessDeniedHandler.class));

        // 登录认证
        http.formLogin(custom -> {
            custom.loginProcessingUrl("/v1/auth");
            custom.failureUrl("/index.html");
            custom.successHandler(context.getBean(LoginSuccessfulHandler.class));
            custom.failureHandler(context.getBean(LoginFailureHandler.class));
        });

        // 注销登录
        http.logout(custom -> {
            custom.addLogoutHandler((request, response, authentication) -> log.info("注销登录!"));
            custom.clearAuthentication(true);
            custom.invalidateHttpSession(true);
            custom.logoutUrl("/logout");
        });

    }


    @Bean
    public SimpleUrlHandlerMapping customFaviconHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Integer.MIN_VALUE);
        mapping.setUrlMap(Collections.singletonMap("/favicon.ico", faviconRequestHandler()));
        return mapping;
    }

    @Bean
    protected ResourceHttpRequestHandler faviconRequestHandler() {
        ResourceHttpRequestHandler requestHandler = new ResourceHttpRequestHandler();
        ClassPathResource classPathResource = new ClassPathResource("/static");
        List<Resource> locations = List.of(classPathResource);
        requestHandler.setLocations(locations);
        return requestHandler;
    }

}
