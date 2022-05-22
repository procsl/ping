package cn.procsl.ping.boot.infra.web.user;

import cn.procsl.ping.boot.infra.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Indexed
@RestController
@RequiredArgsConstructor
public class UserController {

    final UserService userService;

    @PostMapping("users")
    public Long register(@Validated @RequestBody UserDTO userDTO) {
        return this.userService.register(userDTO.getAccount(), userDTO.getPassword());
    }


}
