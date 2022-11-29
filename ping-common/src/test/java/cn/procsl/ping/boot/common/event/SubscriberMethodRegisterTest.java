package cn.procsl.ping.boot.common.event;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

public class SubscriberMethodRegisterTest {

    @Inject
    EventBusBridge eventBusBridge;

    @Test
    public void resolver() {
        eventBusBridge.publisher("test", "发布事件");
    }
}