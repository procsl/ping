package cn.procsl.ping.boot.rest.exception.resolver;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import cn.procsl.ping.boot.rest.exception.ExceptionCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class RestHandlerExceptionResolver extends AbstractHandlerExceptionResolver {


    /**
     * Actually resolve the given exception that got thrown during handler execution,
     * returning a {@link ModelAndView} that represents a specific error page if appropriate.
     * <p>May be overridden in subclasses, in order to apply specific exception checks.
     * Note that this template method will be invoked <i>after</i> checking whether this
     * resolved applies ("mappedHandlers" etc), so an implementation may simply proceed
     * with its actual exception handling.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  the executed handler, or {@code null} if none chosen at the time
     *                 of the exception (for example, if multipart resolution failed)
     * @param ex       the exception that got thrown during handler execution
     * @return a corresponding {@code ModelAndView} to forward to,
     * or {@code null} for default processing in the resolution chain
     */
    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ModelAndView mv = new ModelAndView();
        ExceptionCode code = new ExceptionCode();
        code.setMessage(ex.getMessage());
        code.setCode("001");
        mv.addObject(RestWebProperties.modelKey, code);
        mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        log.warn("全局异常处理,请求异常:", ex);
        return mv;
    }
}
