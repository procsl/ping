package cn.procsl.ping.web.component.resolver;

import cn.procsl.ping.web.component.view.Constant;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.Date;

/**
 * @author procsl
 * @date 2019/12/29
 */
@Slf4j
public class RestHandlerResolver implements HandlerMethodReturnValueHandler, Ordered {

    @Setter
    @Getter
    int order;

    @Autowired
    RequestResponseBodyMethodProcessor processor;

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

        if (returnValue == null) {
            processor.handleReturnValue(null, returnType, mavContainer, webRequest);
            return;
        }
        mavContainer.addAttribute(Constant.RETURN_VALUE.getValue(), returnValue);
    }

    private boolean notHttpEntityType(MethodParameter returnType) {
        return !(HttpEntity.class.isAssignableFrom(returnType.getParameterType()) && !RequestEntity.class.isAssignableFrom(returnType.getParameterType()));
    }

    private boolean notSimpleTye(MethodParameter returnType) {
        return !(Void.class.isAssignableFrom(returnType.getParameterType()) ||
                String.class.isAssignableFrom(returnType.getParameterType()) ||
                Date.class.isAssignableFrom(returnType.getParameterType()) ||
                Number.class.isAssignableFrom(returnType.getParameterType()));
    }


}
