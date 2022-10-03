package cn.procsl.ping.boot.admin.auth.access;

import cn.procsl.ping.boot.admin.auth.login.SessionUserDetail;
import cn.procsl.ping.boot.admin.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.admin.domain.rbac.HttpServletPermissionMatcherService;
import cn.procsl.ping.boot.admin.domain.rbac.PermissionCacheService;
import cn.procsl.ping.boot.admin.domain.rbac.PermissionMatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Collection;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class HttpAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    final AuthorizationDecision accessDenied = new AuthorizationDecision(false);

    final AuthorizationDecision accessAllow = new AuthorizationDecision(true);

    final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    final PermissionMatcherService<HttpPermission> permissionMatcherService = new HttpServletPermissionMatcherService();

    final PermissionCacheService<HttpPermission, Long> grantedAuthorityLoader;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {

        Authentication auth = authentication.get();
        if (auth == null) {
            log.warn("Authentication is null!");
            return accessDenied;
        }

        if (authenticationTrustResolver.isAnonymous(auth)) {
            return accessDenied;
        }

        if (auth.isAuthenticated() && auth.getPrincipal() instanceof SessionUserDetail) {
            SessionUserDetail details = (SessionUserDetail) auth.getPrincipal();
            Long subject = details.getId();
            Collection<HttpPermission> permissions = this.grantedAuthorityLoader.getPermissions(subject);
            boolean bool = permissionMatcherService.matcher(context.getRequest(), permissions).isEmpty();
            return bool ? this.accessDenied : this.accessAllow;
        }

        return accessDenied;
    }

}
