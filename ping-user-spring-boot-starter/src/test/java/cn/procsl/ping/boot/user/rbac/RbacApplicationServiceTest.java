package cn.procsl.ping.boot.user.rbac;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@SpringBootApplication
public class RbacApplicationServiceTest {

    final AccessControlService rbacApplicationService;

    final JpaRepository<RoleEntity, Long> roleJpaRepository;


    @Test
    public void test() {
        Assertions.assertNotNull(rbacApplicationService, "注入失败");
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.roleJpaRepository.flush();
    }

    @Test
    @Transactional
    @Rollback
    public void createRole() {
    }

    @Test
    @Transactional
    @Rollback
    public void deleteRole() {
    }

    @Test
    @Transactional
    @Rollback
    public void changeRolePermissions() {
    }

    @Test
    @Transactional
    @Rollback
    public void changeRoleName() {

    }

}
