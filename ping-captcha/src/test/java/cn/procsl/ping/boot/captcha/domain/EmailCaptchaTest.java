package cn.procsl.ping.boot.captcha.domain;

import cn.procsl.ping.boot.captcha.domain.email.EmailCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static cn.procsl.ping.boot.captcha.domain.Captcha.number_chars;
import static cn.procsl.ping.boot.captcha.domain.Captcha.random;

@Slf4j
public class EmailCaptchaTest {

    final static String char2 = "ABCDEFJHIJKLMNOPQRSTUVWXYZ";

    final protected static char[] chars = (char2 + "abcdefjhijklmnopqrstuvwxyz0123456789").toCharArray();

    @RepeatedTest(50)
    public void randomTest() {
        log.info(random(1, number_chars));
        log.info(random(2, number_chars));
        log.info(random(6, number_chars));
        log.info(random(16, chars));
        log.info(random(32, chars));
    }

    @RepeatedTest(50)
    public void randomTest32() {
        log.info(random(32, chars));
        log.info(random(6, number_chars));
        log.info(random(32, char2.toCharArray()));
    }

    @RepeatedTest(50)
    public void randomTest16() {
        log.info(random(16, chars));
        log.info(random(6, number_chars));
        log.info(random(16, char2.toCharArray()));
    }

    @Test
    public void check() {

        EmailCaptcha captcha = new EmailCaptcha("123", "test@email.com");
        log.info("邮箱验证码:{}", captcha);
        Assertions.assertNotNull(captcha.getTicket());
        Assertions.assertThrowsExactly(VerifyFailureException.class, () -> captcha.verify("123456"));

        captcha.verify(captcha.ticket);
        captcha.verify(captcha.ticket);
        Assertions.assertThrowsExactly(VerifyFailureException.class, () -> captcha.verify(captcha.ticket));

    }
}