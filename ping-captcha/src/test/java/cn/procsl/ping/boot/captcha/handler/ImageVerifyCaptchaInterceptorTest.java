package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.captcha.TestCaptchaApplication;
import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptchaBuilderService;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptchaRepository;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@SpringBootTest(classes = TestCaptchaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ImageVerifyCaptchaInterceptorTest {

    ImageVerifyCaptchaHandler imageVerifyCaptcha;

    @Inject
    ImageCaptchaRepository imageCaptchaRepository;

    @BeforeEach
    void setUp() {
        this.imageVerifyCaptcha = new ImageVerifyCaptchaHandler(imageCaptchaRepository);
        log.info("开始执行测试");
    }

    @Test
    public void verify() {
        log.info("执行方法");
        ImageCaptcha captcha = new ImageCaptcha(1L, "123", "234567", 2);
        ImageVerifyCommand successContext = new MockVerifyCommand(captcha, null);
        ImageVerifyCommand errorContext = new MockVerifyCommand(captcha, "0980980");

        Assertions.assertThrowsExactly(VerifyFailureException.class, () -> imageVerifyCaptcha.verify(errorContext));

        imageVerifyCaptcha.verify(successContext);
        Assertions.assertThrowsExactly(VerifyFailureException.class, () -> imageVerifyCaptcha.verify(successContext));
        log.info("结束执行方法");
    }

    static class MockVerifyCommand extends ImageVerifyCommand {
        final ImageCaptchaBuilderService service = new ImageCaptchaBuilderService();
        private final ImageCaptcha captcha;
        private final String token;
        private final String ticket;

        public MockVerifyCommand(ImageCaptcha captcha, String ticket) {
            super(null);
            this.captcha = captcha;
            this.token = service.buildToken(this.key(), captcha);
            if (ticket == null) {
                this.ticket = this.captcha.getTicket();
            } else {
                this.ticket = ticket;
            }
        }

        @Override
        public String target() {
            return this.captcha.getTarget();
        }

        @Override
        public String ticket() {
            return Base64.getEncoder().encodeToString(ticket.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String token() {
            return this.token;
        }

    }


}