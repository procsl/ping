package cn.procsl.ping.web.component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author procsl
 * @date 2020/02/16
 */
public interface RequestLifeCycle {

    /**
     * 请求时
     *
     * @param request
     * @param response
     */
    void start(HttpServletRequest request, HttpServletResponse response);

    /**
     * 结束时
     *
     * @param request
     * @param response
     */
    void end(HttpServletRequest request, HttpServletResponse response);
}
