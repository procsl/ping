package cn.procsl.ping.boot.jpa;

import cn.procsl.ping.boot.jpa.domain.ExtensionRepository;
import cn.procsl.ping.boot.jpa.domain.StandRepository;
import cn.procsl.ping.boot.jpa.support.extension.EnableJpaExtensionRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

@SpringBootApplication
@EnableJpaExtensionRepositories(basePackages = {"cn.procsl.ping.boot.jpa.jpql"})
//        , basePackageClasses = {ExtensionRepository.class, StandRepository.class})
public class TestJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestJpaApplication.class, args);
    }

}
