package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @author procsl
 * @date 2020/07/06
 */
@Slf4j
@Transactional
@SpringBootTest
@RunWith(SpringRunner.class)
public class RoleServiceTest {

    @Inject
    RoleService roleService;

    private Long roleId;

    @Before
    public void before() {
        this.roleId = roleService.create("default角色", null);
    }

    @Test
    public void create() {
        roleService.create("角色", null);
    }

    @Test(expected = BusinessException.class)
    public void delete() {
        this.roleService.delete(roleId);
        roleService.load(roleId);
    }

    @Test
    public void rename() {
        roleService.rename(roleId, "newName");
        Role role = roleService.load(roleId);
        Assert.assertEquals(role.getName(), "newName");
    }

    @Test
    public void changeInherit() {

        Role role = roleService.load(roleId);
        Assert.assertEquals(role.getInheritBy(), Role.EMPTY_ROLE_ID);

        Long inherit = roleService.create("inherit", null);
        roleService.changeInherit(roleId, inherit);

        Role role1 = roleService.load(roleId);
        Assert.assertEquals(role1.getInheritBy(), inherit);
    }

    @Test(expected = BusinessException.class)
    public void changeInheritCase1() {
        Long inherit = roleService.create("inherit", 10000000L);
        roleService.changeInherit(roleId, inherit);
    }

    @Test
    public void load() {
        roleService.load(roleId);
    }

    @Test(expected = BusinessException.class)
    public void checkInheritable() {
        roleService.checkInheritable(10000L);
    }

    @Test
    public void checkInheritableCase1() {
        roleService.checkInheritable(Role.EMPTY_ROLE_ID);
    }

    @Test
    public void checkNameable() {
        roleService.checkNameable(roleId, "default角色");
    }

    @Test(expected = BusinessException.class)
    public void checkNameableCase1() {
        roleService.checkNameable(10000L, "default角色");
    }
}
