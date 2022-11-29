package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.captcha.TestCaptchaApplication;
import cn.procsl.ping.boot.captcha.domain.VerifyFailureException;
import cn.procsl.ping.boot.captcha.domain.email.EmailCaptcha;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;

@Slf4j
@SpringBootTest(classes = TestCaptchaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EmailVerifyCaptchaInterceptorTest {

    @Inject
    EmailCaptchaHandler emailCaptchaHandler;

    @Inject
    JpaRepository<EmailCaptcha, Long> emailCaptchaLongJpaRepository;

    @Test
    public void verify() {

        {
            log.info("开始");
            EmailCaptcha emailCaptcha = new EmailCaptcha("123456", "test@email.com");
            emailCaptchaLongJpaRepository.save(emailCaptcha);
            emailCaptchaHandler.verify(new SimpleMockCaptchaCommand(emailCaptcha));
            Assertions.assertThrowsExactly(VerifyFailureException.class, () -> {
                emailCaptchaHandler.verify(new SimpleMockCaptchaCommand(emailCaptcha));
            });
            log.info("结束");
        }

        {
            EmailCaptcha emailCaptcha = new EmailCaptcha("654321", "test@email.com");
            Assertions.assertThrowsExactly(VerifyFailureException.class, () -> {
                emailCaptchaHandler.verify(new SimpleMockCaptchaCommand(emailCaptcha));
            });
        }
    }

    @Test
    public void createEmailCaptcha() {
        this.emailCaptchaHandler.createEmailCaptcha("123456", "test@email.com");
    }
}