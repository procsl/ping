package cn.procsl.ping.app.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * starter
 *
 * @author procsl
 * @date 2020/04/26
 */
@SpringBootApplication
@EnableJpaAuditing
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

}
