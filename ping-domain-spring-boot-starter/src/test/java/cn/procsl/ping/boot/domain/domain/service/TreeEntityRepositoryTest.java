package cn.procsl.ping.boot.domain.domain.service;

import cn.procsl.ping.boot.domain.domain.entity.TreeEntity;
import cn.procsl.ping.boot.domain.support.business.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.support.exector.DomainRepositoryFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
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
public class TreeEntityRepositoryTest {

    @Inject
    AdjacencyTreeRepository<TreeEntity, String> adjacencyTreeExecutor;

    @Test
    public void test() {
        Assert.assertNotNull(adjacencyTreeExecutor);
    }


}
