package cn.procsl.ping.boot.common.web;

import cn.procsl.ping.boot.common.CommonAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@Indexed
@Component
public class WebErrorAttributes extends DefaultErrorAttributes {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
                                         Exception ex) {
        log.debug("解析异常", ex);
        return super.resolveException(request, response, handler, ex);
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errors = super.getErrorAttributes(webRequest, options);
        log.info("errors:{}", errors);
        Object message = errors.get("message");
        if (message == null) {
            message = webRequest.getAttribute(RequestDispatcher.ERROR_MESSAGE, RequestAttributes.SCOPE_REQUEST);
        }
        if (message == null) {
            message = errors.get("error");
        }
        if (message == null) {
            message = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        }

        Object code = webRequest.getAttribute(CommonAutoConfiguration.SYSTEM_ERROR_CODE_KEY,
                RequestAttributes.SCOPE_REQUEST);
        if (code == null) {
            code = String.format("%s%s", errors.getOrDefault("status", HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    "001");
        }
        return Map.of("message", message, "code", code);
    }

    @Override
    protected String getMessage(WebRequest webRequest, Throwable error) {
        String message = super.getMessage(webRequest, error);
        log.debug("message:{}", message);
        return message;
    }

    @Override
    public Throwable getError(WebRequest webRequest) {
        Throwable exception = super.getError(webRequest);
        log.debug("exception:", exception);
        return exception;
    }
}
