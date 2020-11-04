package cn.procsl.ping.boot.domain.support.executor;

import cn.procsl.ping.boot.domain.annotation.EnableDomainRepositories;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.business.utils.PathUtils;
import cn.procsl.ping.boot.domain.domain.model.Path;
import cn.procsl.ping.boot.domain.domain.model.QPath;
import cn.procsl.ping.boot.domain.domain.model.QTree;
import cn.procsl.ping.boot.domain.domain.model.Tree;
import cn.procsl.ping.boot.domain.domain.repository.TreeTestRepository;
import cn.procsl.ping.business.domain.DomainId;
import com.github.javafaker.Faker;
import com.github.jsonzou.jmockdata.MockConfig;
import com.querydsl.core.NonUniqueResultException;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode.ROOT_DEPTH;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
@SpringBootApplication
@EnableDomainRepositories(repositories = "cn.procsl.ping.boot.domain.domain.repository", entities = "cn.procsl.ping.boot.domain.domain.model")
@ComponentScan("cn.procsl.ping.boot.domain.test")
@Rollback(value = false)
public class AdjacencyTreeExecutorTest {

    static {
        mockConfig = new MockConfig()
            .globalConfig()
            .subConfig(Tree.class, "name")
            .stringRegex("[a-z0-9_]{20,20}")

            .globalConfig()
            .excludes(Tree.class, "id", "path");

        FAKER = new Faker(Locale.CHINA);
    }

    @Inject
    JpaRepository<Tree, Long> jpaRepository;

    @Inject
    QuerydslPredicateExecutor<Tree> querydslPredicateExecutor;

    @Inject
    TreeTestRepository treeEntityRepository;

    @Inject
    JPAQueryFactory jpaQueryFactory;

    @Inject
    EntityManager entityManager;

    @Inject
    BeanFactory beanFactory;

    private static final Faker FAKER;

    private static final MockConfig mockConfig;

    private QTree T = QTree.tree;

    @Inject
    AdjacencyTreeRepository<Tree, Long, Path> treeExecutor;
    private QPath P = QPath.path;

    private List<Long> roots;

    @Before
    public void init() {

        roots = this.treeExecutor.getRoots(T.id).collect(Collectors.toList());
        if (!roots.isEmpty()) {
            roots = this.roots;
            entityManager.clear();
            return;
        }

        Tree root = createTree(null);

        int context = 1;
        for (int i = 0; i < 50; i++) {
            log.info("index:{}", i);
            create(root, context);
        }
        jpaRepository.flush();
        roots = Collections.singletonList(root.getId());
        entityManager.clear();
    }

    private void create(Tree parent, int context) {

        log.info("{}context 递归{}:{}", getTab(context), context);
        context++;
        if (FAKER.number().numberBetween(0, 9) < 3) {
            log.info("{}context 回归:{}", getTab(context - 1), context);
            return;
        }
        Tree entity = createTree(parent);
        create(entity, context);
    }

    String getTab(int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append('\t');
        }
        return builder.toString();
    }

    Long maxId() {
        entityManager.clear();
        Long rootId = roots.get(0);
        int maxDepth = this.treeExecutor.findMaxDepth(rootId);
        return this.treeExecutor
            .findAll(T.id, T.depth.eq(maxDepth))
            .limit(1).findFirst().get();
    }

    @Test
    public void testProjections() {
        Long anyId = maxId();

        @Cleanup
        Stream<Long> ids = this.treeExecutor.getParents(T.id, anyId);

        @Cleanup
        Stream<Tree> entity = this.treeExecutor.getParents(T, anyId);

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
        Stream<SimpleDTO> consProj = this.treeExecutor.getParents(constructor, anyId);

        @Cleanup
        Stream<SimpleDTO> beanProj = this.treeExecutor.getParents(beans, anyId);

        @Cleanup
        Stream<SimpleDTO> fieldProj = this.treeExecutor.getParents(fields, anyId);

        @Cleanup
        Stream<Map<Expression<?>, ?>> mapProj = this.treeExecutor.getParents(map, anyId);

        @Cleanup
        Stream<List<?>> listProj = this.treeExecutor.getParents(list, anyId);

        @Cleanup
        Stream<Tuple> tupleProj = this.treeExecutor.getParents(tuple, anyId);

        ids.forEach(i -> log.info("id:{}", i.toString()));

        entity.forEach(i -> log.info("entity:{}", i.toString()));

        consProj.forEach(i -> log.info("proj:{}", i.toString()));

        beanProj.forEach(i -> log.info("beans:{}", i.toString()));

        fieldProj.forEach(i -> log.info("fields:{}", i.toString()));

        mapProj.forEach(i -> log.info("map:{}", i.toString()));

        listProj.forEach(i -> log.info("list:{}", i.toString()));

        tupleProj.forEach(i -> log.info("tuple:{}", i.toString()));
    }

    public Tree createTree(Tree parent) {
        return jpaRepository.saveAndFlush(new Tree(parent));
    }

    @Test
    public void findOne() {
        Long id = maxId();
        Tree one = this.treeExecutor.findOne(T, T.parentId.eq(id));
        Assert.assertNull(one);
    }

    @Test(expected = NonUniqueResultException.class)
    @Rollback
    public void findOneThrow() {
        Long rootId = roots.get(0);
        int maxDepth = this.treeExecutor.findMaxDepth(rootId);
        Tree one = this.treeExecutor.findOne(T, T.depth.eq(ROOT_DEPTH + 1));
    }

    @Test
    public void findAll() {
        Integer rand = new Random().ints(1, 0, 10).findFirst().orElse(10);
        List<BigInteger> all = (List<BigInteger>) entityManager
            .createNativeQuery("select T.id from ping.dt_tree as T where T.depth between :min and :max")
            .setParameter("min", ROOT_DEPTH)
            .setParameter("max", rand)
            .getResultList();

        Stream<Long> stream = this.treeExecutor.findAll(T.id, T.depth.between(ROOT_DEPTH, rand));

        List<Long> all1 = stream.collect(Collectors.toList());

        Assert.assertEquals(all.size(), all1.size());
        all.removeIf(i -> all1.contains(i.longValue()));
        Assert.assertTrue(all.isEmpty());
        log.info("{}", all1);
    }

    @Test
    public void testFindAll() {
        Integer rand = new Random().ints(1, 0, 10).findFirst().orElse(10);
        List<Long> data;
        {
            List<BigInteger> all = (List<BigInteger>) entityManager
                .createNativeQuery("select T.id from ping.dt_tree as T where T.depth order by T.id desc ")
                .setFirstResult(0)
                .setMaxResults(10)
                .getResultList();
            data = all.stream().map(i -> i.longValue()).collect(Collectors.toList());
        }

        PageRequest s = PageRequest.of(0, 10, QSort.by(T.id.desc()));
        List<Long> all1 = this.treeExecutor.findAll(T.id, s).getContent();

        Assert.assertEquals(all1.size(), data.size());
        for (int i = 0; i < data.size(); i++) {
            Assert.assertEquals(data.get(i), all1.get(i));
        }
        log.info("page:{}", all1);
    }

    @Test
    public void getParents() {
        Long maxId = this.maxId();

        List<Long> data;
        {
            List<BigInteger> query = (List<BigInteger>) this.entityManager
                .createNativeQuery("select path_id as id from ping.dt_tree_path where id=:id order by seq asc")
                .setParameter("id", maxId)
                .getResultList();
            data = query.stream().map(i -> i.longValue()).collect(Collectors.toList());
        }


        {
            List<Long> parents = this.treeExecutor.getParents(T.id, maxId)
                .collect(Collectors.toList());
            Assert.assertEquals(parents.size(), data.size());
            for (int i = 0; i < data.size(); i++) {
                Assert.assertEquals(data.get(i), parents.get(i));
            }
        }


        {
            List<Long> parents = this.treeExecutor.getParents(Projections.tuple(T.id, T.depth), maxId)
                .map(item -> item.get(T.id))
                .collect(Collectors.toList());
            Assert.assertEquals(parents.size(), data.size());
            for (int i = 0; i < data.size(); i++) {
                Assert.assertEquals(data.get(i), parents.get(i));
            }
        }

    }

    @Test
    public void testGetParents() {
        Long maxId = this.maxId();
        int min = 0;
        int max = 10;

        List<Long> data;
        {
            List<BigInteger> query = (List<BigInteger>) this.entityManager
                .createNativeQuery("select path_id as id from ping.dt_tree_path where id=:id order by seq asc, id desc")
                .setParameter("id", maxId)
                .setFirstResult(min)
                .setMaxResults(max)
                .getResultList();
            data = query.stream().map(i -> i.longValue()).collect(Collectors.toList());
        }

        {
            PageRequest pageable = PageRequest.of(min, max, QSort.by(T.id.desc()));
            List<Long> parents = this.treeExecutor.getParents(T.id, pageable, maxId).getContent();

            Assert.assertEquals(parents.size(), data.size());
            for (int i = 0; i < data.size(); i++) {
                Assert.assertEquals(data.get(i), parents.get(i));
            }
        }

        {
            PageRequest pageable = PageRequest.of(min, max, QSort.by(T.id.desc()));
            List<Long> parents = this.treeExecutor
                .getParents(Projections.tuple(T.id, T.name), pageable, maxId)
                .getContent()
                .stream()
                .map(item -> item.get(T.id))
                .collect(Collectors.toList());

            Assert.assertEquals(parents.size(), data.size());
            for (int i = 0; i < data.size(); i++) {
                Assert.assertEquals(data.get(i), parents.get(i));
            }
        }
    }

    @Test
    public void getDirectChildren() {
        Long root = roots.get(0);
        List<Long> data;
        {
            List<BigInteger> query = (List<BigInteger>) this.entityManager
                .createNativeQuery("select id from ping.dt_tree where parent_id = :id ")
                .setParameter("id", root)
                .getResultList();
            data = query.stream().map(i -> i.longValue()).collect(Collectors.toList());
        }

        List<Long> childs = this.treeExecutor.getDirectChildren(T.id, root).collect(Collectors.toList());
        Assert.assertEquals(childs.size(), data.size());
        for (Long child : childs) {
            Assert.assertTrue(data.contains(child));
        }

        for (Long s : data) {
            Assert.assertTrue(childs.contains(s));
        }
    }

    @Test
    public void testGetDirectChildren() {
        Long root = roots.get(0);
        int min = 0;
        int max = 10;
        List<Long> data;
        {
            List<BigInteger> query = (List<BigInteger>) this.entityManager
                .createNativeQuery("select id from ping.dt_tree where parent_id = :id ")
                .setParameter("id", root)
                .setFirstResult(min)
                .setMaxResults(max)
                .getResultList();
            data = query.stream().map(i -> i.longValue()).collect(Collectors.toList());
        }

        {
            PageRequest page = PageRequest.of(min, max);
            List<Long> childs = this.treeExecutor.getDirectChildren(T.id, page, root).getContent();
            Assert.assertEquals(childs.size(), data.size());
            for (Long child : childs) {
                Assert.assertTrue(data.contains(child));
            }

            for (Long s : data) {
                Assert.assertTrue(childs.contains(s));
            }
        }
    }

    @Test
    public void getAllChildren() {
        Long root = roots.get(0);
        List<Long> data;
        {
            List<BigInteger> query = this.entityManager
                .createNativeQuery("SELECT tree.id FROM	dt_tree as tree INNER JOIN dt_tree_path as path ON tree.id = path.id WHERE path.path_id =:id order by tree.depth asc")
                .setParameter("id", root)
                .getResultList();
            data = query.stream().map(i -> i.longValue()).collect(Collectors.toList());
        }

        {
            List<Long> childs = this.treeExecutor.getAllChildren(T.id, root).collect(Collectors.toList());

            Assert.assertEquals(childs.size(), data.size());
            for (Long child : childs) {
                Assert.assertTrue(data.contains(child));
            }

            for (Long s : data) {
                Assert.assertTrue(childs.contains(s));
            }
        }
    }

    @Test
    public void testGetAllChildren() {
        Long root = roots.get(0);
        int min = 0;
        int max = 10;
        BigInteger data;
        {
            data = (BigInteger) this.entityManager
                .createNativeQuery("SELECT\n" +
                    "	count( tree.id )\n" +
                    "FROM\n" +
                    "	dt_tree tree\n" +
                    "	INNER JOIN dt_tree_path path ON tree.id = path.id \n" +
                    "WHERE\n" +
                    "	path.path_id =:id")
                .setParameter("id", root).getSingleResult();
        }

        {
            PageRequest page = PageRequest.of(min, max);
            Page<Long> childs = this.treeExecutor.getAllChildren(T.id, page, root);
            Assert.assertEquals(childs.getTotalElements(), data.longValue());
        }
    }

    @Test
    public void getDirectParent() {
        Optional<Long> id = this.treeExecutor.getDirectParent(T.id, roots.get(0));
        Assert.assertTrue(id.get().equals(roots.get(0)));
    }

    @Test
    public void getDepth() {
        int depth = this.treeExecutor.getDepth(roots.get(0));
        Assert.assertEquals(depth, 0);
    }

    @Test
    public void findMaxDepth() {
        Integer depath = (Integer) this.entityManager
            .createNativeQuery("SELECT\n" +
                "\ttree0_.depth AS col_0_0_ \n" +
                "FROM\n" +
                "\tdt_tree tree0_\n" +
                "\tINNER JOIN dt_tree_path path1_ ON tree0_.id = path1_.id \n" +
                "WHERE\n" +
                "\tpath1_.path_id =:id \n" +
                "ORDER BY\n" +
                "\ttree0_.depth DESC \n" +
                "\tLIMIT 1")
            .setParameter("id", roots.get(0))
            .getSingleResult();
        int max = this.treeExecutor.findMaxDepth(roots.get(0));
        Assert.assertEquals((Integer) max, (Integer) depath);
    }

    @Test
    @Rollback
    public void mount() {
        List<Long> childs = this.treeExecutor
            .getDirectChildren(T.id, roots.get(0))
            .filter(i -> i > 5)
            .filter(i -> !(i.equals(roots.get(0))))
            .limit(3)
            .collect(Collectors.toList());
        Assert.assertTrue(childs.size() > 2);

        Long child1 = childs.get(0);
        List<Long> child3 = this.treeExecutor.getDirectChildren(T.id, child1).filter(i -> !i.equals(child1)).collect(Collectors.toList());
        Assert.assertFalse(child3.isEmpty());

        Long child2 = childs.get(1);

        this.treeExecutor.mount(child1, child2);
        List<Long> child5 = this.treeExecutor.getDirectChildren(T.id, roots.get(0)).filter(i -> i.equals(child2)).collect(Collectors.toList());
        Assert.assertTrue(child5.isEmpty());

        List<Long> child6 = this.treeExecutor.getDirectChildren(T.id, child1).filter(i -> !i.equals(child1)).collect(Collectors.toList());
        Assert.assertFalse(child6.isEmpty());
        Assert.assertTrue(child6.contains(child2));
    }

    @Test
    public void remove() {
        this.treeExecutor.remove(roots.get(0));
        Long count = (Long) this.entityManager.createQuery("select count(a) from cn.procsl.ping.boot.domain.domain.model.Tree as a ").getSingleResult();
        Assert.assertEquals((Long) count, (Long) 0L);
    }

    @Test
    public void findLinks() {
        Long max = maxId();
        List<Long> list = this.treeExecutor.findLinks(T.id, max, roots.get(0)).collect(Collectors.toList());
        Assert.assertEquals(list.size(), this.treeExecutor.getDepth(max) + 1);
    }

    @Test
    public void searchAll() {
        Long maxId = maxId();
        Tree tree = this.jpaRepository.findById(maxId).get();

        String name = tree.getName();
        List<Long> path = PathUtils.split(name, "/").stream().map(item -> Long.valueOf(item)).collect(Collectors.toList());
        List<SimpleDTO> simpleDTO = this.treeExecutor
            .searchAll(Projections.constructor(SimpleDTO.class, T.id, T.name),
                path, index -> T.id.eq(path.get(index)), true);

        Assert.assertEquals(simpleDTO.size(), tree.getDepth() + 1);
        for (int i = 0; i < simpleDTO.size(); i++) {
            Assert.assertEquals(path.get(i), simpleDTO.get(i).getId());
        }
        log.info("{}", simpleDTO);
    }

    @Test
    public void searchOne() {
        Long maxId = maxId();
        Tree tree = this.jpaRepository.findById(maxId).get();

        String name = tree.getName();
        List<Long> path = PathUtils.split(name, "/").stream().map(item -> Long.valueOf(item)).collect(Collectors.toList());
        SimpleDTO simpleDTO = this.treeExecutor
            .searchOne(Projections.constructor(SimpleDTO.class, T.id, T.name),
                path, index -> T.id.eq(path.get(index)), true);

        Assert.assertEquals(simpleDTO.id, tree.getId());
        Assert.assertEquals(simpleDTO.getName(), tree.getName());
        log.info("{}", simpleDTO);
    }

    @Test
    public void testFindLinks() {
        Long max = maxId();
        PageRequest page = PageRequest.of(0, 10, QSort.by(T.id.desc()));
        Page<Long> list = this.treeExecutor.findLinks(T.id, page, max, roots.get(0));
        Assert.assertEquals(list.getTotalElements(), this.treeExecutor.getDepth(max) + 1);
    }

    @Test
    public void calcDepth() {
    }

    @Test
    public void calcChildren() {
    }

    @Test
    public void calcParent() {
    }

    @Test
    public void getRoots() {
    }

    @Test
    public void testGetRoots() {
    }

    @AllArgsConstructor
    @Setter
    @Getter
    @ToString
    @NoArgsConstructor
    public static class SimpleDTO implements DomainId<Long> {
        Long id;
        String name;
    }
}
