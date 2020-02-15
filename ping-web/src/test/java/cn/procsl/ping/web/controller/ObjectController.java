package cn.procsl.ping.web.controller;

import cn.procsl.ping.web.serializable.SkipFilter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author procsl
 * @date 2020/02/07
 */
@Controller
@RequestMapping("object")
public class ObjectController {


    @GetMapping("full")
    public TestObject object() {
        return new TestObject();
    }


    @GetMapping("skip")
    public Object skipFilter() {
        return new TestObject2();
    }

    @Data
    public static class TestObject {

        @JacksonXmlProperty(isAttribute = true)
        String one = "123";

        String two = "朝闻道";

        TestObject.Test three = new TestObject.Test();

        TestObject.Stack stack = new TestObject.Stack();

        TestObject.Stack[] stacks = {stack, stack, stack, stack};

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
            String id = "1231231";

            String[] array = {"array1", "array2", "array3", "array4"};
        }
    }

    @SkipFilter
    public static class TestObject2 extends TestObject {
    }

}
