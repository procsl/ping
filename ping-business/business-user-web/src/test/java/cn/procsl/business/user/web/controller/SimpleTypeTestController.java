package cn.procsl.business.user.web.controller;

import cn.procsl.business.user.web.annotation.Accepted;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author procsl
 * @date 2020/01/06
 */
@Controller
@RequestMapping("simple")
public class SimpleTypeTestController {

    @PostMapping("string")
    public String postString() {
        return "string";
    }

    @PostMapping("integer")
    public int postVoid() {
        return 100;
    }

    @PostMapping("double")
    public double postDouble() {
        return 200D;
    }

    @PostMapping("long")
    public long postLong() {
        return 3000L;
    }

    @PostMapping("date")
    public Date postDate() {
        return new Date();
    }

}
