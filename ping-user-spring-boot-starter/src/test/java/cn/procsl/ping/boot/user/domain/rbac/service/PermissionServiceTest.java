package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.user.domain.rbac.model.Permission;
import cn.procsl.ping.boot.user.domain.rbac.model.Target;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class PermissionServiceTest {

    @Inject
    PermissionService permissionService;

    @Inject
    EntityManager entityManager;

    private Collection<Long> permissions;

    @Before
    public void before() {
        permissions = permissionService.getByTargets(
            Arrays.asList(
                new TargetValue("菜单1", "readonly"),
                new TargetValue("菜单1", "edit"),
                new TargetValue("菜单2", "all")
            )
        ).stream().map(Permission::getId).collect(Collectors.toUnmodifiableList());
        entityManager.clear();
    }

    @Test
    public void getByTargets() {
        testCreate();
    }

    @Test
    public void testCreate() {

        List<Target> list = Arrays.asList(
            new TargetValue("菜单1", "readonly"),
            new TargetValue("菜单1", "edit"),
            new TargetValue("菜单2", "all")
        );

        List<Long> ids = new ArrayList<>();
        for (Target value : list) {
            Long id = this.permissionService.create(value);
            ids.add(id);
        }
        entityManager.flush();
        entityManager.clear();

        permissions = permissionService.getByTargets(list).stream().map(Permission::getId).collect(Collectors.toUnmodifiableList());
        for (Long i : ids) {
            boolean bool = permissions.contains(i);
            Assert.assertTrue(bool);
        }

        for (Long permission : permissions) {
            boolean bool = ids.contains(permission);
            Assert.assertTrue(bool);
        }
    }

    @Test
    public void testCreate1() {
        Long id = this.permissionService.create("resource", "operator");
        entityManager.flush();
        entityManager.clear();

        TargetValue ta = new TargetValue("resource", "operator");
        permissions = permissionService.getByTargets(Collections.singleton(ta)).stream().map(Permission::getId).collect(Collectors.toUnmodifiableList());

        boolean bool = permissions.contains(id);
        Assert.assertTrue(bool);
        Assert.assertEquals(1, permissions.size());
    }
}
