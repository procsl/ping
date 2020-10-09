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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.procsl.ping.boot.user.domain.rbac.service.RoleService.R;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class RoleServiceTest {

    @Inject
    RoleService roleService;

    @Inject
    PermissionService permissionService;

    @Inject
    EntityManager entityManager;

    @Inject
    AdjacencyTreeRepository<Role, Long, Node> treeRepository;

    private Long mainMenuId;

    private Long userMenuId;

    private Long menuMenuId;

    private Long roleMenuId;

    private Long root;


    @Before
    public void setUp() throws Exception {
        mainMenuId = permissionService.create("菜单1", "读");
        userMenuId = permissionService.create("菜单2", "写");
        menuMenuId = permissionService.create("菜单2", "read");
        roleMenuId = permissionService.create("菜单3", "删除");

        permissionService.create("菜单管理权限", "读");
        permissionService.create("角色管理权限", "读");

        root = roleService.create("超级管理员", null, Arrays.asList(mainMenuId, userMenuId, menuMenuId));
        entityManager.clear();
    }

    @Test
    public void disable() {
        int i = this.roleService.disable(root);
        log.info("update:{}", i);
        Role role = this.roleService.getOne(root);
        Assert.assertFalse(role.getState());
    }

    @Test
    public void enable() {
        int i = this.roleService.enable(root);
        log.info("update:{}", i);
        Role role = this.roleService.getOne(root);
        Assert.assertTrue(role.getState());
    }

    @Test
    public void changeState() {
        entityManager.clear();
        int i = this.roleService.changeState(root, false);
        log.info("update:{}", i);
        Role role = this.roleService.getOne(root);
        Assert.assertFalse(role.getState());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void create() {

        {
            Long roleId = roleService.create("运营角色", root, Arrays.asList(menuMenuId, userMenuId));
            entityManager.clear();
            Assert.assertNotNull(roleId);
            Role role = this.roleService.getOne(roleId);
            Set<Permission> perms = role.getPermission();

            Set<Long> permsId = perms.stream().map(Permission::getId).collect(Collectors.toSet());
            Assert.assertTrue(permsId.contains(menuMenuId));
            Assert.assertTrue(permsId.contains(userMenuId));
            Assert.assertFalse(permsId.contains(mainMenuId));
            Assert.assertFalse(permsId.contains(roleMenuId));
        }

        {
            Long roleId = roleService.create("其他角色", root, Arrays.asList(menuMenuId, userMenuId, mainMenuId));
            entityManager.clear();
            Assert.assertNotNull(roleId);
            Role role = this.roleService.getOne(roleId);
            Set<Permission> perms = role.getPermission();

            List<Long> permsId = perms.stream().map(Permission::getId).collect(Collectors.toList());
            Assert.assertFalse(permsId.contains(menuMenuId));
            Assert.assertFalse(permsId.contains(userMenuId));
            Assert.assertTrue(permsId.contains(mainMenuId));
            Assert.assertEquals(1, permsId.size());
        }

        {
            Long roleId = roleService.create("其他角色1", root, null);
            entityManager.clear();
            Assert.assertNotNull(roleId);
            Role role = this.roleService.getOne(roleId);
            Set<Permission> perms = role.getPermission();
            Assert.assertTrue(perms.isEmpty());
        }

        {
            roleService.create("其他角色", root, null);
        }

    }

    @Test
    public void testCreate() {
        Long roleId = roleService.create("角色1", root, null);
        {
            List<Long> parents = this.treeRepository.getParents(R.id, roleId).collect(Collectors.toList());
            Assert.assertTrue(parents.contains(roleId));
            Assert.assertTrue(parents.contains(root));
            Assert.assertEquals(2, parents.size());
            entityManager.clear();
        }

        {
            List<Long> children = this.treeRepository.getAllChildren(R.id, roleId).collect(Collectors.toList());
            Assert.assertTrue(children.isEmpty());
            entityManager.clear();
        }

        {
            List<Long> rootChildren = this.treeRepository.getDirectChildren(R.id, root).collect(Collectors.toList());
            boolean bool = rootChildren.contains(roleId);
            Assert.assertTrue(bool);
        }
    }

    @Test
    public void changName() {
        this.roleService.changName(root, "角色啊");
        Role rootRole = this.roleService.getOne(root);
        Assert.assertEquals("角色啊", rootRole.getName());

        this.roleService.changName(root, "others");
        rootRole = this.roleService.getOne(root);
        Assert.assertEquals("others", rootRole.getName());
    }

    @Test()
    public void changePermission() {
        {


            List<Target> olds = Arrays.asList(
                Target.toTarget("菜单管理权限", "读"),
                Target.toTarget("角色管理权限", "读")
            );
            this.roleService.changePermission(root, olds);
            this.entityManager.flush();

            Role role = this.roleService.getOne(root);

            Assert.assertTrue(role.hasPermission(olds.get(0)));
            Assert.assertTrue(role.hasPermission(olds.get(1)));

            Assert.assertEquals(2, role.getPermission().size());
        }
    }

    @Test
    public void changePermissionById() {
        {

            this.roleService.changePermissionById(root, Arrays.asList(menuMenuId, roleMenuId));

            List<Long> rooted = this.roleService.getOne(root).getPermission().stream().map(Permission::getId).collect(Collectors.toList());

            Assert.assertTrue(rooted.contains(menuMenuId));
            Assert.assertTrue(rooted.contains(roleMenuId));

            Assert.assertEquals(2, rooted.size());
        }

        {

            this.roleService.changePermissionById(root, Arrays.asList(menuMenuId, roleMenuId, 200000000L));

            List<Long> rooted = this.roleService.getOne(root).getPermission().stream().map(Permission::getId).collect(Collectors.toList());

            Assert.assertTrue(rooted.contains(menuMenuId));
            Assert.assertTrue(rooted.contains(roleMenuId));

            Assert.assertEquals(2, rooted.size());
        }
    }

    @Test
    public void searchOne() {
        roleService.create("管理员", root, Arrays.asList(mainMenuId, userMenuId));
        entityManager.clear();
        Role admin = this.roleService.searchOne("超级管理员/管理员");
        Assert.assertEquals("管理员", admin.getName());
        Assert.assertEquals(root, admin.getParentId());
    }


}
