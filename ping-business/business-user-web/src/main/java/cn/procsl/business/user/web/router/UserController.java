package cn.procsl.business.user.web.router;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author procsl
 * @date 2019/12/25
 */
@RestController
@RequestMapping("users")
public class UserController {

    @PostMapping
    public void create() {
    }

    public String test() {
        return "test";
    }
}
