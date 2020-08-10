package cn.procsl.ping.boot.domain.support.business;

import cn.procsl.ping.boot.domain.domain.entity.TreeEntity;
import cn.procsl.ping.boot.domain.domain.repository.TreeEntityTestRepository;
import cn.procsl.ping.boot.domain.support.exector.DomainRepositoryFactoryBean;
import com.github.javafaker.Faker;
import com.github.jsonzou.jmockdata.MockConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;

import static cn.procsl.ping.boot.domain.domain.entity.TreeEntity.root;

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
@Rollback(false)
public class AdjacencyTreeRepositoryTest {

    @Inject
    AdjacencyTreeRepository<TreeEntity, String> treeExecutor;

    @Inject
    JpaRepository<TreeEntity, String> jpaRepository;

    @Inject
    QuerydslPredicateExecutor<TreeEntity> querydslPredicateExecutor;

    @Inject
    TreeEntityTestRepository treeEntityRepository;

    private static final Faker FAKER;

    private static final MockConfig mockConfig;

    static {
        mockConfig = new MockConfig()
                .globalConfig()
                .subConfig(TreeEntity.class, "name")
                .stringRegex("[a-z0-9_]{20,20}")

                .globalConfig()
                .excludes(TreeEntity.class, "id", "path");

        FAKER = new Faker(Locale.CHINA);
    }

    @Before
    public void init() {

        TreeEntity persistence = new TreeEntity();
        BeanUtils.copyProperties(root, persistence);
        jpaRepository.save(persistence);
        BeanUtils.copyProperties(persistence, root);

        for (int i = 0; i < 500; i++) {
            TreeEntity child = new TreeEntity();
            child.fullByParent(root);
            child.setName(FAKER.name().name());
            TreeEntity entity = jpaRepository.save(child);
            jpaRepository.save(entity);
        }
    }

    @Test
    public void parents() {
        List<TreeEntity> all = jpaRepository.findAll();
        for (TreeEntity entity : all) {
            log.info(entity.toString());
        }

        treeExecutor.parents("id");
    }

    @Test
    public void children() {
        treeExecutor.children("id");
    }

    @Test
    public void directParent() {
        treeExecutor.directParent("id");
    }
}
