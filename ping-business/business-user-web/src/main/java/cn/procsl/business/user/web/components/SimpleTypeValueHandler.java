package cn.procsl.business.user.web.components;

import lombok.Setter;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.Date;
import java.util.List;

/**
 * @author procsl
 * @date 2020/01/01
 */
public class SimpleTypeValueHandler implements HandlerMethodReturnValueHandler, Ordered {


    @Setter
    RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    protected RequestResponseBodyMethodProcessor requestResponseBodyMethodProcessor;

    @Setter
    int order;

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.getParameterType() == Integer.class
                || returnType.getParameterType() == String.class
                || returnType.getParameterType() == Long.class
                || returnType.getParameterType() == Short.class
                || returnType.getParameterType() == Double.class
                || returnType.getParameterType() == Float.class
                || returnType.getParameterType() == Byte.class
                || returnType.getParameterType() == Date.class;
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        requestResponseBodyMethodProcessor.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }

    public void init() {
        this.requestResponseBodyMethodProcessor = new RequestResponseBodyMethodProcessor(this.requestMappingHandlerAdapter.getMessageConverters());
        Assert.notNull(this.requestResponseBodyMethodProcessor, "requestResponseBodyMethodProcessor can't be null");
    }

    @Override
    public int getOrder() {
        return order;
    }
}
