package cn.procsl.ping.boot.common.event;

import cn.procsl.ping.boot.common.TestCommonApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.inject.Inject;

@Slf4j
@SpringBootTest(classes = TestCommonApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PublisherPointcutTest {

    @Inject
    PublishComponent publishComponent;

    @Test
    public void matches() {
        publishComponent.callable();
        publishComponent.forLiPing("forever");
        publishComponent.forLiPing2("forever");
    }
}