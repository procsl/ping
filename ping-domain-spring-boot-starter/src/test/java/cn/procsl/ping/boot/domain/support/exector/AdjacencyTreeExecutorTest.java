package cn.procsl.ping.boot.domain.support.exector;

import cn.procsl.ping.boot.domain.business.Operator;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.domain.entity.PathNode;
import cn.procsl.ping.boot.domain.domain.entity.QTreeEntity;
import cn.procsl.ping.boot.domain.domain.entity.TreeEntity;
import cn.procsl.ping.boot.domain.domain.repository.AdjacencyTreeRepositoryTestRepository;
import cn.procsl.ping.boot.domain.domain.repository.TreeEntityTestRepository;
import com.github.javafaker.Faker;
import com.github.jsonzou.jmockdata.MockConfig;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
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
@Rollback(value = true)
public class AdjacencyTreeExecutorTest {

    @Inject
    AdjacencyTreeRepository<TreeEntity, Long, PathNode> treeExecutor;

    @Inject
    JpaRepository<TreeEntity, Long> jpaRepository;

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

    TreeEntity root;


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
        Optional<TreeEntity> optional = querydslPredicateExecutor.findOne(QTreeEntity.treeEntity.name.eq("/1"));
        if (optional.isPresent()) {
            root = optional.get();
            return;
        }

        root = new TreeEntity();
        root.init();
        jpaRepository.save(root);
        root.postPersist();

        int context = 1;
        for (int i = 0; i < 5; i++) {
            log.info("index:{}", i);
            create(root, context);
            if (i % 20 == 0) {
                jpaRepository.flush();
            }
        }
        jpaRepository.flush();
    }

    private void create(TreeEntity parent, int context) {

        log.info("{}context 递归{}:{}", getTab(context), context);
        context++;

        TreeEntity child = new TreeEntity();
        child.changeParent(parent);
//        child.setName(FAKER.name().name());
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
                .setParameter("id", 1L)
                .getResultList()
                .forEach(item -> log.info("---------------->" + item.getName() + ":" + item.getDepth() + ":" + item.getId()));
    }

    @Test
    @Transactional
    public void parents() {
        @Cleanup
        Stream<TreeEntity> parent = treeExecutor.getParents(0L);
        parent.forEach(item -> log.info(item.getName()));
    }

    @Test
    public void children() {
        @Cleanup
        Stream<TreeEntity> children = treeExecutor.getDirectChildren(root.getId());
        children.limit(10).forEach(item -> log.info(item.getName()));
    }

    @Test
    public void directParent() {
        Optional<TreeEntity> opera = treeExecutor.getDirectParent(0L);
        Assert.assertTrue(opera.isEmpty());
    }

    @Test
    public void depth() {
        int depth = treeExecutor.getDepth(root.getId());
        Assert.assertNotEquals(-1, depth);
    }

    @Test
    public void maxDepth() {
        int max = treeExecutor.findMaxDepth(root.getId());
        Assert.assertNotEquals(0, max);
    }

    @Test
    public void findDepthNodes() {
        Stream<TreeEntity> stream = this.treeExecutor.findDepthNodes(root.getId(), 28, Operator.EQ, Sort.Direction.DESC);
        stream.forEach(item -> log.info(item.toString()));
    }

    @Test
    public void moveTo() {
        this.treeExecutor.mount(1L, 2L);
    }

    @Test
    public void remove() {
        this.treeExecutor.remove(2L);
    }

    @Test
    public void calcDepth() {
        Integer depth = this.treeExecutor.calcDepth(1L, 2L);
        log.info("{}", depth);
    }

    @Test
    public void findLinks() {
        Stream<TreeEntity> links = this.treeExecutor.findLinks(1L, 13L);
        links.forEach(item -> log.info(item.getName()));
    }

    @Test
    public void parentIds() {
        Stream<Long> ids = this.treeExecutor.getParentIds(13L);
        ids.forEach(item -> log.info(item.toString()));
    }

    @Test
    public void findLinkIds() {
        Stream<Long> links = this.treeExecutor.findLinkIds(1L, 13L);
        links.forEach(item -> log.info(item.toString()));
    }

    @Test
    public void getAllChildrenIds() {
        Stream<Long> children = this.treeExecutor.getAllChildrenIds(1L);
        children.forEach(item -> log.info("id: ->{}", item));
    }

    @Test
    public void getAllChildren() {
        Stream<TreeEntity> children = this.treeExecutor.getAllChildren(1L);
        children.forEach(item -> log.info("id: ->{}", item.getName()));
    }
}
