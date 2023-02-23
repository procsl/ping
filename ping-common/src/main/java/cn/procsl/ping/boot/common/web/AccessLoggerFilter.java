package cn.procsl.ping.boot.common.web;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class AccessLoggerFilter extends OncePerRequestFilter implements InitializingBean {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String traceName = "X-Request-Id";
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
