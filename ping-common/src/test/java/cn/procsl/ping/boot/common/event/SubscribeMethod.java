package cn.procsl.ping.boot.common.event;

import cn.procsl.ping.boot.common.annotation.Subscriber;
import cn.procsl.ping.boot.common.annotation.SubscriberRegister;
import lombok.extern.slf4j.Slf4j;

import java.util.EventObject;

@Slf4j
@SubscriberRegister
public class SubscribeMethod {

    @Subscriber(name = "test")
    public void onEvent(@Subscriber.EventId String id) {
        log.info("方法被调用:{}", id);
    }

    @Subscriber(name = "test")
    public void onEvent1() {
        log.info("方法被调用1");
    }

    @Subscriber(name = "test")
    public void onEvent2(EventObject eventObject) {
        log.info("方法被调用2:{}", eventObject);
    }

    @Subscriber(name = "love")
    public void onEvent3(String parameters) {
        log.info("接受:{}", parameters);
    }

    @Subscriber(name = "test")
    public void onEvent4(Long parameters) {
        log.info("方法被调用4:{}", parameters);
    }

    @Subscriber(name = "test")
    public void onEvent5() {
        log.info("方法被调用5");
        throw new RuntimeException("test");
    }

}
