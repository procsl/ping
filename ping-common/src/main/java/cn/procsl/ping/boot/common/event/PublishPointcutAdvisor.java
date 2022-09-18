package cn.procsl.ping.boot.common.event;

import lombok.NonNull;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.stereotype.Component;

@Component
public class PublishPointcutAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    final PublishPointcut pointcut = new PublishPointcut();

    public PublishPointcutAdvisor(EventPublisher eventPublisher) {
        setAdvice(new PublishMethodInterceptor(eventPublisher));
    }

    @Override
    public @NonNull Pointcut getPointcut() {
        return pointcut;
    }

}
