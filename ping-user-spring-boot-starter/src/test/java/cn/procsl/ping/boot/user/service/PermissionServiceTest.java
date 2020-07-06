package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.user.domain.rbac.entity.ResourceType;
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
import java.util.Set;

/**
 * @author procsl
 * @date 2020/07/06
 */
@Slf4j
@Transactional
@SpringBootTest
@RunWith(SpringRunner.class)
public class PermissionServiceTest {

    @Inject
    PermissionService permissionService;

    @Inject
    RoleService roleService;

    @Inject
    ResourceService resourceService;

    private Long resourceId;

    private Long roleId;

    @Before
    public void before() {
        this.roleId = roleService.create("角色", null);
        this.resourceId = resourceService.create("资源", ResourceType.BUTTON, null, null);
    }

    @Test
    public void grant() {
        permissionService.grant(roleId, resourceId);

        Role role = roleService.load(roleId);

        boolean bool = role.getPermissions().contains(resourceId);
        Assert.assertTrue(bool);
    }

    @Test
    public void revoke() {
        grant();

        permissionService.revoke(roleId, resourceId);

        Role role = roleService.load(roleId);
        boolean bool = role.getPermissions().contains(resourceId);
        Assert.assertFalse(bool);
    }

    @Test
    public void has() {
        grant();

        permissionService.has(roleId, resourceId);
    }

    @Test
    public void getPermissions() {
        Set<Long> permissions = permissionService.getPermissions(roleId);
        Assert.assertTrue(permissions.isEmpty());

        grant();
        Set<Long> permissions1 = permissionService.getPermissions(roleId);
        Assert.assertFalse(permissions1.isEmpty());
    }
}
