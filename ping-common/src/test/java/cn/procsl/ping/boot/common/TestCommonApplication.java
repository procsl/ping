package cn.procsl.ping.boot.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(scanBasePackages = "cn.procsl.ping.boot")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.common.validator")
@EntityScan(basePackages = "cn.procsl.ping.boot.common.validator")
public class TestCommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestCommonApplication.class, args);
    }


}
