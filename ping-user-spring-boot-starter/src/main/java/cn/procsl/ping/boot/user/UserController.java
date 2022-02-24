package cn.procsl.ping.boot.user;

import cn.procsl.ping.business.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "用户中心接口", description = "用户信息相关接口")
public class UserController {

    final UserService userService;

    @Transactional
    @PostMapping("users")
    @ResponseStatus(code = HttpStatus.CREATED, reason = "创建成功")
    @Operation(operationId = "userRegistered", description = "用户注册")
    public HttpEntity<Long> register(@RequestBody @Validated UserRegisterDTO register) throws URISyntaxException, BusinessException {
        Long id = this.userService.registered(register.getUserName(), register.getGender(), register.getAccountName(), register.getPassword(), Collections.emptyList());
        URI url = new URI("https://api.procsl.cn/user/" + id);
        return ResponseEntity.created(url).body(id);
    }

}
