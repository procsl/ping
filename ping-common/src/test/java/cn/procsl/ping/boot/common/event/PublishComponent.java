package cn.procsl.ping.boot.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class PublishComponent {

    @Transactional
    @Publish(name = "test", parameter = "静态参数")
    public void callable() {
        log.info("被调用了");
    }

}
