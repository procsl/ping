package cn.procsl.ping.boot.rest.exception.resolver;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import cn.procsl.ping.boot.rest.exception.ExceptionCode;
import cn.procsl.ping.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class BusinessExceptionResolver extends AbstractHandlerExceptionResolver {

    @Override
    protected ModelAndView doResolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        if (e instanceof BusinessException) {
            ModelAndView mv = new ModelAndView();
            ExceptionCode code = new ExceptionCode();
            code.setMessage(e.getMessage());
            code.setCode(String.format("%s%s", ((BusinessException) e).httpStatus(), ((BusinessException) e).code()));
            mv.addObject(RestWebProperties.modelKey, code);
            mv.setStatus(HttpStatus.resolve(((BusinessException) e).httpStatus()));
            log.warn("业务异常处理:", e);
            return mv;
        }
        return null;
    }


}
