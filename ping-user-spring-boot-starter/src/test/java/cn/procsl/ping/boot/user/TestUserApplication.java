package cn.procsl.ping.boot.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication
@EnableJpaAuditing
public class TestUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestUserApplication.class, args);
    }

}
