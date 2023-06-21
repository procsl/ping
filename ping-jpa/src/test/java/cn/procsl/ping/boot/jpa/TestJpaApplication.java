package cn.procsl.ping.boot.jpa;

import cn.procsl.ping.boot.jpa.domain.ExtensionRepository;
import cn.procsl.ping.boot.jpa.domain.StandRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

@SpringBootApplication
@EnableJpaRepositories(value = {"cn.procsl.ping.boot.jpa.domain", "cn.procsl.ping.boot.jpa.jpql"},
        basePackageClasses = {ExtensionRepository.class, StandRepository.class}
        , bootstrapMode = BootstrapMode.LAZY)
@EntityScan("cn.procsl.ping.boot.jpa.domain")
public class TestJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestJpaApplication.class, args);
    }

}
