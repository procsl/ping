package cn.procsl.ping.boot.system.domain.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    void creator() {
        User user = User.creator("username", "account@procsl.cn", "123456");
        Assertions.assertNotNull(user);
        Assertions.assertEquals("username", user.getName());
        Assertions.assertEquals(Gender.unknown, user.getGender());
        Assertions.assertEquals("account@procsl.cn", user.getAccount().getName());
    }

    @Test
    void updateSelf() {
        User user = User.creator("username", "account@procsl.cn", "123456");
        Account account = user.getAccount();

        user.updateSelf("12345", Gender.man, "update self!");
        Assertions.assertEquals("12345", user.getName());
        Assertions.assertEquals(Gender.man, user.getGender());

        Assertions.assertEquals(account, user.getAccount());
        Assertions.assertEquals("account@procsl.cn", user.getAccount().getName());
    }
}