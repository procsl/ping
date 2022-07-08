package cn.procsl.ping.boot.admin.web.config.security;

import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.service.PermissionMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Indexed
@Component
@RequiredArgsConstructor
public class DynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    final AuthorizationDecision accessDenied = new AuthorizationDecision(false);

    final AuthorizationDecision accessAllow = new AuthorizationDecision(true);

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {

        val matcher = (PermissionMatcher) context.getRequest().getSession().getAttribute("dynamic-url-matcher");
        if (matcher == null) {
            return accessDenied;
        }

        Optional<Permission> option = matcher.matcher(context.getRequest());
        if (option.isEmpty()) {
            return accessDenied;
        }

        SessionUserDetails sessionUserDetails = (SessionUserDetails) authentication.get().getPrincipal();
        HttpServletRequest request = context.getRequest();
        log.debug("用户[{}] 拥有 {} 权限, 允许访问:[{} {}]", sessionUserDetails.getUsername(), option.get(), request.getMethod(), request.getRequestURI());
        return accessAllow;
    }

}
