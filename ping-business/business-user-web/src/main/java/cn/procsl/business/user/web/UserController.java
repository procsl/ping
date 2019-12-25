package cn.procsl.business.user.web;

import cn.procsl.business.user.dto.UserDTO;
import cn.procsl.business.user.service.UserService;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author procsl
 * @date 2019/12/25
 */
@Controller
@RequestMapping("users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public void create(UserDTO user) {
        this.userService.create(ImmutableList.of(user));
    }
}
