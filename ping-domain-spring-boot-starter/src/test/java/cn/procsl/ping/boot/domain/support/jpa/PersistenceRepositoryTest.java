package cn.procsl.ping.boot.domain.support.jpa;

import cn.procsl.ping.boot.domain.business.common.repository.PersistenceRepository;
import cn.procsl.ping.boot.domain.domain.model.User;
import cn.procsl.ping.boot.domain.support.executor.DomainRepositoryFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
@SpringBootApplication
@EnableJpaRepositories(
    basePackages = "cn.procsl.ping.boot.domain.domain.repository",
    repositoryFactoryBeanClass = DomainRepositoryFactoryBean.class,
    bootstrapMode = BootstrapMode.LAZY
)
@EntityScan(basePackages = "cn.procsl.ping.boot.domain.domain.model")
@Configuration
public class PersistenceRepositoryTest {

    @Inject
    PersistenceRepository<User, String> persistenceRepository;

    @Test
    public void test() {
        String str = persistenceRepository.toString();
        log.info(str);
    }

}
