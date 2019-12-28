package cn.procsl.business.impl.user;

import cn.procsl.business.user.dto.UserDTO;
import cn.procsl.business.user.service.UserService;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static cn.procsl.business.user.dto.UserDTO.Account.Type.email;
import static cn.procsl.business.user.dto.UserDTO.Setting.Theme.black;
import static cn.procsl.business.user.dto.UserDTO.Status.enable;

/**
 * @author procsl
 * @date 2019/12/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/user-business.xml"})
@Rollback
public class UserServiceImplTest {

    @Autowired
    UserService userService;

    @Test
    public void create() {
        UserDTO user = UserDTO.builder()
                .account(UserDTO.Account.builder()
                        .credential("zhaowendao@procsl.cn")
                        .password("chens11111anlu")
                        .type(email).build())
                .gender(UserDTO.Gender.未知)
                .status(enable)
                .setting(UserDTO.Setting.builder()
                        .theme(black)
                        .build())
                .name("朝闻道")
                .build();
        this.userService.create(ImmutableList.of(user));

    }

    @Test
    public void update() {
    }

    @Test
    public void query() {
    }

    @Test
    public void delete() {
    }
}