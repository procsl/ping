package cn.procsl.ping.boot.common.event;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
class PublishMethodInterceptor implements MethodInterceptor {

    final EventPublisher eventPublisher;

    @Nullable
    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
//        AnnotationUtils.findAnnotation(invocation.getMethod(),invocation )

        Object target = invocation.getThis();
        if (target == null) {
            log.warn("找不到源对象");
            return invocation.proceed();
        }

        Class<?> clazz = AopUtils.getTargetClass(target);

        Method realTarget = AopUtils.getMostSpecificMethod(invocation.getMethod(), clazz);

        Publish publisher = AnnotationUtils.findAnnotation(realTarget, Publish.class);
        if (publisher == null) {
            log.warn("找不到 @Publish 注解");
            return invocation.proceed();
        }

        switch (publisher.trigger()) {
            case always:
                publish(publisher);
                return invocation.proceed();
            case complete:
                Object result = invocation.proceed();
                publish(publisher);
                return result;
            case error:
                try {
                    return invocation.proceed();
                } catch (Exception e) {
                    publish(publisher);
                    throw e;
                }
        }
        return invocation.proceed();
    }

    void publish(Publish publisher) {
        try {
            eventPublisher.publish(publisher.name(), publisher.parameter());
        } catch (Exception e) {
            log.error("事件发布出现错误:", e);
        }
    }

}
