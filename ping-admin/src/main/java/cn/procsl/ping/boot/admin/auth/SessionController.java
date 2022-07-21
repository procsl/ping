package cn.procsl.ping.boot.admin.auth;

import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.error.ErrorCode;
import cn.procsl.ping.boot.common.utils.ObjectUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "session", description = "用户会话信息管理")
public class SessionController {

    final AuthenticationManager authenticationManager;

    final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    final AuthenticationProcessing authenticationProcessing;

    @ResponseBody
    @GetMapping("/v1/session")
    @Operation(summary = "获取用户当前登录信息")
    public SessionUserDetails currentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof SessionUserDetails) {
            return (SessionUserDetails) authentication.getPrincipal();
        }
        throw new BusinessException("用户尚未登录,请登录");
    }

    @ResponseBody
    @PostMapping("/v1/session")
    @Operation(summary = "用户登录")
    public SessionUserDetails createSession(HttpServletRequest request, HttpServletResponse response,
                                            @Validated @RequestBody LoginDetailsDTO details)
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

        if (!(authenticate.getPrincipal() instanceof SessionUserDetails)) {
            throw new BusinessException("系统内部错误");
        }
        SessionUserDetails tmp = (SessionUserDetails) authenticate.getPrincipal();
        if (ObjectUtils.nullSafeEquals(tmp.getUsername(), details.getAccount())) {
            return tmp;
        }
        throw new BusinessException("系统已登录其他用户, 请注销后登录");
    }

    @ResponseBody
    @ExceptionHandler(value = BadCredentialsException.class)
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public ErrorCode BadCredentialsExceptionHandler(BadCredentialsException badCredentialsException) {
        return ErrorCode.builder("E002", "账户或密码错误");
    }

}
