package cn.procsl.ping.boot.rbac.domain.model;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author procsl
 * @date 2020/04/13
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
public class QueryDslTest {

    private JPAQueryFactory queryFactory;

    @Inject
    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private JpaRepository<User, String> userDao;

    @Inject
    private JpaRepository<Role, Long> roleDao;

    @Before
    public void init() {
        queryFactory = new JPAQueryFactory(entityManager);
    }

    @Test
    public void test() {
        long count = queryFactory.update(QUser.user).set(QUser.user.name, "procsl").where(QUser.user.name.eq("test")).execute();
        Assert.assertEquals(count, 0L);
    }

    @Test
    public void test2() {
        User user = User.create().name("test").done();
        userDao.save(user);

        long count = queryFactory
                .update(QUser.user)
                .set(QUser.user.name, "傻")
                .where(QUser.user.name.eq("test"))
                .execute();
        entityManager.clear();

        Assert.assertNotEquals(user.getName(), "傻");
        Assert.assertEquals(count, 1L);

        userDao.findById(user.getId())
                .ifPresent(u -> Assert.assertEquals(u.getName(), "傻"));
    }

    @Test
    public void test3() {
//
//        // 创建一点角色
//        Role role1 = Role.builder().name("HelloWorld").build();
//        roleDao.save(role1);
//
//        User user = User.builder().name("test").create();
////        user.addRole(role1);
//
//        userDao.save(user);
//        userDao.findById(user.getId()).ifPresent(u -> Assert.assertTrue(u.contains(role1)));
//        log.info("user:[{}]", user);
//
//        Query query = entityManager.createNativeQuery("select * from pb_user_role where user_id = '"+user.getId()+"'");
//        List list = query.getResultList();
//        Assert.assertEquals(1, list.size());
//        log.info("list:[{}]", list);
    }
}
