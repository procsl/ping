package cn.procsl.ping.boot.common.event;

import cn.procsl.ping.boot.common.annotation.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

@Component
@Slf4j
public class PublishComponent {

    @Publisher(eventName = "test", parameter = "静态参数")
    public void callable() {
        log.info("被调用了");
    }

    @Publisher(eventName = "love", parameter = "#p0")
    public void forLiPing(String love) {
        log.info("ok:{}", love);
    }

    @Publisher(eventName = "love", parameter = "#p0 + ' ' + #return ")
    public String forLiPing2(String love) {
        log.info("ok:{}", love);
        return "me too";
    }

    @Publisher(eventName = "test", parameter = "#root[callback]?.get()")
    public void callable1() {
        log.info("被调用了2222");
    }


    @Component
    static class PublicAttrConfigure implements PublisherRootAttributeConfigure {

        @Override
        public Map<String, Object> getAttributes() {
            Supplier<String> getter = () -> "hello";
            return Map.of("callback", getter);
        }
    }

}
