package cn.procsl.ping.app.admin.api;

import cn.procsl.ping.app.admin.dto.UserRegisterDTO;
import cn.procsl.ping.boot.rest.annotation.Created;
import cn.procsl.ping.boot.rest.annotation.ExceptionHandler;
import cn.procsl.ping.boot.rest.annotation.RestEndpoint;
import cn.procsl.ping.boot.user.domain.user.service.UserService;
import com.google.common.hash.Hashing;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Controller
@RestEndpoint("user")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户模块")
@Slf4j
public class UserController {

    final UserService userService;

    @PostMapping
    @Created
    @ExceptionHandler(status = HttpStatus.BAD_REQUEST, code = "005", message = "账号已存在", exceptions = DataIntegrityViolationException.class)
    public String register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        String code = Hashing.md5().hashBytes(registerDTO.getPassword().getBytes()).toString();
        return userService.register(registerDTO.getRealName(), registerDTO.getAccount(), code, null);
    }

    @PutMapping
    @Operation(responses = @ApiResponse(content = {@Content(mediaType = "application/json")}))
    public void noContent() {
        log.info("coming");
    }

    @PutMapping("string")
    public String returnString() {
        log.info("coming");
        return "coming";
    }
}
