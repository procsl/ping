package cn.procsl.ping.boot.rbac.domain.model;

import cn.procsl.ping.boot.rbac.domain.repository.RoleRepository;
import cn.procsl.ping.boot.rbac.domain.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author procsl
 * @date 2020/04/15
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
public class LinkByIdTest {

    private JPAQueryFactory queryFactory;

    @Inject
    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private UserRepository userDao;

    @Inject
    private RoleRepository roleDao;

    @Before
    public void init() {
        queryFactory = new JPAQueryFactory(entityManager);
    }

    @Test
    public void test() {
        log.info("success");
    }

    @Test
    public void test2() {
        User procsl = User.create().name("procsl").done();
        userDao.save(procsl);
    }

    @Test
    public void test3() {
        Role role = Role.create().name("super").done();
        roleDao.save(role);
    }


}
