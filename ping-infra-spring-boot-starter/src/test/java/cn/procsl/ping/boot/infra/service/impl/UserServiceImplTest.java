package cn.procsl.ping.boot.infra.service.impl;

import cn.procsl.ping.boot.infra.InfraApplication;
import cn.procsl.ping.boot.infra.domain.user.RegisterService;
import cn.procsl.ping.boot.infra.domain.user.User;
import cn.procsl.ping.boot.infra.service.AccountService;
import com.github.jsonzou.jmockdata.JMockData;
import com.github.jsonzou.jmockdata.MockConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;

@SpringBootTest(classes = InfraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class UserServiceImplTest {


    @Autowired
    RegisterService userService;

    @Autowired
    AccountService accountService;

    @Autowired
    JpaRepository<User, Long> jpaRepository;

    @Test
    @DisplayName("测试用户注册")
    void register() {
        MockConfig config = new MockConfig();
        config.setEnabledPrivate(true);
        String account = JMockData.mock(String.class);
        String password = JMockData.mock(String.class);
        Long userId = userService.register(account, password);
        Assertions.assertNotNull(userId);

        Long id = accountService.authenticate(account, password);
        User user = this.jpaRepository.getById(id);
        Assertions.assertNotNull(user);

    }


}