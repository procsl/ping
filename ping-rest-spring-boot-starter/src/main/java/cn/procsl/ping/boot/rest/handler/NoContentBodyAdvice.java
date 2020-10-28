package cn.procsl.ping.boot.rest.handler;

import cn.procsl.ping.boot.rest.annotation.Hypermedia;
import cn.procsl.ping.boot.rest.annotation.Ok;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

class NoContentBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Ok ok = returnType.getMethodAnnotation(Ok.class);
        if (ok != null && ok.status().equals(HttpStatus.NO_CONTENT)) {
            return true;
        }

        if (returnType.hasMethodAnnotation(Hypermedia.class)) {
            return false;
        }

        Class<?> type = returnType.getMethod().getReturnType();
        return ClassUtils.isAssignable(Void.class, type);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.NO_CONTENT);
        return null;
    }
}
