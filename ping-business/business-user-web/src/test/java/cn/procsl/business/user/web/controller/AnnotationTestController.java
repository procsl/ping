package cn.procsl.business.user.web.controller;

import cn.procsl.business.user.web.annotation.Accepted;
import cn.procsl.business.user.web.annotation.Created;
import cn.procsl.business.user.web.annotation.NoContent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author procsl
 * @date 2020/01/08
 */
@Controller
public class AnnotationTestController {

    @PostMapping("accepted")
    @Accepted
    public String postAccepted() {
        return "accepted";
    }

    @GetMapping("accepted")
    public String getAccepted() {
        return "accepted";
    }

    @PostMapping("noContent")
    @NoContent
    public String noContent() {
        return "noContent";
    }

    @PostMapping("noContent2")
    public void noContent2() {
    }

    @PostMapping("create")
    @Created(location = "https://api.procls.cn/product/{id}")
    public String create() {
        return "1231231232390803";
    }

    @PostMapping("create2")
    @Created()
    public String create2() {
        return "1231231232390803";
    }

}
