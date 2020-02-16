package cn.procsl.ping.business.user.web.controller;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author procsl
 * @date 2019/12/25
 */
@Controller
@RequestMapping("users")
@Api(tags = "用户信息接口")
public class UserController {

    @GetMapping("echo")
    public String test() {
        return "hello world";
    }

    @GetMapping("user-info")
    public Map<String, String> userInfo() {
        return ImmutableMap.of("userName", "朝闻道");
    }
}
