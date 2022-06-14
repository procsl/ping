package cn.procsl.ping.admin.config.security;

import cn.procsl.ping.boot.infra.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.infra.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.infra.domain.rbac.Permission;
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
import java.util.Collection;

@Slf4j
@Indexed
@Component
@RequiredArgsConstructor
public class LoginSuccessfulHandler implements AuthenticationSuccessHandler {

    final AccessControlService accessControlService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SessionUserDetails user = (SessionUserDetails) authentication.getPrincipal();
        log.info("登录成功:{}", user.getUsername());

        Collection<HttpPermission> https = accessControlService.loadPermissions(user.getId(), this::convert);

        HttpPermissionMatcher matcher = new HttpPermissionMatcher(https);
        request.getSession().setAttribute("dynamic-url-matcher", matcher);
        response.sendRedirect("/index.html");
    }

    HttpPermission convert(Permission permission) {
        if (permission instanceof HttpPermission) {
            log.debug("加载权限:{}", permission);
            return (HttpPermission) permission;
        }
        return null;
    }

}
