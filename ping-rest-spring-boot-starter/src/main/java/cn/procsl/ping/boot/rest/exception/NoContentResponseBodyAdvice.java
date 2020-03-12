package cn.procsl.ping.boot.rest.exception;

import cn.procsl.ping.boot.rest.config.RestWebProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author procsl
 * @date 2020/02/25
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class NoContentResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    final RestWebProperties properties;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
//        Class<?> controller = returnType.getMethod().getDeclaringClass();
//        for (String s : this.properties.getRestControllerPackageName()) {
//            boolean bool = controller.getName().startsWith(s);
//            if (bool) {
//                Class<?> returnParameter = returnType.getParameterType();
//                return "void".equals(returnParameter.getName())
//                        ||
//                        returnType.hasMethodAnnotation(NoContent.class)
//                        ||
//                        Void.class.isAssignableFrom(returnType.getParameterType());
//
//            }
//        }
        return false;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.NO_CONTENT);
        return null;
    }
}
