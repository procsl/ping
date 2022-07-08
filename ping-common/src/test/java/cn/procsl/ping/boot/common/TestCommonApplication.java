package cn.procsl.ping.boot.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(scanBasePackages = "cn.procsl.ping.boot.domain")
public class TestCommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestCommonApplication.class, args);
    }


}
