package cn.procsl.ping.boot.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping
@ApiResponse
public class UserController {

    final UserService userService;

    @Transactional
    @PostMapping("users")
    @ResponseStatus(code = HttpStatus.CREATED, reason = "创建成功")
    @Operation(operationId = "userRegistered", description = "用户注册")
    public HttpEntity<Long> register(@RequestBody @Validated UserRegisterDTO register) throws URISyntaxException {
        Long id = this.userService.registered(register.getUserName(), register.getGender(), register.getAccountName(), register.getPassword(), Collections.singleton("普通角色"));
        URI url = new URI("https://api.procsl.cn/user/" + id);
        return ResponseEntity.created(url).body(id);
    }

}
