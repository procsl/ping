package cn.procsl.ping.boot.common.event;

import cn.procsl.ping.boot.common.TestCommonApplication;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = TestCommonApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SpringEventBusBridgeTest {

    @Inject
    EventBusBridge eventBusBridge;

    @BeforeEach
    public void setUp() {
        log.info("开始执行");
    }

    @Test
    @Order(1)
    public void publisher() {
        String id = eventBusBridge.publisher("测试", "参数1, 参数2");
        log.info("事件ID:{}", id);
        id = eventBusBridge.publisher("test", "来自注解的参数");
        log.info("事件ID:{}", id);
    }

    @Test
    @Order(0)
    public void subscribe() {
        this.eventBusBridge.subscriber("测试", serializable -> {
            log.info("成功执行了:{}", serializable);
        });
    }
}