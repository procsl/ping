package cn.procsl.ping.boot.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(scanBasePackages = "cn.procsl.ping.boot")
public class TestUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestUserApplication.class, args);
    }

}
