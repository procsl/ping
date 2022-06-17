package cn.procsl.ping.boot.domain.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdviceTest implements AdviceInterface {

    @Override
    @Overrideable(target = AdviceInterface.class)
    public void call() {
        log.info("进入advice");
        throw new IllegalStateException("测试失败");
    }

}
