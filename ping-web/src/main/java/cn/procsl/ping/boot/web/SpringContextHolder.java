package cn.procsl.ping.boot.web;

import org.springframework.context.ApplicationContext;

public final class SpringContextHolder {

    static ApplicationContext applicationContext;

    public static ApplicationContext getContext() {
        return applicationContext;
    }

    synchronized static void setContext(ApplicationContext applicationContext) {
        if (SpringContextHolder.applicationContext == null) {
            SpringContextHolder.applicationContext = applicationContext;
        }
    }

}
