package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
import cn.procsl.ping.boot.user.domain.rbac.entity.Session;
import cn.procsl.ping.boot.user.domain.rbac.entity.SessionId;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Arrays;
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
    JpaRepository<Role, Long> roleJpaRepository;

    @Test
    public void create() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        Session session = sessionService.findById(id);
        Assert.assertNotNull(session);
    }

    @Test
    public void delete() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        Session session = sessionService.findById(id);
        Assert.assertNotNull(session);

        sessionService.delete(id);
        Session session1 = sessionService.findById(id);
        Assert.assertNull(session1);
    }

    @Test
    public void disable() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        sessionService.disable(id);

        Session session = sessionService.findById(id);
        Assert.assertNotNull(session);
        Assert.assertFalse(session.isActive());
    }

    @Test
    public void enable() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        sessionService.enable(id);

        Session session = sessionService.findById(id);
        Assert.assertNotNull(session);
        Assert.assertTrue(session.isActive());
    }

    @Test
    public void bindRoles() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        Role role1 = Role.creator().name("角色1").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role2 = Role.creator().name("角色2").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role3 = Role.creator().name("角色3").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role4 = Role.creator().name("角色4").inheritBy(Role.EMPTY_ROLE_ID).done();
        roleJpaRepository.save(role1);
        roleJpaRepository.save(role2);
        roleJpaRepository.save(role3);
        roleJpaRepository.save(role4);

        Set<Long> roles = Arrays
                .asList(role1.getId(), role2.getId(), role3.getId(), role4.getId())
                .stream()
                .collect(Collectors.toSet());
        sessionService.bindRoles(id, roles);

        Session session = sessionService.findById(id);
        boolean bool = session.getRoles().containsAll(roles);

        Assert.assertTrue(bool);
    }

    @Test(expected = BusinessException.class)
    public void bindRoles2() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        Role role1 = Role.creator().name("角色1").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role2 = Role.creator().name("角色2").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role3 = Role.creator().name("角色3").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role4 = Role.creator().name("角色4").inheritBy(Role.EMPTY_ROLE_ID).done();
        roleJpaRepository.save(role1);
        roleJpaRepository.save(role2);
        roleJpaRepository.save(role3);
        roleJpaRepository.save(role4);

        Set<Long> roles = Arrays
                .asList(role1.getId(), role2.getId(), role3.getId(), role4.getId())
                .stream()
                .collect(Collectors.toSet());
        sessionService.bindRoles(id, roles);

        Session session = sessionService.findById(id);
        boolean bool = session.getRoles().containsAll(roles);

        Assert.assertTrue(bool);
    }

    @Test
    public void unbindRole() {
        SessionId id = sessionService.create();
        Assert.assertNotNull(id);

        Role role1 = Role.creator().name("角色1").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role2 = Role.creator().name("角色2").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role3 = Role.creator().name("角色3").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role4 = Role.creator().name("角色4").inheritBy(Role.EMPTY_ROLE_ID).done();
        roleJpaRepository.save(role1);
        roleJpaRepository.save(role2);
        roleJpaRepository.save(role3);
        roleJpaRepository.save(role4);

        Set<Long> roles = Arrays
                .asList(role1.getId(), role2.getId(), role3.getId(), role4.getId())
                .stream()
                .collect(Collectors.toSet());
        sessionService.bindRoles(id, roles);

        Session session = sessionService.findById(id);
        boolean bool = session.getRoles().containsAll(roles);
        Assert.assertTrue(bool);

        sessionService.unbindRole(id, role1.getId());
        sessionService.unbindRole(id, role2.getId());

        Session session1 = sessionService.findById(id);
        boolean bool1 = session1.getRoles().contains(role1.getId());
        boolean bool2 = session1.getRoles().contains(role2.getId());

        boolean bool3 = session1.getRoles().contains(role3.getId());
        boolean bool4 = session1.getRoles().contains(role4.getId());

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

        Role role1 = Role.creator().name("角色1").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role2 = Role.creator().name("角色2").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role3 = Role.creator().name("角色3").inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role4 = Role.creator().name("角色4").inheritBy(Role.EMPTY_ROLE_ID).done();
        roleJpaRepository.save(role1);
        roleJpaRepository.save(role2);
        roleJpaRepository.save(role3);
        roleJpaRepository.save(role4);
        Set<Long> roles2 = Arrays
                .asList(role1.getId(), role2.getId(), role3.getId(), role4.getId())
                .stream()
                .collect(Collectors.toSet());
        sessionService.bindRoles(id, roles2);

        Set<Long> roles3 = sessionService.getRoles(id);
        Assert.assertEquals(roles2.size(), roles3.size());
        Assert.assertTrue(roles2.containsAll(roles3));
    }
}
