package cn.procsl.business.user.web.controller;

import cn.procsl.business.user.web.error.RestError;
import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/01/09
 */
@Controller
@RequestMapping("format")
public class GeneralController implements Serializable {

    @GetMapping("map")
    public Map<String, String> map() {
        Map<String, String> map = new HashMap<>(2);
        map.put("key1", "value");
        map.put("key2", "value2");
        return map;
    }

    @GetMapping("list")
    public List<String> list() {
        return ImmutableList.of("hello", "world", "list");
    }

    @GetMapping("object")
    public Object object() {
        return new Serializable() {
            public String echo;
            public RestError error;

            {
                error = new RestError().setCode("123").setMessage("哈哈哈哈哈哈哈哈");
                echo = "你好呀";
            }
        };
    }

}
