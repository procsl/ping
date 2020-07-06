package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.user.domain.rbac.entity.Session;
import cn.procsl.ping.boot.user.domain.rbac.entity.SessionId;
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
import java.util.stream.Collectors;

/**
 * @author procsl
 * @date 2020/07/02
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

    private Set<Long> roles;

    @Before
    public void before() {
        this.roles = Arrays
                .asList(
                        roleService.create("role1", null),
                        roleService.create("role2", null),
                        roleService.create("role3", null),
                        roleService.create("role4", null)
                )
                .stream()
                .collect(Collectors.toSet());
    }

    @Test
    public void create() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        Session session = sessionService.load(id);
        Assert.assertNotNull(session);
    }

    @Test
    public void delete() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        Session session = sessionService.load(id);
        Assert.assertNotNull(session);

        sessionService.delete(id);
        Session session1 = sessionService.load(id);
        Assert.assertNull(session1);
    }

    @Test
    public void disable() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        sessionService.disable(id);

        Session session = sessionService.load(id);
        Assert.assertNotNull(session);
        Assert.assertFalse(session.isActive());
    }

    @Test
    public void enable() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        sessionService.enable(id);

        Session session = sessionService.load(id);
        Assert.assertNotNull(session);
        Assert.assertTrue(session.isActive());
    }

    @Test
    public void bindRoles() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        sessionService.bindRoles(id, roles);

        Session session = sessionService.load(id);
        boolean bool = session.getRoles().containsAll(roles);

        Assert.assertTrue(bool);
    }

    @Test
    public void bindRoles2() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        sessionService.bindRoles(id, roles);

        Session session = sessionService.load(id);
        boolean bool = session.getRoles().containsAll(roles);

        Assert.assertTrue(bool);
    }

    @Test
    public void unbindRole() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        sessionService.bindRoles(id, roles);

        Session session = sessionService.load(id);
        boolean bool = session.getRoles().containsAll(roles);
        Assert.assertTrue(bool);

        List<Long> list = roles.stream().collect(Collectors.toList());

        sessionService.unbindRole(id, list.get(0));
        sessionService.unbindRole(id, list.get(1));

        Session session1 = sessionService.load(id);
        boolean bool1 = session1.getRoles().contains(list.get(0));
        boolean bool2 = session1.getRoles().contains(list.get(1));

        boolean bool3 = session1.getRoles().contains(list.get(2));
        boolean bool4 = session1.getRoles().contains(list.get(3));

        Assert.assertFalse(bool1);
        Assert.assertFalse(bool2);

        Assert.assertTrue(bool3);
        Assert.assertTrue(bool4);
    }

    @Test
    public void getRoles() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        Set<Long> roles = sessionService.getRoles(id);
        Assert.assertEquals(roles.size(), 0);

        sessionService.bindRoles(id, this.roles);

        Set<Long> roles3 = sessionService.getRoles(id);
        Assert.assertEquals(this.roles.size(), roles3.size());
        Assert.assertTrue(this.roles.containsAll(roles3));
    }

}
