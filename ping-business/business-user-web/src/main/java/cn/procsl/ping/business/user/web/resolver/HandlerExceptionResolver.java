package cn.procsl.ping.business.user.web.resolver;

import cn.procsl.ping.business.exception.BusinessException;
import cn.procsl.ping.web.component.error.RestError;
import cn.procsl.ping.web.component.view.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基于rest风格的全局异常处理
 *
 * @author procsl
 * @date 2019/12/25
 */
@Slf4j
public class HandlerExceptionResolver extends AbstractHandlerExceptionResolver {

    @Value("ping.business.user.web.defaultMessage:系统错误")
    private String defaultMessage;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ModelAndView mv = new ModelAndView();
        // 只要是异常, 禁用缓存
        response.setHeader("Cache-Control", "no-store");

        // 业务异常依据抛出的业务的异常的指示返回状态码
        if (ex instanceof BusinessException) {
            response.setStatus(((BusinessException) ex).httpStatus());
            RestError error = new RestError();
            error.setMessage(ex.getMessage());
            error.setCode(((BusinessException) ex).httpStatus() + ((BusinessException) ex).getCode());
            return mv.addObject(Constant.ERROR_VALUE.getValue(), error);
        }

        if (ex instanceof HttpRequestMethodNotSupportedException) {
            response.setStatus(406);
            RestError error = new RestError();
            error.setMessage(log.isDebugEnabled() ? ex.getMessage() : "Not Acceptable");
            error.setCode("4006001");
            return mv.addObject(Constant.ERROR_VALUE.getValue(), error);
        }

        if (ex instanceof NoHandlerFoundException) {
            RestError error = new RestError();
            error.setMessage(log.isDebugEnabled() ? ex.getMessage() : "Not Found");
            error.setCode("4004001");
            return mv.addObject(Constant.ERROR_VALUE.getValue(), error);
        }

        response.setStatus(500);
        RestError error = new RestError();
        error.setMessage(log.isDebugEnabled() ? ex.getMessage() : this.defaultMessage);
        error.setCode("500000");
        log.error("全局异常", ex);
        return mv.addObject(Constant.ERROR_VALUE.getValue(), error);
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
