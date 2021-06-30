package cn.procsl.ping.boot.user.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@SpringBootApplication
public class UserLoginServiceTest {

    @Inject
    UserLoginService userLoginService;

    @Test
    public void isNotNull() {
        Assert.assertNotNull(userLoginService);
    }

    @Test
    public void login() {
    }


}
