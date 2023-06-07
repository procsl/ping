package cn.procsl.ping.boot.common;

import cn.procsl.ping.boot.common.advice.AnnotationPointcutAdvisor;
import cn.procsl.ping.boot.common.error.CommonErrorAttributes;
import cn.procsl.ping.boot.common.event.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Collection;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@AutoConfiguration(before = {JpaBaseConfiguration.class, ErrorMvcAutoConfiguration.class})
@ConditionalOnMissingBean(CommonAutoConfiguration.class)
public class CommonAutoConfiguration {

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

    @Bean
    public CommonErrorAttributes errorAttributes() {
        return new CommonErrorAttributes();
    }


}
