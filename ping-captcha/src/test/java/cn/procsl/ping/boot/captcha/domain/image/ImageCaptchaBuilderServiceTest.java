package cn.procsl.ping.boot.captcha.domain.image;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Slf4j
public class ImageCaptchaBuilderServiceTest {

    final ImageCaptchaBuilderService imageCaptchaBuilderService = new ImageCaptchaBuilderService();


    @Test
    public void buildToken() {
        String res = imageCaptchaBuilderService.buildToken("12345", new ImageCaptcha(1L, "测试", "abcdefg", 2));
        log.info("res {}", res);
    }

    @Test
    public void buildForToken() throws IOException {
        String token = "ss9qf2RRoiwyQYtfKR6HTTd3mXY9lFbGad0D4hoqEvkwTAuDOMCfh30b42Yje1kCWabWPnAm/+Kb4" +
                "/86Ds6KTYDfor9hOSKO9l2IB1JIlidNqz1j0921Atr48sPY1xASEmhd4fiIgAzjUU+dULPdhw==";
        ImageCaptcha captcha = imageCaptchaBuilderService.buildForToken("12345", token);
        log.info("res {}", captcha);
    }

    @Test
    public void secondExpired() {
        ImageCaptcha image = new ImageCaptcha(123L, "123", "ticket", 2);
        int second = image.validSecond();
        log.info("second:{}", second);
    }

}