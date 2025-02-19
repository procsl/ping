package cn.procsl.ping.boot.jpa;

import cn.procsl.ping.boot.jpa.support.extension.EnableJpaExtensionRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableJpaExtensionRepositories(basePackages = {
    "cn.procsl.ping.boot.jpa.jpql",
    "cn.procsl.ping.boot.jpa.support.query"
})
//        , basePackageClasses = {ExtensionRepository.class, StandRepository.class})
@EntityScan({"cn.procsl.ping.boot.jpa.support.query", "cn.procsl.ping.boot.jpa.support.query.ast.domain"})
public class TestJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestJpaApplication.class, args);
    }

}
