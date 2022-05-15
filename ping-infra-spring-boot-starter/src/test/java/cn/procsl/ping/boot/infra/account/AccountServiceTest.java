package cn.procsl.ping.boot.infra.account;

import cn.procsl.ping.boot.infra.InfraApplication;
import cn.procsl.ping.boot.infra.conf.ConfigService;
import cn.procsl.ping.exception.BusinessException;
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

import javax.validation.ConstraintViolationException;

@Slf4j
@DisplayName("用户账户服务单元测试")
@Transactional
@SpringBootTest(classes = InfraApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AccountServiceTest {

    @Autowired
    ConfigService configService;

    @Autowired
    AccountService accountService;

    @Autowired
    JpaRepository<Account, Long> jpaRepository;

    Long gid;

    @Test
    void name() {
        this.configService.getConfig("test");
    }

    @BeforeEach
    @Transactional
    public void setUp() {
        String name = "account@foxmail.com";
        String password = "1234567";
        this.gid = this.accountService.create(name, password);
    }

    @Test
    @DisplayName("测试创建用户账户")
    @Transactional
    @Rollback
    public void create() {
        String name = "program_chen@foxmail.com";
        String password = "1234567";
        Long id = this.accountService.create(name, password);
        Account account = this.jpaRepository.getById(id);
        Assertions.assertEquals(account.getName(), name);
        Assertions.assertEquals(account.getPassword(), password);
        Assertions.assertTrue(account.isEnable());
        log.info("测试成功");
    }

    @Test
    @DisplayName("测试创建用户账户:已存在账户")
    @Transactional
    @Rollback
    public void createAccountExists() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            Account entity = this.jpaRepository.getById(gid);
            try {
                this.accountService.create(entity.getName(), "any");
            } catch (Exception e) {
                log.error("打印异常:", e);
                throw e;
            }
        });
    }

    @Test
    @DisplayName("测试登录用户账户:正常登录")
    @Transactional
    @Rollback
    public void check() {
        Assertions.assertNotNull(gid);
        Account account = this.jpaRepository.getById(gid);
        Long id = this.accountService.check(account.getName(), account.getPassword());
        Assertions.assertEquals(id, account.getId());
    }

    @Test
    @DisplayName("测试登录用户账户:密码错误")
    @Transactional
    @Rollback
    public void checkAccountPasswordError() {
        Assertions.assertNotNull(gid);
        Account account = this.jpaRepository.getById(gid);

        Assertions.assertThrows(BusinessException.class, () -> this.accountService.check(account.getName(), "987654321"), "密码错误");

    }

    @Test
    @DisplayName("测试登录用户账户:账户不存在")
    @Transactional
    @Rollback
    public void checkAccountNotFound() {
        Assertions.assertNotNull(gid);
        Account account = this.jpaRepository.getById(gid);

        Assertions.assertThrows(BusinessException.class, () -> this.accountService.check("NotFountAccount", account.getPassword()), "账户不存在");

    }

    @Test
    @DisplayName("测试登录用户账户:账户已被禁用")
    @Transactional
    @Rollback
    public void checkAccountDisable() {
        Assertions.assertNotNull(gid);
        Account account = this.jpaRepository.getById(gid);
        account.disabled();
        this.jpaRepository.save(account);

        Assertions.assertThrows(BusinessException.class, () -> this.accountService.check(account.getName(), account.getPassword()), "账户已被禁用");

    }

    @Test
    @Rollback
    @Transactional
    @DisplayName("测试禁用账户")
    public void disable() {
        {
            Account account = this.jpaRepository.getById(gid);
            Assertions.assertEquals(account.getState(), AccountStatus.enable);
        }
        {
            this.accountService.disable(gid);
            Account account = this.jpaRepository.getById(gid);
            Assertions.assertEquals(account.getState(), AccountStatus.disable);
        }
    }

    @Test
    @Rollback
    @Transactional
    @DisplayName("测试启用账户")
    public void enable() {
        {
            Account account = this.jpaRepository.getById(gid);
            Assertions.assertEquals(account.getState(), AccountStatus.enable);
        }
        {
            this.accountService.disable(gid);
            Account account = this.jpaRepository.getById(gid);
            Assertions.assertEquals(account.getState(), AccountStatus.disable);
        }
        {
            this.accountService.enable(gid);
            Account account = this.jpaRepository.getById(gid);
            Assertions.assertEquals(account.getState(), AccountStatus.enable);
        }
    }
}
