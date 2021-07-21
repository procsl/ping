package cn.procsl.ping.boot.rest.config;

import cn.procsl.ping.boot.rest.exception.resolver.ConfigureHandlerExceptionResolver;
import cn.procsl.ping.boot.rest.exception.resolver.MethodArgumentNotValidExceptionResolver;
import cn.procsl.ping.boot.rest.exception.resolver.RestHandlerExceptionResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.EnumMap;
import java.util.List;

import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.json;
import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.xml;

/**
 * 一些默认的配置
 *
 * @author procsl
 * @date 2020/03/13
 */
@Slf4j
public class RestWebMvcConfigurer implements WebMvcConfigurer {


    final EnumMap<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes;

    final EnumMap<RestWebProperties.MetaMediaType, ObjectMapper> objectMappers;

    final ApplicationContext applicationContext;

    public RestWebMvcConfigurer(EnumMap<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes,
                                EnumMap<RestWebProperties.MetaMediaType, ObjectMapper> objectMappers,
                                ApplicationContext applicationContext) {
        this.mediaTypes = mediaTypes;
        this.objectMappers = objectMappers;
        this.applicationContext = applicationContext;
    }


    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.clear();
        resolvers.add(new MethodArgumentNotValidExceptionResolver());
        resolvers.add(new ConfigureHandlerExceptionResolver());
        resolvers.add(new RestHandlerExceptionResolver());
    }


    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jsonConvert = null;
        MappingJackson2XmlHttpMessageConverter xmlConvert = null;

        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                jsonConvert = (MappingJackson2HttpMessageConverter) converter;
            }

            if (converter instanceof MappingJackson2XmlHttpMessageConverter) {
                xmlConvert = (MappingJackson2XmlHttpMessageConverter) converter;
            }
        }

        if (jsonConvert != null) {
            ObjectMapper jsonMapper = this.objectMappers.get(json);
            Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
            builder.applicationContext(this.applicationContext);
            builder.configure(jsonMapper);
            jsonConvert.setObjectMapper(jsonMapper);
        }

        if (xmlConvert != null) {
            ObjectMapper xmlMapper = this.objectMappers.get(xml);
            Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
            builder.applicationContext(this.applicationContext);
            builder.configure(xmlMapper);
            xmlConvert.setObjectMapper(xmlMapper);
        }
    }


}
