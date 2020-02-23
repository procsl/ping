package cn.procsl.ping.business.user.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.LinkedList;
import java.util.List;

/**
 * @author procsl
 * @date 2019/12/25
 */
@Controller
@RequestMapping("users")
@Tag(name = "用户模块", description = "这是用户模块描述")
public class UserController {

    LinkedList<User> userInfo = new LinkedList<>();

    @GetMapping
    public List<User> userInfo() {
        return userInfo;
    }

    @PostMapping
    public void push(@RequestBody User user) {
        userInfo.add(user);
    }

    @DeleteMapping
    public void delete(@NotEmpty Integer[] id) {
        User user = new User();
        for (Integer s : id) {
            user.id = s;
            userInfo.remove(user);
        }
    }

    @GetMapping("{id}")
    public User findUser(@PathVariable("id") Integer id) {
        for (User user : this.userInfo) {
            if (user != null && id.equals(user.id)) {
                return user;
            }
        }
        return null;
    }

    @GetMapping("enum")
    public PatternEnum testEnum(PatternEnum patternEnum) {
        return patternEnum;
    }

    @Getter
    @Setter
    protected static class User {
        Integer id;
        String name;
        String gender;
        PatternEnum pattern;
        List<String> list;

        @Override
        public boolean equals(Object obj) {
            return this.id.equals(((User) obj).id);
        }

        @Override
        public int hashCode() {
            return id == null ? 0 : id;
        }
    }

    protected static enum PatternEnum {
        A, B, C, D
    }
}
