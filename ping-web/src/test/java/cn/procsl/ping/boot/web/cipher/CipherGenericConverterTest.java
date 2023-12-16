package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.web.TestWebApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@SpringBootTest(classes = TestWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CipherGenericConverterTest {


    @Test
    public void test() {

    }

}