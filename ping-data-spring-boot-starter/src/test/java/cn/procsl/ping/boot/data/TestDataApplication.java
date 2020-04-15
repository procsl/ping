package cn.procsl.ping.boot.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication
@ComponentScan("cn.procsl.ping.boot.data.domain")
public class TestDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestDataApplication.class, args);
    }

}
