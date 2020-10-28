package cn.procsl.ping.boot.rest.exception.resolver;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import cn.procsl.ping.boot.rest.exception.ParameterError;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Order(Integer.MIN_VALUE)
@RequiredArgsConstructor
public class MethodArgumentNotValidExceptionResolver extends AbstractHandlerExceptionResolver {

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException tmp = (MethodArgumentNotValidException) ex;
            ModelAndView mv = new ModelAndView();
            mv.setStatus(HttpStatus.BAD_REQUEST);
            List<Map<String, String>> errors = tmp.getBindingResult().getFieldErrors().stream().map(item -> {
                HashMap<String, String> t = new HashMap<>(1);
                t.put(item.getField(), item.getDefaultMessage());
                return t;
            }).collect(Collectors.toList());

            mv.addObject(RestWebProperties.modelKey, new ParameterError("001", "参数校验失败", errors));
            return mv;
        }

        return null;
    }
}
