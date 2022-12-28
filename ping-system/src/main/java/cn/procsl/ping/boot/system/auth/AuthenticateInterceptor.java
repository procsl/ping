package cn.procsl.ping.boot.system.auth;

import cn.procsl.ping.boot.common.web.AbstractMethodAnnotationInterceptor;
import cn.procsl.ping.boot.system.domain.user.AuthenticateException;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.server.PathContainer;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import static cn.procsl.ping.boot.system.auth.SessionController.session_key;

public class AuthenticateInterceptor extends AbstractMethodAnnotationInterceptor<PermitAll> {

    PathPattern pattern = PathPatternParser.defaultInstance.parse("/v1/**");


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

        String uri = request.getRequestURI();
        PathContainer pathContainer = PathContainer.parsePath(uri);
        if (!pattern.matches(pathContainer)) {
            return true;
        }

        Object id = request.getSession().getAttribute(session_key);
        if (id != null) {
            return true;
        }
        throw new AuthenticateException("用户尚未登录");
    }
}
