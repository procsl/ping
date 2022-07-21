package cn.procsl.ping.boot.admin.auth;

import cn.procsl.ping.boot.common.web.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
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
@RequiredArgsConstructor
public class PermissionAccessDeniedHandler implements AccessDeniedHandler {

    @Value("${server.error.path:/error}")
    String url;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("拒绝访问:[{} {}]", request.getMethod(), request.getRequestURI());
        ResponseUtils.forbiddenError(request, response, url, "001", "无权限,拒绝访问");
    }

}
