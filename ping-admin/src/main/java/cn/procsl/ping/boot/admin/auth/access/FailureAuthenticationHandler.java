package cn.procsl.ping.boot.admin.auth.access;

import cn.procsl.ping.boot.common.web.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Indexed
@Component
public class FailureAuthenticationHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Value("${server.error.path:/error}")
    String error;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException, ServletException {

        ResponseUtils.unauthorizedError(request, response, error, "401001", "你尚未登录,请登录");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException e) throws IOException, ServletException {
        ResponseUtils.forbiddenError(request, response, error, "403001", "无权限,拒绝访问");
    }
}
