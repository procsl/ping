package cn.procsl.ping.boot.infra.domain.rbac;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class RoleTest {

    @Test
    @DisplayName("测试修改权限")
    public void changePermissions() {
        Role role = new Role("role");

        Assertions.assertEquals(0, role.getPermissions().size());

        List<Permission> permissions = Arrays.asList(
                HttpPermission.create("post", "/v1"),
                HttpPermission.create("post", "/v2"),
                HttpPermission.create("post", "/v3")
        );
        role.changePermissions(permissions);

        Assertions.assertEquals(3, role.getPermissions().size());
        val c2 = role.getPermissions();
        Assertions.assertArrayEquals(permissions.stream().distinct().toArray(), c2.toArray());

    }


    @Test
    void change() {
        Role role = new Role("role", List.of(HttpPermission.create("get", "/v1/test")));
        role.change("test", List.of(
                HttpPermission.create("get", "/v1/test"),
                HttpPermission.create("post", "/v1/test"),
                HttpPermission.create("delete", "/v1/test")
        ));
        Assertions.assertEquals("test", role.getName());

        Assertions.assertEquals(3, role.permissions.size());

    }
}