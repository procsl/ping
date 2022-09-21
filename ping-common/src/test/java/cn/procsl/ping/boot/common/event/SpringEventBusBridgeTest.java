package cn.procsl.ping.boot.common.event;

import cn.procsl.ping.boot.common.TestCommonApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.inject.Inject;

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
        eventBusBridge.publisher("测试", "参数1, 参数2");
        eventBusBridge.publisher("test", "来自注解的参数");
    }

    @Test
    @Order(0)
    public void subscribe() {
        this.eventBusBridge.subscribe("测试", serializable -> {
            log.info("成功执行了:{}", serializable);
        });
    }
}