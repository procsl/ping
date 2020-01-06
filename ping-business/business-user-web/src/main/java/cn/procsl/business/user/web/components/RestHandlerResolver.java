package cn.procsl.business.user.web.components;

import cn.procsl.business.user.web.annotation.Accepted;
import cn.procsl.business.user.web.annotation.Created;
import cn.procsl.business.user.web.annotation.NoContent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author procsl
 * @date 2019/12/29
 */
@Slf4j
public class RestHandlerResolver implements HandlerMethodReturnValueHandler, Ordered, InitializingBean {

    @Setter
    @Getter
    int order;

    @Setter
    @Value("${ping.business.web.returnKey:__return_key__}")
    private String returnKey;

    @Autowired
    @Setter
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Setter
    private RequestResponseBodyMethodProcessor requestResponseBodyMethodProcessor;


    /**
     * 拦截所有的类型
     *
     * @param returnType
     * @return
     */
    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return true;
    }

    @Override
    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws Exception {

        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        // 判断是否为简单类型,如果是则直接根据相关的注解返回
        if (!isSimpleType(returnValue, returnType)) {
            mavContainer.addAttribute(this.returnKey, returnValue);
        }

        response.setHeader("Content-type", MediaType.TEXT_HTML_VALUE);
        do {

            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            if (HttpMethod.GET.matches(request.getMethod())) {
                break;
            }

            // 如果存在此注解或者为Void类型, 直接返回
            if (returnType.getParameterType() == Void.class || returnType.hasMethodAnnotation(NoContent.class)) {
                this.requestResponseBodyMethodProcessor.handleReturnValue(null, returnType, mavContainer, webRequest);
                response.setStatus(HttpStatus.NO_CONTENT.value());
                return;
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
                log.debug("location is null. skip");
                break;
            }

            String id = this.convert(returnValue, returnType);
            if (id == null || id.isEmpty()) {
                log.debug("id is null. skip");
                break;
            }

            String link = replace(create.name(), location, id);
            response.setHeader("Location", link);
        } while (false);
        this.requestResponseBodyMethodProcessor.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }


    private boolean isSimpleType(Object returnValue, MethodParameter returnType) throws IOException {
        if (returnValue == null) {
            return true;
        }
        if (returnType.getParameterType() == Void.class) {
            return true;
        }
        if (returnType.getParameterType() == String.class) {
            return true;
        }
        if (returnType.getParameterType().isInstance(Number.class)) {
            return true;
        }
        return false;
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

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.requestResponseBodyMethodProcessor == null) {
            this.requestResponseBodyMethodProcessor = new RequestResponseBodyMethodProcessor(this.requestMappingHandlerAdapter.getMessageConverters());
        }
    }
}
