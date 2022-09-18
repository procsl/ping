package cn.procsl.ping.boot.connect.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
final class HandlerInvoker {

    final Collection<Class<? extends Annotation>> classes;

    final ProxyArgumentResolverLoader resolver = new ProxyArgumentResolverLoader();

    HandlerInvoker(Collection<Class<? extends Annotation>> classes) {
        this.classes = new HashSet<>(classes);
    }

    public void invoke(SocketIOConnectContext context, Object[] args) {
        try {
            int count = context.getMethod().getParameterCount();
            if (count == 0) {
                context.invoke();
            }

            Object[] newArgs = new Object[count];
            Parameter[] parameters = context.getMethod().getParameters();
            for (int i = 0; i < parameters.length; i++) {
                newArgs[i] = this.resolver.resolveArgument(context, i, parameters[i], args);
            }
            context.invoke(newArgs);
        } catch (Exception e) {
            log.error("消息处理异常", e);
            throw new RuntimeException("消息处理错误", e);
        }
    }


    public List<List<Map<String, Object>>> findEventHandler(Object handler) {
        return this.classes.stream()
                           .map(annotation -> this.getMaps(handler, annotation))
                           .filter(item -> !item.isEmpty())
                           .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getMaps(Object handler, Class<? extends Annotation> target) {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(handler.getClass());
        return Arrays.stream(methods).map(method -> {
            Annotation annotation = AnnotationUtils.findAnnotation(method, target);
            if (annotation == null) {
                return null;
            }
            return Map.of("method", method, "handler", handler, "annotation", annotation);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


}
