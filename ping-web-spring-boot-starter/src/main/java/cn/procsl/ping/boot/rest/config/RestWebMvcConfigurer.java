package cn.procsl.ping.boot.rest.config;

import cn.procsl.ping.boot.rest.exception.resolver.BusinessExceptionResolver;
import cn.procsl.ping.boot.rest.exception.resolver.ConfigureHandlerExceptionResolver;
import cn.procsl.ping.boot.rest.exception.resolver.MethodArgumentNotValidExceptionResolver;
import cn.procsl.ping.boot.rest.exception.resolver.RestHandlerExceptionResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 一些默认的配置
 *
 * @author procsl
 * @date 2020/03/13
 */
@Slf4j
public class RestWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.clear();
        resolvers.add(new MethodArgumentNotValidExceptionResolver());
        resolvers.add(new BusinessExceptionResolver());
        resolvers.add(new ConfigureHandlerExceptionResolver());
        resolvers.add(new RestHandlerExceptionResolver());
    }


}
