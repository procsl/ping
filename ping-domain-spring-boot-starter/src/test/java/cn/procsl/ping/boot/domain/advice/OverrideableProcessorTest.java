package cn.procsl.ping.boot.domain.advice;

import cn.procsl.ping.boot.domain.DomainApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@SpringBootTest(classes = DomainApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Rollback
@Transactional
@Configuration
public class OverrideableProcessorTest {

    @Autowired
    AdviceTest test;

    public void test() {
        test.call();
    }
}