package cn.procsl.ping.boot.user.domain.user.service;

import cn.procsl.ping.boot.user.domain.user.model.User;
import cn.procsl.ping.business.exception.BusinessException;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class UserServiceTest {

    @Inject
    UserService userService;

    @Inject
    EntityManager entityManager;

    String id;

    @Test
    public void register() {
        String md5 = Hashing.md5().hashBytes("123456".getBytes()).toString();
        String userId = userService.register("朝闻道", "procsl", md5, null);
        log.info("userId:{}", userId);
        Assert.assertNotNull(userId);
        entityManager.flush();
        entityManager.clear();
        User user = this.userService.getOne(userId);
        Assert.assertNotNull(user.getAccount());
        Assert.assertNotNull(user.getProfile());
        this.id = userId;
    }

    @Test
    public void login() {
        register();
        entityManager.clear();
        String md5 = Hashing.md5().hashBytes("123456".getBytes()).toString();
        userService.login("procsl", md5);
    }

    @Test(expected = BusinessException.class)
    public void login2() {
        register();
        userService.disable(id);
        entityManager.clear();
        String md5 = Hashing.md5().hashBytes("123456".getBytes()).toString();
        userService.login("procsl", md5);
    }

    @Test(expected = BusinessException.class)
    public void login3() {
        register();
        entityManager.clear();
        String md5 = Hashing.md5().hashBytes("12345678".getBytes()).toString();
        userService.login("procsl", md5);
    }

    @Test(expected = BusinessException.class)
    public void resetPassword() {
        register();
        String md5 = Hashing.md5().hashBytes("12345678".getBytes()).toString();
        userService.resetPassword(id, md5);
        entityManager.flush();
        entityManager.clear();
        md5 = Hashing.md5().hashBytes("123456".getBytes()).toString();
        userService.login("procsl", md5);
    }

    @Test
    public void changePassword() {
        register();
        String md51 = Hashing.md5().hashBytes("123456".getBytes()).toString();
        String md52 = Hashing.md5().hashBytes("123457".getBytes()).toString();
        userService.changePassword(id, md51, md52);

        userService.login("procsl", md52);
    }

    @Test(expected = BusinessException.class)
    public void changePassword1() {
        register();
        String md51 = Hashing.md5().hashBytes("123458".getBytes()).toString();
        String md52 = Hashing.md5().hashBytes("123457".getBytes()).toString();
        userService.changePassword(id, md51, md52);
    }
}
