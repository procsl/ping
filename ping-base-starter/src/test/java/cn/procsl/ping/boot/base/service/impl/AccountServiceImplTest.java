package cn.procsl.ping.boot.base.service.impl;

import cn.procsl.ping.boot.base.TestBaseApplication;
import cn.procsl.ping.boot.base.domain.user.Account;
import cn.procsl.ping.boot.base.domain.user.AccountState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@DisplayName("用户账户服务单元测试")
@Transactional
@SpringBootTest(classes = TestBaseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AccountServiceImplTest {

    @Autowired
    JpaRepository<Account, Long> jpaRepository;

    Long gid;

    @BeforeEach
    @Transactional
    public void setUp() {
        String name = "account@foxmail.com";
        String password = "1234567";
        this.gid = this.jpaRepository.save(Account.create(name, password)).getId();
    }

    @Test
    @DisplayName("测试创建用户账户")
    @Transactional
    @Rollback
    public void create() {
        String name = "program_chen@foxmail.com";
        String password = "1234567";
        Long id = this.jpaRepository.save(Account.create(name, password)).getId();
        assert id != null;
        Account account = this.jpaRepository.getReferenceById(id);
        Assertions.assertEquals(account.getName(), name);
        Assertions.assertEquals(account.getPassword(), password);
        Assertions.assertTrue(account.isEnable());
        log.info("测试成功");
    }

    @Test
    @Rollback
    @Transactional
    @DisplayName("测试禁用账户")
    public void disable() {
        {
            Account account = this.jpaRepository.getReferenceById(gid);
            Assertions.assertEquals(account.getState(), AccountState.enable);
        }
        {
            Account account = this.jpaRepository.getReferenceById(gid);
            account.disabled();
            Assertions.assertEquals(account.getState(), AccountState.disable);
        }
    }

    @Test
    @Rollback
    @Transactional
    @DisplayName("测试启用账户")
    public void enable() {
        {
            Account account = this.jpaRepository.getReferenceById(gid);
            Assertions.assertEquals(account.getState(), AccountState.enable);
        }
        {
            Account account = this.jpaRepository.getReferenceById(gid);
            account.disabled();
            Assertions.assertEquals(account.getState(), AccountState.disable);
        }
        {
            Account account = this.jpaRepository.getReferenceById(gid);
            account.enabled();
            Assertions.assertEquals(account.getState(), AccountState.enable);
        }
    }

}