package cn.procsl.ping.boot.infra.domain.account;

import cn.procsl.ping.boot.infra.domain.user.Account;
import cn.procsl.ping.exception.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AccountTest {

    @Test
    @DisplayName("测试认证方法")
    public void authenticate() {
        Account account = Account.create("name", "121232123");
        account.authenticate("121232123");

        Assertions.assertThrows(BusinessException.class, () -> account.authenticate("xxxxxxx"));

        account.disabled();
        Assertions.assertThrows(BusinessException.class, () -> account.authenticate("121232123"));
    }
}