package cn.procsl.ping.boot.system.domain.rbac;

import cn.procsl.ping.boot.system.TestSystemApplication;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Rollback
@Transactional
@DisplayName("权限实体测试")
@SpringBootTest(classes = TestSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PermissionTest {

    @Inject
    JpaRepository<Permission, Long> permissionLongJpaRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    public void save() {
        permissionLongJpaRepository.save(HttpPermission.create("get", "/**"));
        permissionLongJpaRepository.save(new PagePermission("htllo", "resources"));
    }

}