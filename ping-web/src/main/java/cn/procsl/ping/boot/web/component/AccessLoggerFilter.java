package cn.procsl.ping.boot.web.component;

import cn.procsl.ping.boot.common.utils.TraceIdGenerator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Slf4j
public class AccessLoggerFilter extends OncePerRequestFilter implements InitializingBean {

    final TraceIdGenerator generator = TraceIdGenerator.initTraceId("yyyyMMdd", 16);


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String traceName = "X-Request-Id";
        String requestId = request.getHeader(traceName);

        if (requestId == null || requestId.isEmpty()) {
            requestId = generator.generateId();
        }
        MDC.put("RequestId", requestId);
        response.setHeader(traceName, requestId);
        filterChain.doFilter(request, response);
    }


}
