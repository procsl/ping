package cn.procsl.ping.boot.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Supplier;

@Component
@Slf4j
public class PublishComponent {

    @Transactional
    @Publisher(name = "test", parameter = "静态参数")
    public void callable() {
        log.info("被调用了");
    }

    @Publisher(name = "love", parameter = "#p0")
    public void forLiPing(String love) {
        log.info("ok:{}", love);
    }

    @Publisher(name = "love", parameter = "#p0 + ' ' + #return ")
    public String forLiPing2(String love) {
        log.info("ok:{}", love);
        return "me too";
    }

    @Publisher(name = "test", parameter = "#root[callback]?.get()")
    public void callable1() {
        log.info("被调用了2222");
    }


    @Component
    static class PublicAttrConfigure implements PublisherRootAttributeRegistry {

        @Override
        public Map<String, Object> getAttributes() {
            Supplier<String> getter = () -> "hello";
            return Map.of("callback", getter);
        }
    }

}
