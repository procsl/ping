package cn.procsl.ping.boot.common.event;

import cn.procsl.ping.boot.common.annotation.Subscriber;
import cn.procsl.ping.boot.common.annotation.SubscriberRegister;
import cn.procsl.ping.boot.common.annotation.Subscribers;
import cn.procsl.ping.boot.common.invoker.AnnotationHandlerInvokerContext;
import cn.procsl.ping.boot.common.invoker.ScannerAnnotationHandlerResolver;
import cn.procsl.ping.boot.common.invoker.SimpleHandlerInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Map;

@Slf4j
public final class SubscriberMethodRegister implements SmartInitializingSingleton {

    final EventBusBridge eventBusBridge;

    final ApplicationContext applicationContext;
    final ArgumentResolverLoader resolver;


    public SubscriberMethodRegister(EventBusBridge eventBusBridge, ApplicationContext applicationContext) {
        this.eventBusBridge = eventBusBridge;
        this.applicationContext = applicationContext;
        this.resolver = new ArgumentResolverLoader(applicationContext);
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, Object> annotations;
        try {
            annotations = applicationContext.getBeansWithAnnotation(SubscriberRegister.class);
        } catch (BeansException e) {
            log.warn("找不到:[{}]", SubscriberRegister.class.getName());
            return;
        }

        Collection<Object> beans = annotations.values();
        ScannerAnnotationHandlerResolver scanner = new ScannerAnnotationHandlerResolver(Subscribers.class);
        ScannerAnnotationHandlerResolver scanner1 = new ScannerAnnotationHandlerResolver(Subscriber.class);
        for (Object bean : beans) {
            resolver(scanner, scanner1, bean);
        }

        this.resolver.load();

    }

    void resolver(ScannerAnnotationHandlerResolver scanner,
                  ScannerAnnotationHandlerResolver scanner1,
                  Object bean) {
        Collection<AnnotationHandlerInvokerContext> contexts = scanner.resolve(bean);
        contexts.stream()
                .map(item -> new SimpleHandlerInvoker<>(item, this.resolver))
                .forEach(item -> {
                    Subscribers annotation = item.getContext().getAnnotation(Subscribers.class);
                    if (annotation.value() == null || annotation.value().length == 0) {
                        return;
                    }
                    for (Subscriber subscriber : annotation.value()) {
                        register(item, subscriber);
                    }
                });

        Collection<AnnotationHandlerInvokerContext> context2 = scanner1.resolve(bean);
        context2.stream()
                .map(item -> new SimpleHandlerInvoker<>(item, this.resolver))
                .forEach(item -> {
                    Subscriber annotation = item.getContext().getAnnotation(Subscriber.class);
                    register(item, annotation);
                });

    }

    private void register(SimpleHandlerInvoker<AnnotationHandlerInvokerContext> item, Subscriber subscriber) {
        log.debug("注册:[{}]订阅 -> [{}#{}]", subscriber.name(),
                item.getContext().getHandler().getClass(),
                item.getContext().getMethod().getName());
        this.eventBusBridge.subscriber(subscriber.name(), item::invoke);
    }

}
