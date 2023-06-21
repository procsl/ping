package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.web.TestWebApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@Transactional
@Rollback
@SpringBootTest(classes = TestWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class DecryptConversionServiceTest {


    @Test
    public void test() {

    }

}