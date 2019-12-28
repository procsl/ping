package cn.procsl.business.user.web.error;

import cn.procsl.business.exception.BusinessException;
import cn.procsl.business.user.web.components.resolver.CustomerContentNegotiatingViewResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基于rest风格的全局异常处理
 *
 * @author procsl
 * @date 2019/12/25
 */
@Slf4j
public class RestHandlerExceptionResolver extends AbstractHandlerExceptionResolver {

    @Value("ping.business.user.web.defaultMessage:系统错误")
    private String defaultMessage;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ContentNegotiatingViewResolver contentNegotiatingViewResolver;

    @Autowired
    private CustomerContentNegotiatingViewResolver viewResolver;

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ModelAndView mv = new ModelAndView();

        Object view = request.getAttribute("mark_view");
        if (view == null) {
            try {
                mv.setView(this.viewResolver.resolveViewName("exception_view", response.getLocale()));
            } catch (Exception e) {
                mv.setView(this.viewResolver.getDefaultViews().get(0));
            }
        }

        // 业务异常依据抛出的业务的异常的指示返回状态码
        if (ex instanceof BusinessException) {
            response.setStatus(((BusinessException) ex).httpStatus());
            ErrorEntity error = new ErrorEntity();
            error.setMessage(ex.getMessage());
            error.setCode(((BusinessException) ex).httpStatus() + ((BusinessException) ex).getCode());
            return mv.addObject(error);
        }

        if (ex instanceof HttpRequestMethodNotSupportedException) {
            response.setStatus(406);
            ErrorEntity error = new ErrorEntity();
            error.setMessage(log.isDebugEnabled() ? ex.getMessage() : "Not Acceptable");
            error.setCode("4006001");
            return mv.addObject(error);
        }

        if (ex instanceof NoHandlerFoundException) {
            ErrorEntity error = new ErrorEntity();
            error.setMessage(log.isDebugEnabled() ? ex.getMessage() : "Not Found");
            error.setCode("4004001");
            return mv.addObject(error);
        }

        response.setStatus(500);
        ErrorEntity error = new ErrorEntity();
        error.setMessage(log.isDebugEnabled() ? ex.getMessage() : this.defaultMessage);
        error.setCode("500000");
        log.error("全局异常:{}", ex.getMessage());
        return mv.addObject(error);
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
