package cn.procsl.ping.boot.user.domain.rbac.entity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author procsl
 * @date 2020/07/08
 */
public class RoleTest {

    private Role role;
    private List<Permission> permissions;

    @Before
    public void setUp() throws Exception {
        this.role = Role.create("角色");

        this.permissions = Arrays.asList(
                Permission.create(1L, Operation.READ_ONLY, null),
                Permission.create(2L, Operation.READ_ONLY, null),
                Permission.create(3L, Operation.READ_ONLY, null)
        );
    }

    @Test
    public void create() {
        Role.create("角色");
    }

    @Test(expected = NullPointerException.class)
    public void createCase1() {
        Role.create(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCase2() {
        Role.create("");
    }

    @Test
    public void testCreate() {
        Role.create("角色", role);
    }

    @Test
    public void testCreateCase1() {
        Role.create("角色", null);
    }

    @Test
    public void testCreate1Case1() {
        Role.create("角色", role, this.permissions);
    }

    @Test
    public void testCreate1Case2() {
        Role.create("角色", role, Collections.EMPTY_SET);
    }

    @Test
    public void changeInherit() {
        Role parent = Role.create("test");
        this.role.changeInherit(parent);

        boolean bool = this.role.getInherit() == parent;
        Assert.assertTrue(bool);
    }

    @Test
    public void changeName() {
        this.role.changeName("name");

        boolean bool = this.role.getName().equals("name");
        Assert.assertTrue(bool);
    }

    @Test
    public void grantPermission() {
        this.role.grantPermission(this.permissions.get(0));
        Assert.assertTrue(this.role.getPermissions().contains(this.permissions.get(0)));
        Assert.assertFalse(this.role.getPermissions().contains(this.permissions.get(1)));
    }

    @Test
    public void revokePermission() {
        this.grantPermission();
        this.role.revokePermission(this.permissions.get(0));
        Assert.assertFalse(this.role.getPermissions().contains(this.permissions.get(0)));
    }

    @Test
    public void hasPermission() {
        this.grantPermission();
        boolean bool = this.role.hasPermission(this.permissions.get(0));

        boolean bool1 = this.role.hasPermission(this.permissions.get(1));
        Assert.assertTrue(bool);
        Assert.assertFalse(bool1);
    }
}
