package cn.procsl.ping.boot.infra.domain.rbac;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RoleTest {

    @Test
    @DisplayName("测试修改权限")
    public void changePermissions() {
        Role role = new Role("role");

        Assertions.assertEquals(0, role.getPermissions().size());

        List<String> permissions = Arrays.asList("1", "2", "3");
        role.changePermissions(permissions);

        Assertions.assertEquals(3, role.getPermissions().size());
        Set<Permission> c2 = role.getPermissions();
        Assertions.assertArrayEquals(permissions.stream().map(Role::createPermission).distinct().toArray(), c2.toArray());

    }

    @Test
    void createPermission() {
        Permission permission = Role.createPermission("permission");
        Assertions.assertEquals("permission", permission.getName());
    }


    @Test
    void change() {
        Role role = new Role("role", List.of("permission"));
        role.change("test", List.of("1", "2", "3"));
        Assertions.assertEquals("test", role.getName());
        Assertions.assertTrue(Set.of(Role.createPermission("1"), Role.createPermission("2"), Role.createPermission("3")).containsAll(role.getPermissions()));

    }
}