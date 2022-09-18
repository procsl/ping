package cn.procsl.ping.boot.common.event;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

import java.lang.reflect.Method;

@Slf4j
class PublishPointcut extends StaticMethodMatcherPointcut {


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

        log.trace("matcher target class:{}", name);
        Method specMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        return specMethod.isAnnotationPresent(Publish.class);
    }
}
