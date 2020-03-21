package cn.procsl.ping.business.user.impl.service;

import cn.procsl.ping.business.user.dto.UserDTO;
import cn.procsl.ping.business.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author procsl
 * @date 2020/03/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceImplTest {

    @Autowired
    UserService userService;

    @Test
    public void create() {
        userService.create(UserDTO.builder().name("test").build());
    }
}
