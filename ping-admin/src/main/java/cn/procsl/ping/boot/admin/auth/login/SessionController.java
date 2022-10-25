package cn.procsl.ping.boot.admin.auth.login;

import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.common.dto.MessageVO;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.event.Publisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.USER_LOGIN;
import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.USER_LOGOUT;


@Slf4j
@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "session", description = "用户会话信息管理")
@RestControllerAdvice
public class SessionController {

    @ResponseBody
    @GetMapping("/v1/session")
    @Operation(summary = "获取用户当前登录信息")
    public SessionUserDetail currentSession() {
        throw new BusinessException(HttpStatus.UNAUTHORIZED, "401002", "尚未登录,请登录");
    }

    @PermitAll
    @ResponseBody
    @PostMapping(value = "/v1/session")
    @VerifyCaptcha(type = CaptchaType.image)
    @Operation(summary = "用户登录", operationId = "authenticate")
    @Publisher(name = USER_LOGIN, parameter = "#details.account")
    public SessionUserDetail createSession(HttpServletRequest request, HttpServletResponse response,
                                           @Validated @RequestBody LoginDetailDTO details)
            throws ServletException, IOException {

        return null;
    }

    @ResponseBody
    @DeleteMapping("/v1/session")
    @Operation(summary = "用户注销", operationId = "logout")
    @Publisher(name = USER_LOGOUT, parameter = "#root[currentAccount].get()?.id")
    public MessageVO deleteSession(HttpServletRequest request, HttpServletResponse response) {

        return null;
    }

}
