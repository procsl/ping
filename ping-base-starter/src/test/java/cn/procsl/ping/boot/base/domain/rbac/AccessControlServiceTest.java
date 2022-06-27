package cn.procsl.ping.boot.base.domain.rbac;

import cn.procsl.ping.boot.base.TestBaseApplication;
import cn.procsl.ping.common.exception.BusinessException;
import com.github.javafaker.Faker;
import com.github.jsonzou.jmockdata.JMockData;
import com.github.jsonzou.jmockdata.MockConfig;
import lombok.extern.slf4j.Slf4j;
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
@SpringBootTest(classes = TestBaseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AccessControlServiceTest {

    @Autowired
    AccessControlService accessControlService;

    @Autowired
    JpaRepository<Role, Long> repository;

    @Autowired
    JpaRepository<Subject, Long> subjectLongJpaRepository;

    @Autowired
    JpaRepository<Permission, Long> permissionLongJpaRepository;

    Faker faker = new Faker(Locale.CHINA);
    Long gid;
    List<Permission> permissions;
    MockConfig config;

    @BeforeEach
    @Transactional
    @DisplayName("初始化")
    void setUp() {
        List<Permission> permissions = Arrays.asList(
                HttpPermission.create("post", "/1"),
                HttpPermission.create("get", "/1"),
                HttpPermission.create("patch", "/3"),
                HttpPermission.create("put", "/4"),
                HttpPermission.create("delete", "/5"),
                new PagePermission("读取", "销售页面"),
                new PagePermission("写入", "数据库"),
                new PagePermission("修改", "个人资料"),
                new PagePermission("删除", "管理页面")
        );
        this.permissions = new ArrayList<>(permissions);
//        {
//            PagePermission mack = JMockData.mock(PagePermission.class);
//            log.info("mack:{}", mack);
//            log.info("class:{}", mack.getClass().getName());
//            this.permissions.add(mack);
//        }

        this.permissionLongJpaRepository.saveAll(this.permissions);
        this.gid = repository.save(new Role(Faker.instance().name().username(), permissions)).getId();
        this.config = new MockConfig().longRange(1, Long.MAX_VALUE).intRange(0, this.permissions.size());

    }


    @Test
    @DisplayName("测试授权")
    public void grant() {

        log.info("开始测试角色授予");

        Long subject = JMockData.mock(Long.class, config);
        log.info("Subject ID = {}", subject);

        log.info("all permissions:{}", this.permissions);
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
    @DisplayName("测试角色判断")
    public void hasRole() {
        log.info("开始测试:创建角色");
        for (int i = 0; i < 10; i++) {
            String roleName = JMockData.mock(String.class, config);
            List<Permission> pers = getPermissions();
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

    List<Permission> getPermissions() {

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