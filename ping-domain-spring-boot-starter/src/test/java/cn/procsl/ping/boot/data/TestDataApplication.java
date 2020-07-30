package cn.procsl.ping.boot.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(scanBasePackages = "cn.procsl.ping.boot.data")
public class TestDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestDataApplication.class, args);
    }

}
