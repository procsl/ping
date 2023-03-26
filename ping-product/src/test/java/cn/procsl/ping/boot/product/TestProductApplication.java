package cn.procsl.ping.boot.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author procsl
 * @date 2023年3月15日
 */
@SpringBootApplication(scanBasePackages = "cn.procsl.ping.boot.product")
public class TestProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestProductApplication.class, args);
    }

}
