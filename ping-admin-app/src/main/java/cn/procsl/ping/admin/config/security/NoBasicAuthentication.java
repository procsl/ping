package cn.procsl.ping.admin.config.security;

import cn.procsl.ping.admin.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Indexed
@Component
@RequiredArgsConstructor
public class NoBasicAuthentication implements AuthenticationEntryPoint, InitializingBean {

    final static String realm = String.format("Basic realm=\"%s\"", URLEncoder.encode("Ping接口文档", StandardCharsets.UTF_8));
    final ObjectMapper objectMapper;
    byte[] message;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.addHeader("WWW-Authenticate", realm);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getOutputStream().write(message);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.message = objectMapper.writeValueAsBytes(ErrorCode.builder("401000", HttpStatus.UNAUTHORIZED.getReasonPhrase()));
    }
}
