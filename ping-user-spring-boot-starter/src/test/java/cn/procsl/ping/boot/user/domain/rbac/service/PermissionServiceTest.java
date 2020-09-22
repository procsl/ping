package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.user.domain.rbac.model.Permission;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class PermissionServiceTest {

    @Inject
    PermissionService permissionService;

    @Test
    public void searchOne() {
        Permission root = permissionService.searchOne("/root");
        log.info("{}", root);
    }

    @Test
    public void create() {
        Long perm = permissionService.create(null, "root", "root", "all", "读");
        log.info("perm:{}", perm);
    }

}
