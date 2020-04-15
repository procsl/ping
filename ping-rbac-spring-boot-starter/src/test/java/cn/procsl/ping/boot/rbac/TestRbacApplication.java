package cn.procsl.ping.boot.rbac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(scanBasePackages = "cn.procsl.ping.boot")
public class TestRbacApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestRbacApplication.class, args);
    }

}
