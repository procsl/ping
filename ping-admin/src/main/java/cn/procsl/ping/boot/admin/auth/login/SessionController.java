package cn.procsl.ping.boot.admin.auth.login;

import cn.procsl.ping.boot.admin.auth.AuthenticationProcessing;
import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.common.dto.MessageVO;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.error.ErrorVO;
import cn.procsl.ping.boot.common.event.Publisher;
import cn.procsl.ping.boot.common.event.PublisherRootAttributeConfigurer;
import cn.procsl.ping.boot.common.utils.ObjectUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.USER_LOGIN;
import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.USER_LOGOUT;


@Slf4j
@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "session", description = "用户会话信息管理")
@RestControllerAdvice
public class SessionController {

    final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    final AuthenticationProcessing authenticationProcessing;

    @ResponseBody
    @GetMapping("/v1/session")
    @Operation(summary = "获取用户当前登录信息")
    public SessionUserDetail currentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof SessionUserDetail) {
            return (SessionUserDetail) authentication.getPrincipal();
        }
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
        Authentication authenticate = SecurityContextHolder.getContext().getAuthentication();

        if (this.authenticationTrustResolver.isAnonymous(authenticate) || !authenticate.isAuthenticated()) {
            UsernamePasswordAuthenticationToken info = new UsernamePasswordAuthenticationToken(details.getAccount(),
                    details.getPassword());
            authenticate = this.authenticationProcessing.login(request, response, info);

            if (authenticate == null) {
                return null;
            }
        }

        if (!(authenticate.getPrincipal() instanceof SessionUserDetail)) {
            throw new BusinessException("系统内部错误");
        }
        SessionUserDetail tmp = (SessionUserDetail) authenticate.getPrincipal();
        if (ObjectUtils.nullSafeEquals(tmp.getUsername(), details.getAccount())) {
            return tmp;
        }
        throw new BusinessException(HttpStatus.CONFLICT, "401003", "当前系统已登录其他用户, 请注销当前用户后登录");
    }

    @ResponseBody
    @DeleteMapping("/v1/session")
    @Operation(summary = "用户注销", operationId = "logout")
    @Publisher(name = USER_LOGOUT, parameter = "#root[currentAccount].get()?.id")
    public MessageVO deleteSession(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.authenticationProcessing.logout(request, response, authentication);
        return new MessageVO(String.format("用户[%s]已退出登录", authentication.getName()));
    }


    @ResponseBody
    @ExceptionHandler(value = DisabledException.class)
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public ErrorVO disabledException() {
        return ErrorVO.builder("401002", "用户已被禁用");
    }

    @ResponseBody
    @ExceptionHandler(value = AuthenticationException.class)
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public ErrorVO authenticationExceptionHandler() {
        return ErrorVO.builder("401001", "账户名或密码错误");
    }

    @Indexed
    @Component
    public static class SessionContext implements PublisherRootAttributeConfigurer {
        @Override
        public Map<String, Object> getAttributes() {
            Supplier<SessionUserDetail> getCurrentAccount = () -> {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null) {
                    return null;
                }
                if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof SessionUserDetail) {
                    return ((SessionUserDetail) authentication.getPrincipal());
                }
                return null;
            };
            return Map.of("currentAccount", getCurrentAccount);
        }
    }


}
