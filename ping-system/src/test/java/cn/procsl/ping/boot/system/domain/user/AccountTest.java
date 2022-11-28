package cn.procsl.ping.boot.system.domain.user;

import cn.procsl.ping.boot.common.error.BusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountTest {

    @Test
    public void create() {
        Account account = Account.create("account", "123456");
        Assertions.assertEquals("account", account.getName());
        Assertions.assertEquals("123456", account.getPassword());
        Assertions.assertEquals(AccountState.enable, account.getState());
    }

    @Test
    public void stateSetting() {
        Account account = Account.create("account", "123456");
        account.stateSetting(AccountState.disable);
        Assertions.assertEquals(AccountState.disable, account.getState());

        account.stateSetting(AccountState.enable);
        Assertions.assertEquals(AccountState.enable, account.getState());
        Assertions.assertEquals("account", account.getName());
        Assertions.assertEquals("123456", account.getPassword());
    }

    @Test
    public void checkPassword() {
        Account account = Account.create("account", "123456");
        boolean bool = account.checkPassword("123456");
        Assertions.assertTrue(bool);

        bool = account.checkPassword("654321");
        Assertions.assertFalse(bool);
    }

    @Test
    public void authenticate() {
        Account account = Account.create("account", "123456");
        account.authenticate("123456");

        account.disabled();
        Assertions.assertThrows(BusinessException.class, () -> account.authenticate("123456"), "账户已被禁用");

        account.enabled();
        Assertions.assertThrows(BusinessException.class, () -> account.authenticate("654321"), "密码错误");
    }
}