package cn.procsl.ping.boot.web.encrypt;

import cn.procsl.ping.boot.web.annotation.SecurityId;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.IOException;
import java.lang.reflect.Type;


@RestControllerAdvice
@RequiredArgsConstructor
class MarkerRequestBodyAdvice extends RequestBodyAdviceAdapter {

    final HttpServletRequest httpServletRequest;

    @Override
    public boolean supports(MethodParameter methodParameter, @Nonnull Type targetType, @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasParameterAnnotation(SecurityId.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(
            @Nonnull HttpInputMessage inputMessage,
            @Nonnull MethodParameter parameter,
            @Nonnull Type targetType,
            @Nonnull Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        SecurityId securityId = parameter.getParameterAnnotation(SecurityId.class);
        httpServletRequest.setAttribute(SecurityId.class.getName(), securityId);
        return super.beforeBodyRead(inputMessage, parameter, targetType, converterType);
    }

    @Override
    public Object afterBodyRead(@Nonnull Object body,
                                @Nonnull HttpInputMessage inputMessage,
                                @Nonnull MethodParameter parameter,
                                @Nonnull Type targetType,
                                @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
        httpServletRequest.removeAttribute(SecurityId.class.getName());
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }
}
