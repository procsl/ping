package cn.procsl.business.user.web.router;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Data;
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
        private String one = "123";

        private String two = "朝闻道";

        private Test three = new Test();

        @Data
        public static class Test {
            @JacksonXmlProperty(localName = "item")
            @JacksonXmlElementWrapper(localName = "items")
            private List item = ImmutableList.of("哈哈哈哈哈", "嘿嘿<xxx嘿");
            private Map<String, String> map = ImmutableMap.of("key", "value");

            private String id = "1231231";
        }
    }
}
