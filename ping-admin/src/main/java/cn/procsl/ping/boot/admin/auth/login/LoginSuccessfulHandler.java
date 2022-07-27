package cn.procsl.ping.boot.admin.auth.login;

import cn.procsl.ping.boot.admin.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.common.web.HttpMethodRequestWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Indexed
@Component
@RequiredArgsConstructor
public class LoginSuccessfulHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        SessionUserDetail user = (SessionUserDetail) authentication.getPrincipal();
        log.info("登录成功:{}", user.getUsername());
        // 转发之后返回登录信息
        request.getRequestDispatcher("/v1/session")
               .forward(new HttpMethodRequestWrapper(request, "GET"), response);
    }

    HttpPermission convert(Permission permission) {
        if (permission instanceof HttpPermission) {
            log.debug("加载权限:{}", permission);
            return (HttpPermission) permission;
        }
        return null;
    }


}
