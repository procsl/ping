package cn.procsl.ping.business.user.web.controller;

import cn.procsl.ping.boot.rest.annotation.NoContent;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author procsl
 * @date 2020/02/19
 */
@RestController
@RequestMapping("users")
@Tag(name = "用户模块")
public class UserController {

    private List<User> users = new LinkedList<>();

    @PostMapping
    @NoContent
    public void create(@RequestBody User user, @RequestParam Integer index) {
        users.add(user);
    }

    @DeleteMapping(value = "{index}", produces = {APPLICATION_JSON_VALUE})
    public User deleteUser(@PathVariable Integer index) {
        return users.remove(0);
    }

    @GetMapping
    public List<User> getUsers() {
        return users;
    }

    @PatchMapping
    public void patch(@RequestBody User user) {
        users.add(user);
    }

    @Data
    protected static class User {
        @NotNull @Length(max = 32) String name;
        @NotNull @Size(max = 32, min = 32) String password;
    }
}
