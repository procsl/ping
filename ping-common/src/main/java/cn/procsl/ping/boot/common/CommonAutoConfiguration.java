package cn.procsl.ping.boot.common;

import cn.procsl.ping.boot.common.aop.AnnotationPointcutAdvisor;
import cn.procsl.ping.boot.common.bridge.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;

import javax.inject.Provider;
import java.util.Collection;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@AutoConfiguration
@AutoConfigureOrder(Integer.MAX_VALUE)
@ConditionalOnMissingBean(CommonAutoConfiguration.class)
public class CommonAutoConfiguration {

    @Bean(name = "publishAnnotationPointcutAdvisor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static AnnotationPointcutAdvisor publishAnnotationPointcutAdvisor(ApplicationContext applicationContext) {
        PublisherMethodInterceptor interceptor = new PublisherMethodInterceptor(applicationContext);
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
