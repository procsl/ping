package cn.procsl.business.user.web.router;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author procsl
 * @date 2019/12/25
 */
@RequestMapping
@RestController
public class IndexController {

    @Autowired
    ApplicationContext context;

    @GetMapping("apis")
    public TestObject api() {
        return new TestObject();
    }

    @Data
    public static class TestObject {
        private String field;
        private String name;
    }
}
