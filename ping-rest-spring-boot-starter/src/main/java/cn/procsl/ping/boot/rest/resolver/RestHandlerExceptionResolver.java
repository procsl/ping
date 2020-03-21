package cn.procsl.ping.boot.rest.resolver;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import cn.procsl.ping.boot.rest.exception.ExceptionCode;
import cn.procsl.ping.boot.rest.exception.NotFoundException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 基于rest风格的全局异常处理
 *
 * @author procsl
 * @date 2019/12/25
 */
@Slf4j
public class RestHandlerExceptionResolver extends AbstractHandlerExceptionResolver {

    @Setter
    @Getter
    protected int order = 0;

    private final RestWebProperties properties;

    @Setter
    private List<String> accepts;

    public RestHandlerExceptionResolver(RestWebProperties properties, List<String> accepts) {
        this.properties = properties;
        this.accepts = accepts;
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        ModelAndView mv = new ModelAndView();
        ExceptionCode code = new ExceptionCode();
        log.warn("请求异常:", ex);
        // 默认设置状态为500
        mv.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        do {
            if (ex instanceof NotFoundException) {
                mv.setStatus(HttpStatus.NOT_FOUND);
                break;
            }

            if (ex instanceof HttpMediaTypeNotAcceptableException) {
                mv.setStatus(HttpStatus.NOT_ACCEPTABLE);
                code.setCode(mv.getStatus().toString() + "001");
                code.setMessage(ex.getMessage());
                break;
            }

            if (ex instanceof RuntimeException) {
                ExceptionCode tmp = this.create(request, response, handler, (RuntimeException) ex, mv);
                return mv.addObject(properties.getModelKey(), tmp);
            }

        } while (false);

        mv.addObject(properties.getModelKey(), code);
        code.setCode(mv.getStatus().value() + "001");
        code.setMessage(log.isDebugEnabled() ? ex.getMessage() : mv.getStatus().toString());
        return mv;

    }

    /**
     * 用于创建包装类
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @param ex       异常
     * @param mv       包装
     * @return 返回异常处理包装
     */
    public ExceptionCode create(HttpServletRequest request, HttpServletResponse response,
                                Object handler, RuntimeException ex, ModelAndView mv) {

        ExceptionCode tmp = new ExceptionCode();
        tmp.setCode(mv.getStatus().value() + "001");
        tmp.setMessage(log.isDebugEnabled() ? ex.getMessage() : "系统出现错误, 请稍后重试");
        return tmp;
    }

}
