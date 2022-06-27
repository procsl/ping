package cn.procsl.ping.boot.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(scanBasePackages = {"cn.procsl.ping"}, exclude = ErrorMvcAutoConfiguration.class)
public class TestBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestBaseApplication.class, args);
    }

}
