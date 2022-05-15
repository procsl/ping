package cn.procsl.ping.boot.infra.rbac;

import cn.procsl.ping.boot.infra.InfraApplication;
import com.github.javafaker.Faker;
import com.github.jsonzou.jmockdata.JMockData;
import com.github.jsonzou.jmockdata.MockConfig;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@DisplayName("用户角色权限服务单元测试")
@Transactional
@SpringBootTest(classes = InfraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Rollback
public class RoleServiceTest {

    @Autowired
    RoleService roleService;

    @Autowired
    JpaRepository<Role, Long> repository;

    @Autowired
    JpaRepository<Subject, Long> subjectLongJpaRepository;

    Faker faker = new Faker(Locale.CHINA);
    Long gid;
    List<String> permissions;
    MockConfig config;


    @BeforeEach
    @Transactional
    @DisplayName("初始化")
    void setUp() {
        val permissions = Arrays.asList("读取", "写入", "查询", "按钮", "查看", "页面");
        this.gid = this.roleService.createRole(faker.name().name(), permissions);
        this.repository.flush();
        this.permissions = new ArrayList<>(permissions);
        this.permissions.addAll(Arrays.asList(JMockData.mock(String[].class)));
        this.config = new MockConfig().longRange(1, Long.MAX_VALUE).intRange(0, this.permissions.size());
    }

    @Test
    @DisplayName("测试创建角色")
    public void createRole() {
        this.roleService.createRole(faker.name().fullName(), Collections.emptySet());
        this.roleService.createRole(faker.name().name(), Arrays.asList("读取", "写入", "查询"));
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            String name = faker.name().name();
            this.roleService.createRole(name, Arrays.asList("读取", "写入", "查询"));
            this.roleService.createRole(name, Arrays.asList("读取", "写入"));
            this.repository.flush();
        });
    }

    @Test
    @DisplayName("测试删除角色")
    public void deleteRole() {
        this.roleService.deleteRole(gid);
        Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> this.repository.getById(gid));
    }

    @Test
    @DisplayName("测试修改角色权限")
    public void changeRolePermissions() {
        String oldName = this.repository.getById(gid).getName();

        List<String> permissions = Arrays.asList("GET https://api.procsl.cn/roles", "POST https://api.procsl.cn/roles", "DELETE https://api.procsl.cn/roles/{id}", "查看");
        this.roleService.changeRolePermissions(gid, permissions);

        Role role = this.repository.getById(gid);
        Assertions.assertEquals(oldName, role.getName());
        Assertions.assertEquals(4, role.getPermissions().size());

        Assertions.assertTrue(role.getPermissions().containsAll(permissions.stream().map(Role::createPermission).collect(Collectors.toSet())));
    }

    @Test
    @DisplayName("测试修改角色名称")
    public void changeRoleName() {
        Role old = this.repository.getById(gid);
        String oldName = old.getName();
        Set<Permission> oldPermissions = old.getPermissions();

        this.roleService.changeRoleName(gid, faker.name().fullName());

        Role role = this.repository.getById(gid);
        Assertions.assertNotEquals(oldName, role.getName());
        Assertions.assertEquals(oldPermissions.size(), role.getPermissions().size());
        Assertions.assertTrue(oldPermissions.containsAll(role.getPermissions()));

        String name = faker.animal().name();
        this.roleService.createRole(name, Collections.emptySet());
        Assertions.assertThrows(ConstraintViolationException.class, () -> this.roleService.changeRoleName(gid, name), "角色已存在");
    }


    List<String> getPermissions() {

        Integer index1 = JMockData.mock(Integer.class, config);
        Integer index2 = JMockData.mock(Integer.class, config);

        if (index1 < index2) {
            return this.permissions.subList(index1, index2);
        } else if (index1 > index2) {
            return this.permissions.subList(index2, index1);
        } else if (index1 == 0) {
            return this.permissions.subList(index1, index2 + 1);
        } else {
            return this.permissions.subList(index1 - 1, index2);
        }
    }
}
