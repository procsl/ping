package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.user.domain.rbac.model.Node;
import cn.procsl.ping.boot.user.domain.rbac.model.Permission;
import cn.procsl.ping.boot.user.domain.rbac.model.Role;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class SubjectServiceTest {

    @Inject
    SubjectService subjectService;

    @Inject
    PermissionService permissionService;

    @Inject
    EntityManager entityManager;

    @Inject
    RoleService roleService;

    @Inject
    AdjacencyTreeRepository<Role, Long, Node> treeRepository;
    private Long admin;
    private Long admin1;
    private Long admin2;

    private List<Target> list;

    private Long id;

    private List<Long> perms;

    @Before
    public void setUp() throws Exception {
        list = Arrays.asList(
            Target.toTarget("1298", "read"),
            Target.toTarget("1299", "read"),
            Target.toTarget("1300", "read")
        );
        for (Target target : list) {
            this.permissionService.create(target);
        }
        perms = this.permissionService
            .getByTargets(list)
            .stream()
            .map(Permission::getId)
            .collect(Collectors.toList());
        id = roleService.create("超级管理员", (Long) null, null);
        entityManager.clear();
    }

    @Test
    public void create() {
        admin = roleService.create("管理员", "超级管理员", list);
        admin1 = roleService.create("管理员1", "超级管理员", list);
        admin2 = roleService.create("管理", "超级管理员/管理员", list);
    }

    @Test
    public void createByRoleId() {
        Long i = roleService.create("这这这", id, perms);
        Assert.assertNotNull(i);
        Role role = roleService.getOne(i);
        Assert.assertEquals(role.getName(), "这这这");
        Assert.assertEquals(role.getParentId(), id);
    }

    @Test
    public void changeRole() {
        roleService.changePermission(id, list);
        Role role = roleService.getOne(id);

        Collection<Permission> ls = permissionService.getByTargets(list);

        Assert.assertEquals(role.getPermission().size(), ls.size());
        for (Permission l : ls) {
            boolean bool = role.getPermission().contains(l);
            Assert.assertTrue(bool);
        }

        for (Permission permission : role.getPermission()) {
            boolean bool = ls.contains(permission);
            Assert.assertTrue(bool);
        }
    }

    @Test
    public void changeRoleById() {
        this.roleService.changePermissionById(id, perms);
        Role role = roleService.getOne(id);

        Collection<Permission> ls = permissionService.getByTargets(list);

        Assert.assertEquals(role.getPermission().size(), ls.size());
        for (Permission l : ls) {
            boolean bool = role.getPermission().contains(l);
            Assert.assertTrue(bool);
        }

        for (Permission permission : role.getPermission()) {
            boolean bool = ls.contains(permission);
            Assert.assertTrue(bool);
        }
    }
}
