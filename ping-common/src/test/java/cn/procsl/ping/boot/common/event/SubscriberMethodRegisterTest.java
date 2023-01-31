package cn.procsl.ping.boot.common.event;

import org.junit.jupiter.api.Test;

import javax.inject.Inject;

public class SubscriberMethodRegisterTest {

    @Inject
    EventBusBridge eventBusBridge;

    @Test
    public void resolver() {
        eventBusBridge.publisher("test", "发布事件");
    }
}