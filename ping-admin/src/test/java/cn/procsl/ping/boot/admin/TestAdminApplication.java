package cn.procsl.ping.boot.admin;

import cn.procsl.ping.boot.captcha.CaptchaAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(exclude = {
        CaptchaAutoConfiguration.class,
        ErrorMvcAutoConfiguration.class
})
public class TestAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestAdminApplication.class, args);
    }

}
