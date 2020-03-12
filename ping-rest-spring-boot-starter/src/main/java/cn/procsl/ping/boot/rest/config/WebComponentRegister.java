package cn.procsl.ping.boot.rest.config;

import cn.procsl.ping.boot.rest.adapter.RestRequestMappingHandlerAdapter;
import cn.procsl.ping.boot.rest.mapping.RestRequestMappingHandlerMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * springboot 真牛逼
 *
 * @author procsl
 * @date 2020/03/08
 */
@RequiredArgsConstructor
public class WebComponentRegister implements WebMvcRegistrations {

    private final RestRequestMappingHandlerMapping mapping;

    private final RestRequestMappingHandlerAdapter adapter;

    private final ExceptionHandlerExceptionResolver resolver;

    @Override
    public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        return adapter;
    }

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return mapping;
    }

    @Override
    public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver() {
        return resolver;
    }
}
