package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.user.domain.rbac.entity.Identity;
import cn.procsl.ping.boot.user.domain.rbac.entity.IdentityId;
import cn.procsl.ping.boot.user.domain.rbac.entity.Role;
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
public class IdentityServiceTest {

    @Inject
    IdentityService identityService;

    @Inject
    JpaRepository<Role, Long> roleJpaRepository;

    @Test
    public void create() {
        IdentityId id = identityService.create();
        Assert.assertNotNull(id);

        Identity identity = identityService.findById(id);
        Assert.assertNotNull(identity);
    }

    @Test
    public void delete() {
        IdentityId id = identityService.create();
        Assert.assertNotNull(id);

        Identity identity = identityService.findById(id);
        Assert.assertNotNull(identity);

        identityService.delete(id);
        Identity identity2 = identityService.findById(id);
        Assert.assertNull(identity2);
    }

    @Test
    public void disable() {
        IdentityId id = identityService.create();
        Assert.assertNotNull(id);

        identityService.disable(id);

        Identity identity = identityService.findById(id);
        Assert.assertNotNull(identity);
        Assert.assertFalse(identity.isActive());
    }

    @Test
    public void enable() {
        IdentityId id = identityService.create();
        Assert.assertNotNull(id);

        identityService.enable(id);

        Identity identity = identityService.findById(id);
        Assert.assertNotNull(identity);
        Assert.assertTrue(identity.isActive());
    }

    @Test
    public void bindRoles() {
        IdentityId id = identityService.create();
        Assert.assertNotNull(id);

        Role role1 = Role.creator().name("角色1").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role2 = Role.creator().name("角色2").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role3 = Role.creator().name("角色3").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role4 = Role.creator().name("角色4").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        roleJpaRepository.save(role1);
        roleJpaRepository.save(role2);
        roleJpaRepository.save(role3);
        roleJpaRepository.save(role4);

        Set<Long> roles = Arrays
                .asList(role1.getId(), role2.getId(), role3.getId(), role4.getId())
                .stream()
                .collect(Collectors.toSet());
        identityService.bindRoles(id, roles);

        Identity identity = identityService.findById(id);
        boolean bool = identity.getRoles().containsAll(roles);

        Assert.assertTrue(bool);
    }

    @Test
    public void unbindRole() {
        IdentityId id = identityService.create();
        Assert.assertNotNull(id);

        Role role1 = Role.creator().name("角色1").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role2 = Role.creator().name("角色2").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role3 = Role.creator().name("角色3").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role4 = Role.creator().name("角色4").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        roleJpaRepository.save(role1);
        roleJpaRepository.save(role2);
        roleJpaRepository.save(role3);
        roleJpaRepository.save(role4);

        Set<Long> roles = Arrays
                .asList(role1.getId(), role2.getId(), role3.getId(), role4.getId())
                .stream()
                .collect(Collectors.toSet());
        identityService.bindRoles(id, roles);

        Identity identity = identityService.findById(id);
        boolean bool = identity.getRoles().containsAll(roles);
        Assert.assertTrue(bool);

        identityService.unbindRole(id, role1.getId());
        identityService.unbindRole(id, role2.getId());

        Identity identity2 = identityService.findById(id);
        boolean bool1 = identity2.getRoles().contains(role1.getId());
        boolean bool2 = identity2.getRoles().contains(role2.getId());

        boolean bool3 = identity2.getRoles().contains(role3.getId());
        boolean bool4 = identity2.getRoles().contains(role4.getId());

        Assert.assertFalse(bool1);
        Assert.assertFalse(bool2);

        Assert.assertTrue(bool3);
        Assert.assertTrue(bool4);
    }

    @Test
    public void getRoles() {
        IdentityId id = identityService.create();
        Assert.assertNotNull(id);

        Set<Long> roles = identityService.getRoles(id);
        Assert.assertEquals(roles.size(), 0);

        Role role1 = Role.creator().name("角色1").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role2 = Role.creator().name("角色2").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role3 = Role.creator().name("角色3").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        Role role4 = Role.creator().name("角色4").max(10).inheritBy(Role.EMPTY_ROLE_ID).done();
        roleJpaRepository.save(role1);
        roleJpaRepository.save(role2);
        roleJpaRepository.save(role3);
        roleJpaRepository.save(role4);
        Set<Long> roles2 = Arrays
                .asList(role1.getId(), role2.getId(), role3.getId(), role4.getId())
                .stream()
                .collect(Collectors.toSet());
        identityService.bindRoles(id, roles2);

        Set<Long> roles3 = identityService.getRoles(id);
        Assert.assertEquals(roles2.size(), roles3.size());
        Assert.assertTrue(roles2.containsAll(roles3));
    }
}
