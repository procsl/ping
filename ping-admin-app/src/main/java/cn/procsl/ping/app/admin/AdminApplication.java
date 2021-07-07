package cn.procsl.ping.app.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * starter
 *
 * @author procsl
 * @date 2020/04/26
 */
@SpringBootApplication(scanBasePackages = "cn.procsl.ping")
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

}
