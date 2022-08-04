package cn.procsl.ping.boot.captcha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(scanBasePackages = "cn.procsl.ping")
public class TestCaptchaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestCaptchaApplication.class, args);
    }

}
