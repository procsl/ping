package cn.procsl.ping.boot.system.web.user;

import cn.procsl.ping.boot.system.domain.user.AuthenticateException;
import cn.procsl.ping.boot.web.component.AbstractMethodAnnotationInterceptor;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static cn.procsl.ping.boot.system.web.user.AuthenticateController.AUTHENTICATION_KEY;

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

        Object id = request.getSession().getAttribute(AUTHENTICATION_KEY);
        if (id != null) {
            return true;
        }
        throw new AuthenticateException("用户尚未登录");
    }
}
