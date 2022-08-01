package cn.procsl.ping.boot.captcha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(scanBasePackages = {"cn.procsl.ping"}, exclude = ErrorMvcAutoConfiguration.class)
public class TestCaptchaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestCaptchaApplication.class, args);
    }

}
