package cn.procsl.ping.boot.user.service.rbac;

import cn.procsl.ping.boot.user.command.rbac.CreateRoleCommand;
import cn.procsl.ping.boot.user.command.rbac.PermissionCommand;
import cn.procsl.ping.boot.user.domain.rbac.entity.Operation;
import cn.procsl.ping.boot.user.domain.rbac.entity.Permission;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author procsl
 * @date 2020/07/08
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
public class PermissionServiceTest {

    @Inject
    PermissionService permissionService;

    @Inject
    RoleService roleService;
    private Long roleId1;
    private Long roleId2;
    private List<Permission> permiss;
    private List<Permission> permiss2;

    @Before
    public void before() {
        permiss = Arrays.asList(
                Permission.create(1L, Operation.READ_ONLY, null),
                Permission.create(2L, Operation.DISABLE, null),
                Permission.create(3L, Operation.WRITEABLE, null)
        );
        CreateRoleCommand command = new CreateRoleCommand("角色", null, permiss);

        permiss2 = Arrays.asList(
                Permission.create(4L, Operation.WRITEABLE, null)
        );
        CreateRoleCommand command2 = new CreateRoleCommand("角色2", null, permiss2);

        roleId1 = roleService.create(command);
        roleId2 = roleService.create(command2);
    }

    @Test
    public void grant() {
        PermissionCommand command = new PermissionCommand(roleId2, permiss.get(0));
        permissionService.grant(command);

        Role role = roleService.load(command.getRoleId());
        Assert.assertTrue(role.getPermissions().contains(permiss.get(0)));
        Assert.assertTrue(role.getPermissions().contains(permiss2.get(0)));
    }

    @Test
    public void revoke() {
        PermissionCommand command = new PermissionCommand(roleId2, permiss2.get(0));
        permissionService.revoke(command);

        Role role = roleService.load(command.getRoleId());
        Assert.assertFalse(role.getPermissions().contains(permiss2.get(0)));
    }

    @Test
    public void has() {
        PermissionCommand command = new PermissionCommand(roleId2, permiss2.get(0));
        boolean bool = permissionService.has(command);
        Assert.assertTrue(bool);
    }

    @Test
    public void getPermissions() {
        Set<Permission> perm = permissionService.getPermissions(roleId2);
        Assert.assertTrue(perm.contains(permiss2.get(0)));
    }
}
