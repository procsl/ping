package cn.procsl.ping.boot.captcha.domain.image;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

@Slf4j
public class ImageCaptchaServiceTest {

    final ImageCaptchaService imageCaptchaService = new ImageCaptchaService();

    @Test
    public void generated() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageCaptcha res = imageCaptchaService.generated("123", os);
        log.info("image captcha:{}", res);
    }


    @Test
    public void buildToken() {
        String res = imageCaptchaService.buildToken("12345", new ImageCaptcha("测试", "abcdefg", 2));
        log.info("res {}", res);
    }

    @Test
    public void buildForToken() {
        String token = "ss9qf2RRoiwyQYtfKR6HTTd3mXY9lFbGad0D4hoqEvkwTAuDOMCfh30b42Yje1kCWabWPnAm/+Kb4" +
                "/86Ds6KTYDfor9hOSKO9l2IB1JIlidNqz1j0921Atr48sPY1xASEmhd4fiIgAzjUU+dULPdhw==";
        ImageCaptcha captcha = imageCaptchaService.buildForToken("12345", token);
        log.info("res {}", captcha);
    }
}