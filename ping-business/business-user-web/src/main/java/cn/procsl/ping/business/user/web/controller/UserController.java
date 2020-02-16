package cn.procsl.ping.business.user.web.controller;

import com.google.common.collect.ImmutableList;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

/**
 * @author procsl
 * @date 2019/12/25
 */
@Controller
@RequestMapping("users")
@Api(tags = "用户信息接口")
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
    public void delete(User user) {
        userInfo.remove(user);
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

    @Getter
    @Setter
    @XmlRootElement(name = "root")
    protected static class User {
        Integer id;
        String name;
        String gender;
        List<String> list = ImmutableList.of("1", "2", "3", "4");

        @Override
        public boolean equals(Object obj) {
            return this.id.equals(((User) obj).id);
        }

        @Override
        public int hashCode() {
            return id == null ? 0 : id;
        }
    }
}
