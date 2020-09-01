package cn.procsl.ping.boot.domain.support.executor;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.domain.model.PathNode;
import cn.procsl.ping.boot.domain.domain.model.QPathNode;
import cn.procsl.ping.boot.domain.domain.model.QTree;
import cn.procsl.ping.boot.domain.domain.model.Tree;
import cn.procsl.ping.boot.domain.domain.repository.AdjacencyTreeRepositoryTestRepository;
import cn.procsl.ping.boot.domain.domain.repository.TreeTestRepository;
import com.github.javafaker.Faker;
import com.github.jsonzou.jmockdata.MockConfig;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
@EntityScan(basePackages = "cn.procsl.ping.boot.domain.domain.model")
@ComponentScan("cn.procsl.ping.boot.domain.test")
public class AdjacencyTreeExecutorTest {

    @Inject
    AdjacencyTreeRepository<Tree, Long, PathNode> treeExecutor;

    @Inject
    JpaRepository<Tree, Long> jpaRepository;

    @Inject
    QuerydslPredicateExecutor<Tree> querydslPredicateExecutor;

    @Inject
    TreeTestRepository treeEntityRepository;

    @Inject
    AdjacencyTreeRepositoryTestRepository repo;

    @Inject
    JPAQueryFactory jpaQueryFactory;

    @Inject
    EntityManager entityManager;

    @Inject
    BeanFactory beanFactory;

    private static final Faker FAKER;

    private static final MockConfig mockConfig;

    private QTree T = QTree.tree;

    private QPathNode P = QPathNode.pathNode;

    static {
        mockConfig = new MockConfig()
            .globalConfig()
            .subConfig(cn.procsl.ping.boot.domain.domain.model.Tree.class, "name")
            .stringRegex("[a-z0-9_]{20,20}")

            .globalConfig()
            .excludes(cn.procsl.ping.boot.domain.domain.model.Tree.class, "id", "path");

        FAKER = new Faker(Locale.CHINA);
    }

    private List<Long> roots;

    @Before
    public void init() {

        List<Long> roots = this.repo.getRoots(T.id).collect(Collectors.toList());
        if (!roots.isEmpty()) {
            roots = this.roots;
            return;
        }

        cn.procsl.ping.boot.domain.domain.model.Tree root = new cn.procsl.ping.boot.domain.domain.model.Tree();
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

    private void create(cn.procsl.ping.boot.domain.domain.model.Tree parent, int context) {

        log.info("{}context 递归{}:{}", getTab(context), context);
        context++;

        cn.procsl.ping.boot.domain.domain.model.Tree child = new cn.procsl.ping.boot.domain.domain.model.Tree();
        child.changeParent(parent);
//        child.setName(FAKER.name().name());
        cn.procsl.ping.boot.domain.domain.model.Tree entity = jpaRepository.save(child);

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
    public void remove() {
        int count = this.repo.remove(11L);
        log.info("count:{}", count);
    }

    @Test
    public void calcDepth() {
        Integer depth = this.treeExecutor.calcDepth(1L, 2L);
        log.info("{}", depth);
    }

    @Test
    public void findLinks() {
        @Cleanup
        Stream<Tuple> links = this.treeExecutor.findLinks(Projections.tuple(T.id, T.name), 11L, 15L, T.depth.in(13, 14, 15));
        links.forEach(item -> {
            log.info("id:{}", item.get(T.id));
            log.info("name:{}", item.get(T.name));
        });
    }

    @Test
    public void getAllChildren() {
        Stream<Long> ids = this.repo.getAllChildren(T.id, 11L);
        ids.forEach(id -> log.info("id:{}", id));
    }

    @Test
    public void getRoots() {
        Page<Long> ids = this.repo.getRoots(T.id, QPageRequest.of(0, 10, QSort.by(T.depth.asc())));
        ids.forEach(id -> log.info("id:{}", id));
    }

    @Test
    public void testGetRoots() {
        Stream<Long> stream = this.repo
            .getRoots(Projections.fields(T.id, T.id), T.parentId.eq(10L));
        stream.forEach(item -> log.info(item.toString()));
    }

    @Test
    public void getParents() {
        @Cleanup
        Stream<Long> ids = this.repo.getParents(T.id, 8011L);

        @Cleanup
        Stream<Tree> entity = this.repo.getParents(T, 8011L);

        // 投影查询 构造函数构造, 参数位置需要和查询字段对应
        ConstructorExpression<SimpleDTO> constructor = Projections.constructor(SimpleDTO.class, T.id, T.name);

        // 需要默认构造函数用于反射实例化, 并且如果field是不可访问的, 则会调用setter
        QBean<SimpleDTO> beans = Projections.bean(SimpleDTO.class, T.id, T.name);

        // 字段注入
        QBean<SimpleDTO> fields = Projections.fields(SimpleDTO.class, T.id, T.name);

        // 以list的形式返回
        QList list = Projections.list(T.id, T.name);

        // 返回map形式
        QMap map = Projections.map(T.id, T.name);

        QTuple tuple = Projections.tuple(T.id, T.name);

        @Cleanup
        Stream<SimpleDTO> consProj = this.repo.getParents(constructor, 8011L);

        @Cleanup
        Stream<SimpleDTO> beanProj = this.repo.getParents(beans, 8011L);

        @Cleanup
        Stream<SimpleDTO> fieldProj = this.repo.getParents(fields, 8011L);

        @Cleanup
        Stream<Map<Expression<?>, ?>> mapProj = this.repo.getParents(map, 8011L);

        @Cleanup
        Stream<List<?>> listProj = this.repo.getParents(list, 8011L);

        @Cleanup
        Stream<Tuple> tupleProj = this.repo.getParents(tuple, 8011L);

        ids.forEach(i -> log.info("id:{}", i.toString()));

        entity.forEach(i -> log.info("entity:{}", i.toString()));

        consProj.forEach(i -> log.info("proj:{}", i.toString()));

        beanProj.forEach(i -> log.info("beans:{}", i.toString()));

        fieldProj.forEach(i -> log.info("fields:{}", i.toString()));

        mapProj.forEach(i -> log.info("map:{}", i.toString()));

        listProj.forEach(i -> log.info("list:{}", i.toString()));

        tupleProj.forEach(i -> log.info("tuple:{}", i.toString()));
    }

    @AllArgsConstructor
    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    public static class SimpleDTO {
        Long id;
        String name;
    }


    @Test
    public void getDirectChildren() {
        Stream<Tree> stream = this.repo.getDirectChildren(T, 0L);
        stream.forEach(item -> log.info("item:{}", item));
    }

    @Test
    public void getDirectParent() {
        Optional<Tuple> direct = this.repo.getDirectParent(Projections.tuple(T.id, T.name), 1L);
        direct.ifPresent(item -> log.info("item:{}", item));
    }

    @Test
    public void getDepth() {
        int depth = treeExecutor.getDepth(roots.get(0));
        Assert.assertNotEquals(-1, depth);
    }

    @Test
    public void findMaxDepth() {
        int max = treeExecutor.findMaxDepth(roots.get(0));
        Assert.assertNotEquals(0, max);
    }

    @Test
    @Rollback(value = false)
    public void mount() {
        this.treeExecutor.mount(16L, 2L);
    }

}
