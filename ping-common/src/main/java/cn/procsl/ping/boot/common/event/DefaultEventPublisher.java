package cn.procsl.ping.boot.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Slf4j
@Component
public class DefaultEventPublisher implements EventPublisher {

    @Override
    public void publish(String name, Serializable parameters) {
        log.info("发布事件:{}, 参数:{}", name, parameters);
    }

}
