package cn.procsl.business.user.web.router;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

        @JacksonXmlProperty(isAttribute = true)
        String one = "123";

        String two = "朝闻道";

        Test three = new Test();

        Stack stack = new Stack();

        Stack[] stacks = {stack, stack, stack, stack};

        public static class Stack {
            @Getter
            String name = "name";
            @Getter
            String echo = "echo hello";
            
            @Getter
            String fuck = "fuck";
        }

        @Data
        public static class Test {

            List list = ImmutableList.of(new Stack(), new Stack());

            Map<String, String> map = ImmutableMap.of("key", "value");

            String id = "1231231";

            String[] array = {"array1", "array2", "array3", "array4"};

        }
    }
}
