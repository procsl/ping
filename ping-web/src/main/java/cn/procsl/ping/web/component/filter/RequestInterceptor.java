package cn.procsl.ping.web.component.filter;

import cn.procsl.ping.web.component.RequestLifeCycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 缓存拦截器
 *
 * @author procsl
 * @date 2019/12/28
 */
@Slf4j
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired(required = false)
    List<RequestLifeCycle> lifeCycles;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (lifeCycles != null) {
            for (RequestLifeCycle lifeCycle : lifeCycles) {
                lifeCycle.start(request, response);
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (lifeCycles != null) {
            for (RequestLifeCycle lifeCycle : lifeCycles) {
                lifeCycle.end(request, response);
            }
        }
    }

}
