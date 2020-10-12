package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.user.domain.rbac.model.Permission;
import cn.procsl.ping.boot.user.domain.rbac.model.Subject;
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
import java.util.Collections;
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

    private List<Target> list;

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
        this.permissionService
            .getByTargets(list)
            .stream()
            .map(Permission::getId)
            .collect(Collectors.toList());
        roleService.create("超级管理员", null);
        roleService.create("管理员", list);
        roleService.create("管理员1", list);
        roleService.create("管理", list);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void bind() {
        Long subjectId = this.subjectService.bind("xxx", Collections.singleton("超级管理员"));
        entityManager.flush();
        entityManager.clear();
        Subject sub = this.subjectService.findById(subjectId);
        Assert.assertNotNull(sub);
        Assert.assertNotNull(sub.getRole());
        Assert.assertEquals(1, sub.getRole().size());
    }

    @Test
    public void bind1() {
        Long subjectId = this.subjectService.bind("xxx", Arrays.asList("超级管理员"));
        entityManager.flush();
        entityManager.clear();
        Subject sub = this.subjectService.findById(subjectId);
        Assert.assertNotNull(sub);
        Assert.assertNotNull(sub.getRole());
        Assert.assertEquals(1, sub.getRole().size());
    }

}
