package cn.procsl.business.user.web.controller;

import cn.procsl.business.user.web.component.SkipFilter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/02/07
 */
@Controller
@RequestMapping("filter")
public class FilterController {

    @Autowired
    ApplicationContext context;

    @GetMapping("apis")
    public TestObject api() {
        return new TestObject();
    }


    @GetMapping("skip-filter")
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

            List list = ImmutableList.of(new TestObject.Stack(), new TestObject.Stack());

            Map<String, String> map = ImmutableMap.of("key", "value");

            String id = "1231231";

            String[] array = {"array1", "array2", "array3", "array4"};

        }
    }

    @SkipFilter
    public static class TestObject2 extends TestObject {
    }

}
