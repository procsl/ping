package cn.procsl.ping.boot.common.advice;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
final class AnnotationPointcut extends StaticMethodMatcherPointcut {

    final Class<? extends Annotation> target;

    @Override
    public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
        String name = targetClass.getName();
        if (name.startsWith("org.springframework")) {
            return false;
        }
        if (name.startsWith("com.zaxxer.hikari")) {
            return false;
        }
        if (name.startsWith("org.hibernate")) {
            return false;
        }
        if (name.startsWith("java.")) {
            return false;
        }
        if (name.startsWith("org.apache.")) {
            return false;
        }
        if (name.startsWith("javax.")) {
            return false;
        }
        if (name.startsWith("jakarta.")) {
           return false;
        }
        if (name.startsWith("jdk.")) {
            return false;
        }
        if (name.startsWith("com.querydsl")) {
            return false;
        }
        if (name.startsWith("org.springdoc")) {
            return false;
        }
        if (name.startsWith("com.fasterxml")) {
            return false;
        }
        if (name.startsWith("com.sun")) {
            return false;
        }
        if (name.startsWith("io.micrometer")) {
            return false;
        }

        log.trace("matcher target class:{}", name);
        Method specMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        return specMethod.isAnnotationPresent(target);
    }
}
