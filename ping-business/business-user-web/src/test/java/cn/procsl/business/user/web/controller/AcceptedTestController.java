package cn.procsl.business.user.web.controller;

import cn.procsl.business.user.web.annotation.Accepted;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author procsl
 * @date 2020/01/06
 */
@Controller
@RequestMapping("accepted")
public class AcceptedTestController {

    @PostMapping("string")
    @Accepted
    public String postString() {
        return "string";
    }

    @PostMapping("integer")
    @Accepted
    public int postVoid() {
        return 100;
    }

    @PostMapping("double")
    @Accepted
    public double postDouble() {
        return 200D;
    }

    @PostMapping("long")
    @Accepted
    public long postLong() {
        return 3000L;
    }
}
