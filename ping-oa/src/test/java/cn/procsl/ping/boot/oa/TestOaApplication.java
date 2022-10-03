package cn.procsl.ping.boot.oa;

import cn.procsl.ping.boot.captcha.CaptchaAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(exclude = CaptchaAutoConfiguration.class)
public class TestOaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestOaApplication.class, args);
    }

}
