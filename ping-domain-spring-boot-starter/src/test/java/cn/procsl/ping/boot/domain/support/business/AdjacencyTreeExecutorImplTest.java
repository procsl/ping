package cn.procsl.ping.boot.domain.support.business;

import cn.procsl.ping.boot.domain.domain.entity.TreeEntity;
import cn.procsl.ping.boot.domain.domain.repository.TreeEntityRepository;
import cn.procsl.ping.boot.domain.support.exector.DomainRepositoryFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author procsl
 * @date 2020/07/31
 */
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
@EntityScan(basePackages = "cn.procsl.ping.boot.domain.domain.entity")
public class AdjacencyTreeExecutorImplTest {

    @Inject
    AdjacencyTreeRepository<TreeEntity, String> treeExecutor;

    @Inject
    JpaRepository<TreeEntity, String> jpaRepository;

    @Inject
    QuerydslPredicateExecutor<TreeEntity> querydslPredicateExecutor;

    @Inject
    TreeEntityRepository treeEntityRepository;

    @Test
    public void parents() {
        treeExecutor.parents("123");
        treeEntityRepository.existsById("123");
        jpaRepository.findAll();
        querydslPredicateExecutor.findAll();
    }

    @Test
    public void children() {
        treeExecutor.children("123");
    }

    @Test
    public void directParent() {
        treeExecutor.directParent("123");
    }
}
