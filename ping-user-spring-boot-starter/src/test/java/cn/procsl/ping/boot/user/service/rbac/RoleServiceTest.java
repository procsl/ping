package cn.procsl.ping.boot.user.service.rbac;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.command.rbac.BindSessionRoleCommand;
import cn.procsl.ping.boot.user.command.rbac.ChangeRoleInheritCommand;
import cn.procsl.ping.boot.user.command.rbac.CreateRoleCommand;
import cn.procsl.ping.boot.user.command.rbac.RenameRoleCommand;
import cn.procsl.ping.boot.user.domain.rbac.entity.Operation;
import cn.procsl.ping.boot.user.domain.rbac.entity.Permission;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author procsl
 * @date 2020/07/08
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
public class RoleServiceTest {

    @Inject
    RoleService roleService;

    @Inject
    SessionService sessionService;

    private Long roleId1;
    private Long roleId2;
    private Long sessionId;

    @Before
    public void before() {
        Collection<Permission> permiss = Arrays.asList(
                Permission.create(1L, Operation.READ_ONLY, null),
                Permission.create(2L, Operation.DISABLE, null),
                Permission.create(3L, Operation.WRITEABLE, null)
        );
        CreateRoleCommand command = new CreateRoleCommand("角色", null, permiss);

        Collection<Permission> permiss2 = Arrays.asList(
                Permission.create(4L, Operation.WRITEABLE, null)
        );
        CreateRoleCommand command2 = new CreateRoleCommand("角色2", roleId1, permiss2);

        roleId1 = roleService.create(command);
        roleId2 = roleService.create(command2);


        sessionId = sessionService.create();
        BindSessionRoleCommand sessionRoleCommand = new BindSessionRoleCommand(sessionId, Collections.singleton(roleId2));
        sessionService.bindRoles(sessionRoleCommand);
    }

    @Test
    public void delete() {
        roleService.delete(roleId1);
    }

    @Test(expected = BusinessException.class)
    public void deleteCase() {
        roleService.delete(roleId2);
    }

    @Test
    public void rename() {
        RenameRoleCommand command = new RenameRoleCommand(roleId1, "newName");
        roleService.rename(command);

        Assert.assertTrue(roleService.load(roleId1).getName().equals("newName"));
    }

    @Test
    public void changeInherit() {
        @NonNull @Valid ChangeRoleInheritCommand command = new ChangeRoleInheritCommand(roleId1, roleId2);
        roleService.changeInherit(command);
        Assert.assertTrue(roleService.load(roleId1).getInherit().getId().equals(roleId2));
    }

    @Test
    public void changeInheritCase() {
        ChangeRoleInheritCommand command = new ChangeRoleInheritCommand(roleId2, null);
        roleService.changeInherit(command);

        Assert.assertTrue(roleService.load(roleId2).getInherit() == null);
    }

    @Test
    public void checkNameable() {
        RenameRoleCommand command = new RenameRoleCommand(roleId1, "你好啊");
        roleService.checkNameable(command);
    }

    @Test
    public void checkNameableCase1() {
        RenameRoleCommand command = new RenameRoleCommand(roleId1, "角色");
        roleService.checkNameable(command);
    }

    @Test(expected = BusinessException.class)
    public void checkNameableCase2() {
        RenameRoleCommand command = new RenameRoleCommand(roleId1, "角色2");
        roleService.checkNameable(command);
    }
}
