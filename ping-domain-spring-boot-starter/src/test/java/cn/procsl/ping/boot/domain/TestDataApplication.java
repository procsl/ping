package cn.procsl.ping.boot.domain;

import cn.procsl.ping.boot.domain.support.executor.DomainRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

/**
 * @author procsl
 * @date 2020/04/06
 */
@SpringBootApplication(scanBasePackages = "cn.procsl.ping.boot.domain")
@EnableJpaRepositories(
        basePackages = "cn.procsl.ping.boot.domain.domain.repository",
        repositoryFactoryBeanClass = DomainRepositoryFactoryBean.class,
        bootstrapMode = BootstrapMode.LAZY
)
@EntityScan(basePackages = "cn.procsl.ping.boot.domain.domain.entity")
public class TestDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestDataApplication.class, args);
    }

}
