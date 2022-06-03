package cn.procsl.ping.boot.infra.domain.rbac;

import cn.procsl.ping.boot.infra.InfraApplication;
import cn.procsl.ping.exception.BusinessException;
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
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Rollback
@Transactional
@DisplayName("访问控制服务单元测试")
@SpringBootTest(classes = InfraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AccessControlServiceTest {

    @Autowired
    AccessControlService accessControlService;

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
        this.gid = repository.save(new Role(Faker.instance().name().username(), permissions)).getId();
        this.permissions = new ArrayList<>(permissions);
        this.permissions.addAll(Arrays.asList(JMockData.mock(String[].class)));
        this.config = new MockConfig().longRange(1, Long.MAX_VALUE).intRange(0, this.permissions.size());
    }


    @Test
    @DisplayName("测试授权")
    public void grant() {

        log.info("开始测试角色授予");

        Long subject = JMockData.mock(Long.class, config);
        log.info("Subject ID = {}", subject);

        List<Role> allRoles = this.repository.findAll();
        log.info("全部角色列表为:{}", allRoles);

        List<String> allRoleNames = allRoles.stream().map(Role::getName).collect(Collectors.toList());
        log.info("为[{}]授予角色:[{}]", subject, allRoleNames);
        this.accessControlService.grant(subject, allRoleNames);
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
        Assertions.assertThrows(BusinessException.class, () -> this.accessControlService.grant(subject, Collections.singleton(role)));

        log.info("测试成功");
    }

    @Test
    @DisplayName("测试权限判断")
    public void hasPermission() {
        log.info("创建多个角色");
        repository.save(new Role(JMockData.mock(String.class, config), getPermissions()));
        repository.save(new Role(JMockData.mock(String.class, config), getPermissions()));
        repository.save(new Role(JMockData.mock(String.class, config), getPermissions()));
        repository.save(new Role(JMockData.mock(String.class, config), getPermissions()));
        repository.save(new Role(JMockData.mock(String.class, config), getPermissions()));
        repository.save(new Role(JMockData.mock(String.class, config), getPermissions()));
        repository.save(new Role(JMockData.mock(String.class, config), getPermissions()));
        repository.save(new Role(JMockData.mock(String.class, config), getPermissions()));

        List<String> perms = getPermissions();
        repository.save(new Role("测试角色", perms));

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
            Assertions.assertFalse(this.accessControlService.hasPermission(id, permission));
        }
        log.info("-------------------------------");

        log.info("测试当前subject无任何一项权限:{}", this.permissions);
        for (String permission : this.permissions) {
            log.info("测试没有权限:[{}]", permission);
            Assertions.assertFalse(this.accessControlService.hasPermission(id, permission));
        }
        log.info("-------------------------------");

        Optional<Role> role = this.repository.findOne(Example.of(new Role("测试角色")));
        Assertions.assertTrue(role.isPresent());

        log.info("开始授权角色:[{}] 角色:[{}] 权限为:{} ", id, role.get().getName(), role.get().getPermissions().stream().map(Permission::getName).collect(Collectors.toList()));
        this.accessControlService.grant(id, Collections.singleton("测试角色"));
        log.info("-------------------------------");

        log.info("测试当前subject:{} 所有权限:{} 拥有的权限:{}", id, this.permissions, perms);
        for (String permission : perms) {
            log.info("测试具有权限:[{}]", permission);
            Assertions.assertTrue(this.accessControlService.hasPermission(id, permission));
        }
        log.info("-------------------------------");

        HashSet<String> differences = new HashSet<>(this.permissions);
        perms.forEach(differences::remove);

        log.info("当前subject[{}]无任何一项其他权限:所有权限:{} 拥有的权限:{} 未拥有的权限:{}", id, this.permissions, perms, differences);
        for (String permission : differences) {
            log.info("测试没有权限:[{}]", permission);
            Assertions.assertFalse(this.accessControlService.hasPermission(id, permission));
        }

    }

    @Test
    @DisplayName("测试角色判断")
    public void hasRole() {
        log.info("开始测试:创建角色");
        for (int i = 0; i < 20; i++) {
            String roleName = JMockData.mock(String.class, config);
            List<String> pers = getPermissions();
            repository.save(new Role(roleName, pers));
            log.info("创建角色:{} permissions = {}", roleName, getPermissions());
        }
        List<Role> roles = this.repository.findAll();

        MockConfig indexConfig = new MockConfig().intRange(0, roles.size());

        ArrayList<String> grantRoles = new ArrayList<>();
        Long id = JMockData.mock(Long.class, config);
        for (int i = 0; i < 3; i++) {
            Role role = roles.get(JMockData.mock(Integer.class, indexConfig));
            log.info("获取一个角色:{} permissions = {}", role.getName(), role.getPermissions());
            grantRoles.add(role.getName());
        }
        log.info("为指定的subject[{}]授予角色:[{}]", id, grantRoles);
        this.accessControlService.grant(id, grantRoles);

        log.info("断言subject[{}]存在授予角色:[{}]", id, grantRoles);
        for (String role : grantRoles) {
            Assertions.assertTrue(this.accessControlService.hasRole(id, role));
        }

        ArrayList<Role> differences = new ArrayList<>(roles);
        differences.removeIf(item -> grantRoles.contains(item.getName()));
        log.info("断言subject[{}]不具有未授予角色:[{}]", id, differences.stream().map(Role::getName).collect(Collectors.toList()));
        for (Role role : differences) {
            Assertions.assertFalse(this.accessControlService.hasRole(id, role.getName()));
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