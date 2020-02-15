package cn.procsl.ping.web.component.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @author procsl
 * @date 2020/01/09
 */
@Slf4j
public class LogTraceFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String tmp = UUID.randomUUID().toString();
        MDC.put("trace_id", tmp);
        boolean bool = log.isDebugEnabled();
        if (bool) {
            res.setHeader("X-trace-id", tmp);
        }
        chain.doFilter(req, res);
    }
}
