package cn.procsl.ping.boot.common.web;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class AccessLoggerFilter extends AbstractRequestLoggingFilter implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        this.setAfterMessagePrefix("请求结束:[");
        this.setBeforeMessagePrefix("请求开始:[");
    }


    @Override
    protected void beforeRequest(@NonNull HttpServletRequest request, @NonNull String message) {
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String traceName = "X-request-ID";
        String requestId = response.getHeader(traceName);

        if (requestId == null || requestId.isEmpty()) {
            requestId = request.getHeader(traceName);
        }

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().replaceAll("-", "");
        }

        try {
            MDC.put("RequestId", requestId);
        } catch (Exception e) {
            logger.warn("初始化日志信息异常:", e);
        }

        log.debug("HTTP request [{}:{}] remote host: [{}] address: [{}]", request.getMethod(), request.getRequestURI(),
                request.getRemoteHost(),
                request.getRemoteAddr());
        response.setHeader(traceName, MDC.get("RequestId"));
        super.doFilterInternal(request, response, filterChain);
    }


    @Override
    protected void afterRequest(@NonNull HttpServletRequest request, @NonNull String message) {
    }
}
