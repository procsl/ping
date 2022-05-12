package cn.procsl.ping.boot.rbac;

import cn.procsl.ping.boot.user.UserApplication;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@DisplayName("用户角色权限服务单元测试")
@Transactional
@SpringBootTest(classes = UserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Rollback
class RoleServiceTest {


    @Autowired
    RoleService roleService;

    @Autowired
    JpaRepository<Role, Long> repository;

    Faker faker = new Faker(Locale.CHINA);
    Long gid;
    List<String> permissions;

    @BeforeEach
    @Transactional
    void setUp() {
        this.permissions = Arrays.asList("读取", "写入", "查询", "按钮", "查看", "页面");
        this.gid = this.roleService.createRole(faker.name().name(), permissions);
    }

    @Test
    void createRole() {
        this.roleService.createRole(faker.name().name(), Arrays.asList("读取", "写入", "查询"));
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            String name = faker.name().name();
            this.roleService.createRole(name, Arrays.asList("读取", "写入", "查询"));
            this.roleService.createRole(name, Arrays.asList("读取", "写入"));
            this.repository.flush();
        });
    }

    @Test
    void deleteRole() {
        this.roleService.deleteRole(gid);
        Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> this.repository.getById(gid));
    }

    @Test
    void changeRolePermissions() {
        String oldName = this.repository.getById(gid).getName();

        List<String> permissions = Arrays.asList("GET https://api.procsl.cn/roles", "POST https://api.procsl.cn/roles", "DELETE https://api.procsl.cn/roles/{id}", "查看");
        this.roleService.changeRolePermissions(gid, permissions);

        Role role = this.repository.getById(gid);
        Assertions.assertEquals(oldName, role.getName());
        Assertions.assertEquals(4, role.getPermissions().size());

        Assertions.assertTrue(role.getPermissions().containsAll(permissions.stream().map(Role::createPermission).collect(Collectors.toSet())));
    }

    @Test
    void changeRoleName() {
        String oldName = this.repository.getById(gid).getName();

        this.roleService.changeRoleName(gid, faker.name().fullName());

        Role role = this.repository.getById(gid);
        Assertions.assertNotEquals(oldName, role.getName());
        Assertions.assertEquals(this.permissions.size(), role.getPermissions().size());

        Assertions.assertTrue(role.getPermissions().containsAll(permissions.stream().map(Role::createPermission).collect(Collectors.toSet())));
    }

    @Test
    void grant() {
    }
}
