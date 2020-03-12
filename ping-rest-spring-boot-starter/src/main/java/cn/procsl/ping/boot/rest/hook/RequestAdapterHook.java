package cn.procsl.ping.boot.rest.hook;

import org.springframework.http.converter.HttpMessageConverter;

import java.util.List;

/**
 * @author procsl
 * @date 2020/03/11
 */
public interface RequestAdapterHook {

    /**
     * 消息转换器处理
     *
     * @param httpMessageConverters
     */
    default void httpMessageConverter(List<HttpMessageConverter<?>> httpMessageConverters) {

    }
}
