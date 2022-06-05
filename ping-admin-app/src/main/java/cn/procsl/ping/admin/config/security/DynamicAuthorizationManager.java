package cn.procsl.ping.admin.config.security;

import cn.procsl.ping.boot.infra.domain.rbac.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import java.util.function.Supplier;

@Indexed
@Component
@RequiredArgsConstructor
public class DynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    final AccessControlService accessControlService;

    final AuthorizationDecision accessDenied = new AuthorizationDecision(false);

    final AuthorizationDecision accessAllow = new AuthorizationDecision(true);

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        return context.getRequest().getMethod().equals("GET") ? accessAllow : accessDenied;
    }

}
