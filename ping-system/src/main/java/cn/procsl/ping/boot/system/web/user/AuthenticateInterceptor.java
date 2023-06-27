package cn.procsl.ping.boot.system.web.user;

import cn.procsl.ping.boot.system.domain.user.AuthenticateException;
import cn.procsl.ping.boot.web.component.AbstractMethodAnnotationInterceptor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static cn.procsl.ping.boot.system.web.user.AuthenticateController.AUTHENTICATION_KEY;

public class AuthenticateInterceptor extends AbstractMethodAnnotationInterceptor<PermitAll> {

    private final String[] prefix;

    public AuthenticateInterceptor(@Nonnull String[] prefix) {
        super(PermitAll.class);
        this.prefix = prefix;
    }

    @Override
    protected boolean doPreHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler, PermitAll annotation) {
        boolean permit = annotation != null;
        if (permit) {
            return true;
        }

        boolean bool = prefix.length != 0;
        String name = handler.getBeanType().getName();
        for (String s : prefix) {
            bool = bool && name.startsWith(s);
        }

        // 如果检测不到指定的包, 则直接返回
        if (!bool) {
            return true;
        }

        // 如果已经登录, 则直接返回
        Object id = request.getSession().getAttribute(AUTHENTICATION_KEY);
        if (id != null) {
            return true;
        }
        throw new AuthenticateException("用户尚未登录");
    }
}
