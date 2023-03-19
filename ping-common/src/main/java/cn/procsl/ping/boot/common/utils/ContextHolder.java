package cn.procsl.ping.boot.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContextHolder {

    static final String lock = "lock";
    private static EntityManager entityManager;

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static synchronized void setApplicationContext(ApplicationContext context) throws BeansException {
        if (context == null) {
            return;
        }
        applicationContext = context;
    }

    public static boolean isInjected() {
        return applicationContext != null;
    }

    public static EntityManager getEntityManager() {

        if (entityManager != null) {
            return entityManager;
        }

        if (!isInjected()) {
            return null;
        }

        synchronized (lock) {
            EntityManagerFactory factory = ContextHolder.getApplicationContext().getBean(EntityManagerFactory.class);
            ContextHolder.entityManager = factory.createEntityManager();
        }
        return entityManager;
    }
}
