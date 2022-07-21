package cn.procsl.ping.boot.admin.auth;

import cn.procsl.ping.boot.common.web.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Indexed
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Value("${server.error.path:/error}")
    String url;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.info("登录失败");
        ResponseUtils.unauthorizedError(request, response, url, "002", "账户或密码错误");
    }

}
