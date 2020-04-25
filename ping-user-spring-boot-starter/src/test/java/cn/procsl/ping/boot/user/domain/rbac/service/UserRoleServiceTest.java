package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.user.domain.rbac.model.QRole;
import cn.procsl.ping.boot.user.domain.rbac.model.Role;
import cn.procsl.ping.boot.user.domain.rbac.repository.RoleRepository;
import cn.procsl.ping.boot.user.domain.rbac.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * @author procsl
 * @date 2020/04/19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
@Named
public class UserRoleServiceTest {

    @Inject
    UserRoleService userRoleService;

    @Inject
    RoleRepository roleDao;

    @Inject
    UserRoleRepository userRoleRepository;

    String userIdentity;

    @Before
    public void before() {
        userIdentity = "402880ec7190bd88017190bdbad60000";
    }

    @Test
    public void test() {
        log.info("success");
    }

    @Test
    public void test2() {
        Role role = Role.create().name("super").done();
        roleDao.save(role);
    }

    public void test3() {
        Role role = Role.create().name("super").done();
        roleDao.save(role);
        Assert.assertTrue(roleDao.findOne(QRole.role.name.eq("super")).isPresent());
    }

    @Test
    public void addRole2User() {
    }
}
