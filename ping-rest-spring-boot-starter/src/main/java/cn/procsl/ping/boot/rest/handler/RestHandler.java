package cn.procsl.ping.boot.rest.handler;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

/**
 * @author procsl
 * @date 2019/12/29
 */
@Slf4j
@RequiredArgsConstructor
public class RestHandler implements HandlerMethodReturnValueHandler, Ordered {

    @Setter
    @Getter
    int order;

    final RequestResponseBodyMethodProcessor processor;

    final RestWebProperties properties;

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
        if (mavContainer.containsAttribute(properties.getModelKey())) {
            return;
        }
        mavContainer.addAttribute(properties.getModelKey(), returnValue);
    }

}
