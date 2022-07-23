package cn.procsl.ping.boot.admin.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
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
public class AuthenticationProcessing implements ApplicationEventPublisherAware {


    @Value("${server.error.path:/error}")
    String error;
    final AuthenticationManager authenticationManager;
    AuthenticationSuccessHandler successHandler;
    AuthenticationFailureHandler failureHandler;
    @Setter
    SessionAuthenticationStrategy sessionStrategy = new NullAuthenticatedSessionStrategy();
    @Setter
    SecurityContextRepository securityContextRepository = new NullSecurityContextRepository();
    @Setter
    RememberMeServices rememberMeServices = new NullRememberMeServices();
    ApplicationEventPublisher eventPublisher;

    public Authentication login(HttpServletRequest request, HttpServletResponse response, Authentication token)
            throws ServletException, IOException {
        try {
            Authentication authenticationResult = this.authenticationManager.authenticate(token);
            if (authenticationResult == null) {
                throw new UsernameNotFoundException("账户不存在!", null);
            }
            this.sessionStrategy.onAuthentication(authenticationResult, request, response);
            successfulAuthentication(request, response, authenticationResult);
            return authenticationResult;
        } catch (InternalAuthenticationServiceException failed) {
            log.error("尝试对用户进行身份验证时发生内部错误:", failed);
            unsuccessfulAuthentication(request, response, failed);
            throw failed;
        } catch (AuthenticationException ex) {
            unsuccessfulAuthentication(request, response, ex);
            throw ex;
        }
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        log.trace("认证失败,清除 SecurityContextHolder");
        this.rememberMeServices.loginFail(request, response);
        if (this.failureHandler != null) {
            this.failureHandler.onAuthenticationFailure(request, response, failed);
        }
    }


    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authResult) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);
        log.debug("设置 SecurityContextHolder 为: {}", authResult);
        this.rememberMeServices.loginSuccess(request, response, authResult);
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        }
        if (this.successHandler != null) {
            this.successHandler.onAuthenticationSuccess(request, response, authResult);
        }
    }

    @Autowired(required = false)
    public void setSuccessHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Autowired(required = false)
    public void setFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }
}
