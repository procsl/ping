package cn.procsl.ping.boot.user.rbac;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@SpringBootApplication
public class RbacApplicationServiceTest {

    @Inject
    RbacApplicationService rbacApplicationService;

    @Inject
    JpaRepository<Role, Long> roleJpaRepository;


    @Test
    public void test() {
        Assert.assertNotNull("注入失败", rbacApplicationService);
    }

    @After
    public void tearDown() throws Exception {
        this.roleJpaRepository.flush();
    }

    @Test
    @Transactional
    @Rollback
    public void createRole() {
        rbacApplicationService.createRole("我是角色", Arrays.asList("POST_www.baidu.com"));
    }

    @Test
    @Transactional
    @Rollback
    public void deleteRole() {
        Long id = rbacApplicationService.createRole("我是角色11", Arrays.asList("POST_www.baidu.com"));
        rbacApplicationService.deleteRole(id);
    }

    @Test
    @Transactional
    @Rollback
    public void changeRolePermissions() {
        Long id = rbacApplicationService.createRole("我是角色12", Arrays.asList("POST_www.baidu.com"));
        rbacApplicationService.changeRolePermissions(id, Arrays.asList("get_www.baidu.com", "post_www.baidu.com"));
        Role role = roleJpaRepository.getOne(id);
        Assert.assertEquals(2, role.getPermissions().size());

        Assert.assertTrue(role.getPermissions().contains(new Permission("get_www.baidu.com")));
        Assert.assertTrue(role.getPermissions().contains(new Permission("post_www.baidu.com")));
        Assert.assertEquals("我是角色12", role.getName());
    }

    @Test
    @Transactional
    @Rollback
    public void changeRoleName() {
        Long id = rbacApplicationService.createRole("我是角色13", Collections.singletonList("POST_www.baidu.com"));
        rbacApplicationService.changeRoleName(id, "我是角色23");
        Role role = roleJpaRepository.getOne(id);
        Assert.assertEquals(1, role.getPermissions().size());

        Assert.assertTrue(role.getPermissions().contains(new Permission("POST_www.baidu.com")));
        Assert.assertEquals("我是角色23", role.getName());
    }

    @Test
    @Transactional
    @Rollback
    public void changeRole() {
        {
            Long id = rbacApplicationService.createRole("我是角色14", Collections.singletonList("POST_www.baidu.com"));
            this.rbacApplicationService.changeRole(id, "我是角色", Arrays.asList("permission1", "permission2"));

            Role role = roleJpaRepository.getOne(id);
            Assert.assertEquals(2, role.getPermissions().size());
            Assert.assertEquals("我是角色", role.getName());
        }

        {

            Long id2 = rbacApplicationService.createRole("我是角色15", Collections.singletonList("POST_www.baidu.com"));
            this.rbacApplicationService.changeRole(id2, null, Arrays.asList("permission1", "permission2"));
            Role role2 = roleJpaRepository.getOne(id2);

            Assert.assertEquals("我是角色15", role2.getName());
            Assert.assertEquals(2, role2.getPermissions().size());
            Assert.assertTrue(role2.getPermissions().contains(new Permission("permission1")));
            Assert.assertTrue(role2.getPermissions().contains(new Permission("permission2")));
        }

        {

            Long id3 = rbacApplicationService.createRole("我是角色16", Collections.singletonList("POST_www.baidu.com"));
            this.rbacApplicationService.changeRole(id3, null, null);
            Role role3 = roleJpaRepository.getOne(id3);

            Assert.assertEquals("我是角色16", role3.getName());
            Assert.assertTrue(role3.getPermissions().contains(new Permission("POST_www.baidu.com")));
            Assert.assertEquals(1, role3.getPermissions().size());
        }
    }
}
