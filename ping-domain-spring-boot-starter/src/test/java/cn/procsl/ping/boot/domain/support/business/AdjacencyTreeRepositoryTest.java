package cn.procsl.ping.boot.domain.support.business;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.domain.entity.QTreeEntity;
import cn.procsl.ping.boot.domain.domain.entity.TreeEntity;
import cn.procsl.ping.boot.domain.domain.repository.AdjacencyTreeRepositoryTestRepository;
import cn.procsl.ping.boot.domain.domain.repository.TreeEntityTestRepository;
import cn.procsl.ping.boot.domain.support.exector.DomainRepositoryFactoryBean;
import com.github.javafaker.Faker;
import com.github.jsonzou.jmockdata.MockConfig;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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
import javax.persistence.EntityManager;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

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
@Rollback(value = false)
public class AdjacencyTreeRepositoryTest {

    @Inject
    AdjacencyTreeRepository<TreeEntity, String> treeExecutor;

    @Inject
    JpaRepository<TreeEntity, String> jpaRepository;

    @Inject
    QuerydslPredicateExecutor<TreeEntity> querydslPredicateExecutor;

    @Inject
    TreeEntityTestRepository treeEntityRepository;

    @Inject
    AdjacencyTreeRepositoryTestRepository adjacencyTreeRepositoryTestRepository;

    @Inject
    EntityManager entityManager;

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
        Optional<TreeEntity> optional = querydslPredicateExecutor.findOne(QTreeEntity.treeEntity.name.eq("root"));
        if (optional.isPresent()) {
            BeanUtils.copyProperties(optional.get(), root);
            return;
        }

        TreeEntity persistence = new TreeEntity();
        BeanUtils.copyProperties(root, persistence);
        jpaRepository.save(persistence);
        BeanUtils.copyProperties(persistence, root);

        int context = 1;
        for (int i = 0; i < 5; i++) {
            create(root, context);
        }
    }

    private void create(TreeEntity parent, int context) {

        log.info("{}context 递归{}:{}", getTab(context), context);
        context++;

        TreeEntity child = new TreeEntity();
        child.fullByParent(parent);
        child.setName(FAKER.name().name());
        TreeEntity entity = jpaRepository.save(child);

        if (FAKER.number().numberBetween(0, 9) < 3) {
            log.info("{}context 回归:{}", getTab(context - 1), context);
            return;
        }
        create(entity, context);
    }

    String getTab(int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append('\t');
        }
        return builder.toString();
    }

    @Test
    public void query() {
        String query = "select tree from TreeEntity as tree where tree.id in (select b.pathId from TreeEntity as a inner join a.path as b where b.id =:id) order by tree.depth asc";
        entityManager
                .createQuery(query, TreeEntity.class)
                .setParameter("id", "b34eeffa-b6ac-4caa-89b6-b197d4be5e4d")
                .getResultList()
                .forEach(item -> log.info("---------------->" + item.getName() + ":" + item.getDepth() + ":" + item.getId()));
    }

    @Test
    @Transactional(readOnly = true)
    public void parents() {
        @Cleanup
        Stream<TreeEntity> parent = treeExecutor.parents("b34eeffa-b6ac-4caa-89b6-b197d4be5e4d");
        parent.forEach(item -> log.info(item.getName()));
    }

    @Test
    public void children() {
        @Cleanup
        Stream<TreeEntity> children = treeExecutor.children(root.getId());
        children.limit(10).forEach(item -> log.info(item.getName()));
    }

    @Test
    public void directParent() {
        Optional<TreeEntity> opera = treeExecutor.directParent("id");
        Assert.assertTrue(opera.isEmpty());
    }
}
