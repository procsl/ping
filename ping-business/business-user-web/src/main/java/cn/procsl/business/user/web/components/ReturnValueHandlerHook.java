package cn.procsl.business.user.web.components;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author procsl
 * @date 2019/12/29
 */
public class ReturnValueHandlerHook implements HandlerMethodReturnValueHandler, Ordered {

    @Setter
    int order;

    @Setter
    @Value("${ping.business.web.returnKey:__return_key__}")
    private String returnKey;

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
        mavContainer.addAttribute(this.returnKey, returnValue);
    }

    @Override
    public int getOrder() {
        return order;
    }
}
