package cn.procsl.ping.admin.config.security;

import cn.procsl.ping.boot.rest.exception.ExceptionCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class PermissionAccessDeniedHandler implements AccessDeniedHandler, InitializingBean {

    final ObjectMapper objectMapper;

    private byte[] message;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("拒绝访问:[{} {}]", request.getMethod(), request.getRequestURI());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(message);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final ExceptionCode denied = ExceptionCode.builder("403000", "Permission access denied.");
        this.message = objectMapper.writeValueAsBytes(denied);
    }
}
