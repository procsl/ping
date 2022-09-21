package cn.procsl.ping.boot.common.advice;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMethodInterceptor<T extends Annotation> implements MethodInterceptor {

    final Class<T> annotationTag;

    @Nullable
    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {

        Object target = invocation.getThis();
        if (target == null) {
            log.warn("找不到源对象");
            return invocation.proceed();
        }

        Class<?> clazz = AopUtils.getTargetClass(target);

        Method realTarget = AopUtils.getMostSpecificMethod(invocation.getMethod(), clazz);

        T annotation = AnnotationUtils.findAnnotation(realTarget, annotationTag);
        if (annotation == null) {
            log.warn("找不到[{}]", annotationTag.getName());
            return invocation.proceed();
        }

        return this.doInvoke(annotation, invocation);
    }

    protected abstract Object doInvoke(T annotation, MethodInvocation invocation) throws Throwable;
}
