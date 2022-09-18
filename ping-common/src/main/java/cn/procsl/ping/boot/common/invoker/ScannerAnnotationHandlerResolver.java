package cn.procsl.ping.boot.common.invoker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ScannerAnnotationHandlerResolver
        implements
        HandlerResolver<AnnotationHandlerInvokerContext> {

    final Class<? extends Annotation> annotationTag;

    public ScannerAnnotationHandlerResolver(Class<? extends Annotation> annotationTag) {
        this.annotationTag = annotationTag;
    }


    @Override
    public Collection<AnnotationHandlerInvokerContext> resolve(Object handler) {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(handler.getClass());
        return Arrays.stream(methods).map(method -> {
                         Annotation annotation =
                                 AnnotationUtils.findAnnotation(method
                                         , annotationTag);
                         if (annotation == null) {
                             return null;
                         }
                         return new InnerAnnotationHandlerInvokerContext(annotation,
                                 handler,
                                 method);
                     })
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    @RequiredArgsConstructor
    @Getter
    final static class InnerAnnotationHandlerInvokerContext implements AnnotationHandlerInvokerContext {

        final Annotation annotation;

        final Object handler;

        final Method method;
    }
}
