package cn.procsl.ping.boot.jpa;

import cn.procsl.ping.boot.jpa.domain.ExtensionRepository;
import cn.procsl.ping.boot.jpa.domain.StandRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

@SpringBootApplication(scanBasePackages = "cn.procsl.ping.boot.jpa")
@EnableJpaRepositories(value = "cn.procsl.ping.boot.jpa.domain",
        basePackageClasses = {ExtensionRepository.class, StandRepository.class}
        , bootstrapMode = BootstrapMode.LAZY)
@EntityScan("cn.procsl.ping.boot.jpa.domain")
public class JpaTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaTestApplication.class, args);
    }

}
