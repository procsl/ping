package cn.procsl.ping.admin.config.security;

import cn.procsl.ping.admin.exception.ExceptionCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Indexed
@Component
@RequiredArgsConstructor
public class NotLoginAuthentication implements AuthenticationEntryPoint, InitializingBean {


    final ObjectMapper objectMapper;

    private byte[] message;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.debug("未登录的访问:", authException);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getOutputStream().write(message);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        message = objectMapper.writeValueAsBytes(ExceptionCode.builder("401000", "你尚未登录,请登录"));
    }

}
