package cn.procsl.ping.business.user.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/02/19
 */
@RestController
@RequestMapping
@Tag(name = "角色")
public class RoleController {

    @GetMapping("string")
    public String getString() {
        throw new RuntimeException("string");
    }


    @GetMapping("/role")
    public Map<String, String> getUser() {
        HashMap<String, String> tmp = new HashMap<>();
        tmp.put("hello", "role");
        return tmp;
    }

    @GetMapping("role/{test}")
    private String integer(@PathVariable String test) {
        return test;
    }
}
