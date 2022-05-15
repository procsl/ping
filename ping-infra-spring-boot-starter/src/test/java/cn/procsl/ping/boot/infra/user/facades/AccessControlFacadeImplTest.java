package cn.procsl.ping.boot.infra.user.facades;

import cn.procsl.ping.boot.infra.InfraApplication;
import cn.procsl.ping.boot.infra.rbac.RoleService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = InfraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AccessControlFacadeImplTest {

    @Autowired
    RoleService roleService;

    @BeforeEach
    void setUp() {
//        roleService.createRole()
    }

    @Test
    void getDefaultRoles() {
        Assertions.assertNotNull(roleService);
    }

    @Test
    void defaultRoleSetting() {
    }
}