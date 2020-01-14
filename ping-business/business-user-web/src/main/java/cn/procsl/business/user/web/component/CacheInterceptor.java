package cn.procsl.business.user.web.component;

import com.github.bohnman.squiggly.web.SquigglyRequestHolder;
import com.github.bohnman.squiggly.web.SquigglyResponseHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 缓存拦截器
 *
 * @author procsl
 * @date 2019/12/28
 */
@Slf4j
public class CacheInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SquigglyRequestHolder.removeRequest();
        SquigglyResponseHolder.removeResponse();
        log.debug("移除字段过滤器");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        SquigglyRequestHolder.setRequest(request);
        SquigglyResponseHolder.setResponse(response);
        // 视图处理前设置过滤器
        log.debug("设置字段过滤器");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SquigglyRequestHolder.removeRequest();
        SquigglyResponseHolder.removeResponse();
        log.debug("移除字段过滤器");
    }
}
