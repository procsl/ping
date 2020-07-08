package cn.procsl.ping.boot.user.service.rbac;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.command.rbac.BindSessionRoleCommand;
import cn.procsl.ping.boot.user.command.rbac.CreateRoleCommand;
import cn.procsl.ping.boot.user.command.rbac.UnbindSessionRoleCommand;
import cn.procsl.ping.boot.user.domain.rbac.entity.Operation;
import cn.procsl.ping.boot.user.domain.rbac.entity.Permission;
import cn.procsl.ping.boot.user.domain.rbac.entity.Session;
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
public class SessionServiceTest {

    @Inject
    SessionService sessionService;

    @Inject
    RoleService roleService;

    private Long id;

    private Long roleId;

    private Long roleId2;

    @Before
    public void before() {
        id = sessionService.create();

        Collection<Permission> permiss = Arrays.asList(
                Permission.create(1L, Operation.READ_ONLY, null),
                Permission.create(2L, Operation.DISABLE, null),
                Permission.create(3L, Operation.WRITEABLE, null)
        );
        CreateRoleCommand command = new CreateRoleCommand("角色", null, permiss);

        roleId = roleService.create(command);

        command = new CreateRoleCommand("角色2", null, null);
        roleId2 = roleService.create(command);
    }

    @Test
    public void create() {
        Assert.assertNotNull(id);
    }

    @Test(expected = BusinessException.class)
    public void delete() {
        try {
            sessionService.delete(id);
        } catch (Exception e) {

        }
        sessionService.load(id);
    }

    @Test
    public void disable() {
        sessionService.disable(id);
        Session session = sessionService.load(id);

        Assert.assertFalse(session.isActive());

    }

    @Test
    public void enable() {
        sessionService.enable(id);
        Session session = sessionService.load(id);

        Assert.assertTrue(session.isActive());
    }

    @Test(expected = NullPointerException.class)
    public void bindRoles() {
        BindSessionRoleCommand command = null;
        sessionService.bindRoles(command);
    }

    @Test
    public void bindRolesCase() {
        BindSessionRoleCommand command = new BindSessionRoleCommand(id, Collections.singleton(roleId));
        sessionService.bindRoles(command);

        Session session = sessionService.load(command.getSessionId());
        boolean bool = session.getRoles().contains(roleId);
        Assert.assertTrue(bool);
    }

    @Test
    public void unbindRole() {
        bindRolesCase();

        UnbindSessionRoleCommand command = new UnbindSessionRoleCommand(id, roleId);
        sessionService.unbindRole(command);

        Session session = sessionService.load(command.getSessionId());
        boolean bool = session.getRoles().contains(roleId);
        Assert.assertFalse(bool);
    }

    @Test
    public void getDirectRoles() {
        bindRolesCase();
        Session session = sessionService.load(id);

        Assert.assertTrue(session.getRoles().contains(roleId));
    }
}
