package cn.procsl.ping.boot.common.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Order
@Slf4j
@Indexed
@Component
public class WebErrorAttributes extends DefaultErrorAttributes {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                         Exception ex) {
        log.info("test");
        return super.resolveException(request, response, handler, ex);
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        log.info("test");
        return super.getErrorAttributes(webRequest, options);
    }

    @Override
    protected String getMessage(WebRequest webRequest, Throwable error) {
        log.info("test");
        return super.getMessage(webRequest, error);
    }

    @Override
    public Throwable getError(WebRequest webRequest) {
        log.info("test");
        return super.getError(webRequest);
    }
}
