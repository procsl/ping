package cn.procsl.ping.boot.common;

import cn.procsl.ping.boot.common.advice.AnnotationPointcutAdvisor;
import cn.procsl.ping.boot.common.event.*;
import cn.procsl.ping.boot.common.utils.ContextHolder;
import cn.procsl.ping.boot.common.web.AccessLoggerFilter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.util.Collection;
import java.util.List;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Slf4j
@AutoConfiguration(before = {JpaBaseConfiguration.class, ErrorMvcAutoConfiguration.class})
@ConditionalOnMissingBean(CommonAutoConfiguration.class)
@EnableJpaRepositories(bootstrapMode = BootstrapMode.LAZY, basePackages = "cn.procsl.ping.boot.common.jpa")
@EntityScan(basePackages = "cn.procsl.ping.boot.common.jpa")
@ComponentScan(basePackages = "cn.procsl.ping.boot.common")
public class CommonAutoConfiguration implements ApplicationContextAware {

    public final static String SYSTEM_ERROR_CODE_KEY = "SYSTEM_ERROR_CODE_KEY";

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        ContextHolder.setApplicationContext(applicationContext);
    }

    @Bean("accessLoggerFilterBean")
    @ConditionalOnMissingBean(name = "accessLoggerFilterBean")
    public FilterRegistrationBean<AccessLoggerFilter> accessLoggerFilterFilterRegistrationBean() {
        FilterRegistrationBean<AccessLoggerFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new AccessLoggerFilter());
        filter.setName("accessLoggerFilter");
        filter.setOrder(Integer.MIN_VALUE);
        filter.setUrlPatterns(List.of("/*"));
        return filter;
    }


    @Bean(name = "publishAnnotationPointcutAdvisor")
    public AnnotationPointcutAdvisor publishAnnotationPointcutAdvisor(EventBusBridge eventBusBridge,
                                                                      @Autowired(required = false) Collection<PublisherRootAttributeConfigurer> configurers) {
        PublisherMethodInterceptor interceptor = new PublisherMethodInterceptor(eventBusBridge, configurers);
        return AnnotationPointcutAdvisor.forAnnotation(Publisher.class, interceptor);
    }

    @Bean
    public SubscriberMethodRegister subscribeMethodRegister(EventBusBridge eventBusBridge,
                                                            ApplicationContext context) {
        return new SubscriberMethodRegister(eventBusBridge, context);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventBusBridge eventBusBridge(ApplicationContext applicationContext) {
        return new SpringEventBusBridge(applicationContext);
    }

}
