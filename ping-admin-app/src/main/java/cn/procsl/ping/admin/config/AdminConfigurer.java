package cn.procsl.ping.admin.config;

import cn.procsl.ping.admin.config.security.BasicAuthAuthenticationProvider;
import cn.procsl.ping.admin.config.security.DynamicAuthorizationManager;
import cn.procsl.ping.admin.config.security.NoBasicAuthentication;
import cn.procsl.ping.admin.config.security.PermissionAccessDeniedHandler;
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
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import static cn.procsl.ping.admin.config.AdminConfigurer.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL})
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
@OpenAPIDefinition(info = @Info(title = "接口文档", version = "1.0", license = @License(url = apache, name = name), description = desc))
public class AdminConfigurer extends WebSecurityConfigurerAdapter {

    final static String name = "Apache License 2.0";

    final static String desc = "Ping接口参考文档, 可用于指导生成测试用例, HTTP Client SDK";

    final static String apache = "https://www.apache.org/licenses/LICENSE-2.0";

    final static String[] apis = new String[]{"/v3/api-docs*.**", "/v3/api-docs/**", "/v1/**"};

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
    protected void configure(HttpSecurity http) throws Exception {

        val dynamicAuthorizationManager = context.getBean(DynamicAuthorizationManager.class);
        http.csrf().disable().authorizeHttpRequests().antMatchers(apis).access(dynamicAuthorizationManager);

        http.authorizeRequests().antMatchers(apis).authenticated().and().httpBasic(custom ->
                custom.authenticationEntryPoint(context.getBean(NoBasicAuthentication.class))
        );

        http.exceptionHandling().accessDeniedHandler(context.getBean(PermissionAccessDeniedHandler.class));
        http.authenticationProvider(this.context.getBean(BasicAuthAuthenticationProvider.class));
    }
}
