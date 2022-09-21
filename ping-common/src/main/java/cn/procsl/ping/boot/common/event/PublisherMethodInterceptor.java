package cn.procsl.ping.boot.common.event;


import cn.procsl.ping.boot.common.advice.AbstractMethodInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.event.EventListener;

@Slf4j
public final class PublisherMethodInterceptor extends AbstractMethodInterceptor<Publisher> {

    final EventBusBridge eventBusBridge;

    public PublisherMethodInterceptor(EventBusBridge eventBusBridge) {
        super(Publisher.class);
        this.eventBusBridge = eventBusBridge;
    }

    @Override
    @EventListener
    protected Object doInvoke(Publisher publisher, MethodInvocation invocation) throws Throwable {
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

    void publish(Publisher publisher) {
        try {
            eventBusBridge.publisher(publisher.name(), publisher.parameter());
        } catch (Exception e) {
            log.error("事件发布出现错误:", e);
        }
    }

}
