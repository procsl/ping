package cn.procsl.ping.boot.rbac;

import cn.procsl.ping.boot.user.UserApplication;
import cn.procsl.ping.exception.BusinessException;
import com.github.javafaker.Faker;
import com.github.jsonzou.jmockdata.JMockData;
import com.github.jsonzou.jmockdata.MockConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@DisplayName("用户角色权限服务单元测试")
@Transactional
@SpringBootTest(classes = UserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
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
    void setUp() {
        val permissions = Arrays.asList("读取", "写入", "查询", "按钮", "查看", "页面");
        this.gid = this.roleService.createRole(faker.name().name(), permissions);
        this.repository.flush();
        this.permissions = new ArrayList<>(permissions);
        this.permissions.addAll(Arrays.asList(JMockData.mock(String[].class)));
        this.config = new MockConfig().longRange(1, Long.MAX_VALUE).intRange(0, this.permissions.size());
    }

    @Test
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
    public void deleteRole() {
        this.roleService.deleteRole(gid);
        Assertions.assertThrows(JpaObjectRetrievalFailureException.class, () -> this.repository.getById(gid));
    }

    @Test
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
    public void changeRoleName() {
        String oldName = this.repository.getById(gid).getName();

        this.roleService.changeRoleName(gid, faker.name().fullName());

        Role role = this.repository.getById(gid);
        Assertions.assertNotEquals(oldName, role.getName());
        Assertions.assertEquals(this.permissions.size(), role.getPermissions().size());

        Assertions.assertTrue(role.getPermissions().containsAll(permissions.stream().map(Role::createPermission).collect(Collectors.toSet())));

        String name = faker.animal().name();
        this.roleService.createRole(name, Collections.emptySet());
        Assertions.assertThrows(ConstraintViolationException.class, () -> this.roleService.changeRoleName(gid, name), "角色已存在");
    }

    @Test

    public void grant() {

        log.info("开始测试角色授予");

        Long subject = JMockData.mock(Long.class, config);
        log.info("Subject ID = {}", subject);

        List<Role> allRoles = this.repository.findAll();
        log.info("全部角色列表为:{}", allRoles);

        List<String> allRoleNames = allRoles.stream().map(Role::getName).collect(Collectors.toList());
        log.info("为[{}]授予角色:[{}]", subject, allRoleNames);
        this.roleService.grant(subject, allRoleNames);
        log.info("授予完成");

        {
            Optional<Subject> optional = this.subjectLongJpaRepository.findOne(Example.of(new Subject(subject)));
            log.info("获取Subject:[{}]", optional);
            Assertions.assertTrue(optional.isPresent());
            optional.ifPresent(item -> {
                List<String> roles = item.getRoles().stream().map(Role::getName).collect(Collectors.toList());
                log.info("Subject角色为:[{}]", roles);
                Assertions.assertTrue(allRoleNames.containsAll(roles));
            });
        }
        String role = faker.name().username();
        log.info("测试授予不存在的角色:[{}]", role);
        Assertions.assertThrows(BusinessException.class, () -> this.roleService.grant(subject, Collections.singleton(role)));

        log.info("测试成功");
    }

    @Test
    public void hasPermissions() {
        log.info("创建多个角色");
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());
        this.roleService.createRole(JMockData.mock(String.class, config), getPermissions());

        List<String> perms = getPermissions();
        this.roleService.createRole("测试角色", perms);

        List<Role> allRoles = this.repository.findAll();
        List<String> allRoleNames = allRoles.stream().map(Role::getName).collect(Collectors.toList());
        log.info("所有角色:{}", allRoleNames);
        log.info("-------------------------------");

        for (Role role : allRoles) {
            log.info("[{}] 对应的权限为:{}", role.getName(), role.getPermissions().stream().map(Permission::getName).collect(Collectors.toList()));
        }
        log.info("-------------------------------");
        long id = JMockData.mock(Long.class, config);

        log.info("测试当前subject无任何一项权限:{}", perms);
        for (String permission : perms) {
            log.info("测试没有权限:[{}]", permission);
            Assertions.assertFalse(this.roleService.hasPermission(id, permission));
        }
        log.info("-------------------------------");

        log.info("测试当前subject无任何一项权限:{}", this.permissions);
        for (String permission : this.permissions) {
            log.info("测试没有权限:[{}]", permission);
            Assertions.assertFalse(this.roleService.hasPermission(id, permission));
        }
        log.info("-------------------------------");

        Optional<Role> role = this.repository.findOne(Example.of(new Role("测试角色")));
        Assertions.assertTrue(role.isPresent());

        log.info("开始授权角色:[{}] 角色:[{}] 权限为:{} ", id, role.get().getName(), role.get().getPermissions().stream().map(Permission::getName).collect(Collectors.toList()));
        this.roleService.grant(id, Collections.singleton("测试角色"));
        log.info("-------------------------------");

        log.info("测试当前subject:{} 所有权限:{} 拥有的权限:{}", id, this.permissions, perms);
        for (String permission : perms) {
            log.info("测试具有权限:[{}]", permission);
            Assertions.assertTrue(this.roleService.hasPermission(id, permission));
        }
        log.info("-------------------------------");

        HashSet<String> differences = new HashSet<>(this.permissions);
        perms.forEach(differences::remove);

        log.info("当前subject[{}]无任何一项其他权限:所有权限:{} 拥有的权限:{} 未拥有的权限:{}", id, this.permissions, perms, differences);
        for (String permission : differences) {
            log.info("测试没有权限:[{}]", permission);
            Assertions.assertFalse(this.roleService.hasPermission(id, permission));
        }

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
