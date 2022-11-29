package cn.procsl.ping.boot.system.auth.login;

import cn.procsl.ping.boot.common.web.AbstractMethodAnnotationInterceptor;
import cn.procsl.ping.boot.system.domain.user.AuthenticateException;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static cn.procsl.ping.boot.system.auth.login.SessionController.session_key;

public class AuthenticateInterceptor extends AbstractMethodAnnotationInterceptor<PermitAll> {

    public AuthenticateInterceptor() {
        super(PermitAll.class);
    }

    @Override
    protected boolean doPreHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler,
                                  PermitAll annotation) {
        boolean permit = annotation != null;
        if (permit) {
            return true;
        }
        Object id = request.getSession().getAttribute(session_key);
        if (id != null) {
            return true;
        }
        throw new AuthenticateException("用户尚未登录");
    }
}
