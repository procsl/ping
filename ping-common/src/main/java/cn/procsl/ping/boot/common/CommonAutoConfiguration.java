package cn.procsl.ping.boot.common;

import cn.procsl.ping.boot.common.aop.AnnotationPointcutAdvisor;
import cn.procsl.ping.boot.common.bridge.EventBusBridge;
import cn.procsl.ping.boot.common.bridge.PublisherMethodInterceptor;
import cn.procsl.ping.boot.common.bridge.SpringEventBusBridge;
import cn.procsl.ping.boot.common.bridge.SubscriberMethodRegister;
import cn.procsl.ping.boot.common.utils.IdentifierGenerator;
import cn.procsl.ping.boot.common.utils.SimpleLongIdGenerator;
import cn.procsl.ping.boot.common.utils.TraceIdGenerator;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import javax.inject.Inject;
import javax.inject.Qualifier;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * &#064;date  2020/03/21
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
    public EventBusBridge eventBusBridge(ApplicationEventPublisher publisher,
                                         @Autowired(required = false) IdentifierGenerator<Long> generator) {
        if (generator == null) {
            generator = new SimpleLongIdGenerator();
        }
        return new SpringEventBusBridge(publisher, generator);
    }


}
