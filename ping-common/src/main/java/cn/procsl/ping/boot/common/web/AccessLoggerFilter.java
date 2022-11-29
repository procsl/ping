package cn.procsl.ping.boot.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class AccessLoggerFilter extends OncePerRequestFilter implements InitializingBean {

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
        MDC.put("RequestId", requestId);
        response.setHeader(traceName, MDC.get("RequestId"));
        filterChain.doFilter(request, response);
    }


}
