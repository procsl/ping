package cn.procsl.ping.boot.rest.web;

import cn.procsl.ping.boot.rest.exception.NotFoundException;
import lombok.NonNull;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author procsl
 * @date 2020/02/21
 */
public class RestDispatcherServlet extends DispatcherServlet {

    private static String getRequestUri(HttpServletRequest request) {
        String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
        if (uri == null) {
            uri = request.getRequestURI();
        }
        return uri;
    }

    @Override
    protected void noHandlerFound(HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        throw new NotFoundException(request.getMethod(), getRequestUri(request));
    }

}
