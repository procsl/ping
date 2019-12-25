package cn.procsl.business.user.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author procsl
 * @date 2019/12/25
 */
@RequestMapping
@Controller
public class IndexController {

    @GetMapping
    @ResponseBody
    public String index() {
        return "hello world";
    }
}
