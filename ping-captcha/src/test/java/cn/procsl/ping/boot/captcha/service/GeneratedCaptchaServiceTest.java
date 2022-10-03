package cn.procsl.ping.boot.captcha.service;

import cn.procsl.ping.boot.captcha.TestCaptchaApplication;
import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = TestCaptchaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GeneratedCaptchaServiceTest {

    @Inject
    GeneratedCaptchaService generatedCaptchaService;

    @Test
    public void generated() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        generatedCaptchaService.generated("11", CaptchaType.image, os);
    }
}