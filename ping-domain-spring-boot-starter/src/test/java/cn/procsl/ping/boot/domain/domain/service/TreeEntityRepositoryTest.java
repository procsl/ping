package cn.procsl.ping.boot.domain.domain.service;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.domain.model.Path;
import cn.procsl.ping.boot.domain.domain.model.Tree;
import cn.procsl.ping.boot.domain.support.executor.DomainRepositoryFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

/**
 * @author procsl
 * @date 2020/07/31
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@SpringBootApplication
@EnableJpaRepositories(
    basePackages = "cn.procsl.ping.boot.domain.domain.repository",
    repositoryFactoryBeanClass = DomainRepositoryFactoryBean.class,
    bootstrapMode = BootstrapMode.LAZY
)
@EntityScan(basePackages = "cn.procsl.ping.boot.domain.domain.model")
@ComponentScan("cn.procsl.ping.boot.domain.test")
public class TreeEntityRepositoryTest {

    @Inject
    AdjacencyTreeRepository<Tree, Long, Path> adjacencyTreeExecutor;

    @Test
    public void test() {
        Assert.assertNotNull(adjacencyTreeExecutor);
    }


}
