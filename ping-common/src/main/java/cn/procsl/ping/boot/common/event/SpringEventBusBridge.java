package cn.procsl.ping.boot.common.event;

import cn.procsl.ping.boot.common.dto.ID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.io.Serializable;
import java.util.EventObject;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class SpringEventBusBridge implements EventBusBridge,
        ApplicationListener<SpringEventBusBridge.InnerListenerEvent> {

    final ApplicationContext context;

    final ConcurrentMap<String, ConcurrentLinkedQueue<Consumer<EventObject>>> tasks =
            new ConcurrentHashMap<>();

    @Override
    public String publisher(String name, Serializable parameters) {
        String eventId = UUID.randomUUID().toString().replaceAll("-", "");
        log.debug("开始发布事件:[{}] 名称:[{}], 参数:[{}]", eventId, name, parameters);
        context.publishEvent(new InnerListenerEvent(eventId, name, parameters));
        return eventId;
    }

    @Override
    public void subscriber(String name, Consumer<EventObject> consumer) {
        this.tasks.computeIfAbsent(name, s -> new ConcurrentLinkedQueue<>());
        final ConcurrentLinkedQueue<Consumer<EventObject>> callbacks = this.tasks.get(name);
        callbacks.add(consumer);
        log.info("注册事件订阅:{}", name);
    }

    @Override
    public void onApplicationEvent(@NonNull SpringEventBusBridge.InnerListenerEvent event) {

        final ConcurrentLinkedQueue<Consumer<EventObject>> callbacks = this.tasks.get(event.getName());
        if (callbacks == null) {
            log.trace("未找到事件:{}", event.getName());
            return;
        }
        log.debug("开始执行事件[{}], 名称:[{}], 任务数量:[{}]", event.getId(), event.getName(), callbacks.size());
        for (Consumer<EventObject> consumer : callbacks) {
            try {
                consumer.accept(event);
            } catch (Exception e) {
                log.error("执行:[{}]发生错误", event.getName(), e);
            }
        }
    }

    static final class InnerListenerEvent extends ApplicationEvent implements ID {

        @Getter
        private final String id;
        @Getter
        private final String name;

        public InnerListenerEvent(String id, String name, Object source) {
            super(source);
            this.id = id;
            this.name = name;
        }

    }

}
