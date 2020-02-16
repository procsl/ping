package cn.procsl.ping.web.component.resolver;

import cn.procsl.ping.web.annotation.Accepted;
import cn.procsl.ping.web.annotation.Created;
import cn.procsl.ping.web.annotation.NoContent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author procsl
 * @date 2020/02/16
 */
@Slf4j
public class SimpleTypeHandlerResolver implements HandlerMethodReturnValueHandler, Ordered {


    @Getter
    @Setter
    int order = 0;

    @Setter
    @Autowired(required = false)
    private RequestResponseBodyMethodProcessor process;

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return "void".equals(returnType.getParameterType().getName()) ||
                Void.class.isAssignableFrom(returnType.getParameterType()) ||
                String.class.isAssignableFrom(returnType.getParameterType()) ||
                Date.class.isAssignableFrom(returnType.getParameterType()) ||
                Number.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        do {

            HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
            // 如果存在此注解或者为Void类型, 直接返回
            if ("void".equals(returnType.getParameterType().getName()) || Void.class.isAssignableFrom(returnType.getParameterType()) || returnType.hasMethodAnnotation(NoContent.class)) {
                response.setStatus(HttpStatus.NO_CONTENT.value());
                this.process.handleReturnValue(null, returnType, mavContainer, webRequest);
                return;
            }

            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (HttpMethod.GET.matches(request.getMethod())) {
                break;
            }

            if (returnType.hasMethodAnnotation(Accepted.class)) {
                response.setStatus(HttpStatus.ACCEPTED.value());
                break;
            }

            // 如果为Created则可以生成Location
            if (!returnType.hasMethodAnnotation(Created.class)) {
                break;
            }

            response.setStatus(HttpStatus.CREATED.value());
            Created create = returnType.getMethodAnnotation(Created.class);
            String location = create.location();
            if (location == null || location.isEmpty()) {
                log.debug("Location is null. skip");
                break;
            }

            String id = this.convert(returnValue, returnType);
            if (id == null || id.isEmpty()) {
                log.debug("Id is null. skip");
                break;
            }

            String link = replace(create.name(), location, id);
            response.setHeader("Location", link);
        } while (false);
        this.process.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    protected String convert(Object returnValue, MethodParameter returnType) {
        if (returnType.getParameterType() == String.class) {
            return (String) returnValue;
        }

        if (returnValue instanceof Integer) {
            return String.valueOf(returnValue);
        }

        if (returnValue instanceof Long) {
            return String.valueOf(returnValue);
        }

        if (returnValue instanceof Character) {
            return String.valueOf(returnValue);
        }
        return null;
    }

    protected String replace(String name, String old, String newStr) {
        if (name == null || name.isEmpty()) {
            return old;
        }
        return old.replace("{" + name + "}", newStr);
    }

}
