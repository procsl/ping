package cn.procsl.ping.boot.admin.auth.access;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Collection;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class HttpAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    final AuthorizationDecision accessDenied = new AuthorizationDecision(false);

    final AuthorizationDecision accessAllow = new AuthorizationDecision(true);

    final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

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

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            boolean bool = authority instanceof HttpGrantedAuthority;
            if (!bool) {
                continue;
            }

            if (((HttpGrantedAuthority) authority).matcher(context.getRequest())) {
                return accessAllow;
            }
        }

        return accessDenied;
    }

}
