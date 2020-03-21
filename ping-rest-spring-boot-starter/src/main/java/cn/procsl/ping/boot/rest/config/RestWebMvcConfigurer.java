package cn.procsl.ping.boot.rest.config;

import cn.procsl.ping.boot.rest.resolver.RestHandlerExceptionResolver;
import cn.procsl.ping.boot.rest.web.RestExtensionContentNegotiationStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.ServletContext;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.json;
import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.xml;

/**
 * 一些默认的配置
 *
 * @author procsl
 * @date 2020/03/13
 */
public class RestWebMvcConfigurer implements WebMvcConfigurer, ServletContextAware, InitializingBean {


    final EnumMap<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes;

    final EnumMap<RestWebProperties.MetaMediaType, ObjectMapper> objectMappers;

    final ApplicationContext applicationContext;

    final RestWebProperties properties;

    RestExtensionContentNegotiationStrategy customerContextNegotiationStrategy;


    public RestWebMvcConfigurer(EnumMap<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes,
                                EnumMap<RestWebProperties.MetaMediaType, ObjectMapper> objectMappers,
                                ApplicationContext applicationContext, RestWebProperties properties) {
        this.mediaTypes = mediaTypes;
        this.objectMappers = objectMappers;
        this.applicationContext = applicationContext;
        this.customerContextNegotiationStrategy = new RestExtensionContentNegotiationStrategy(mediaTypes);
        this.properties = properties;
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false)
                .favorParameter(false)
                .defaultContentTypeStrategy(customerContextNegotiationStrategy)
                .ignoreUnknownPathExtensions(true)
                .useRegisteredExtensionsOnly(true);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseRegisteredSuffixPatternMatch(true)
                .setUseSuffixPatternMatch(false)
                .setUseTrailingSlashMatch(true);
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.clear();
        List<String> accepts = new LinkedList<>();
        this.mediaTypes.forEach((k, v) -> v.forEach(item -> accepts.add(item.toString())));
        resolvers.add(new RestHandlerExceptionResolver(this.properties, accepts));
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

        {
            ObjectMapper jsonMapper = this.objectMappers.get(json);
            Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
            builder.applicationContext(this.applicationContext);
            builder.configure(jsonMapper);
            jsonConvert.setObjectMapper(jsonMapper);
        }

        {
            ObjectMapper xmlMapper = this.objectMappers.get(xml);
            Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
            builder.applicationContext(this.applicationContext);
            builder.configure(xmlMapper);
            xmlConvert.setObjectMapper(xmlMapper);
        }
    }

    protected MappingJackson2HttpMessageConverter createJackson2HttpMessageConverter() {
        ObjectMapper jsonMapper = this.objectMappers.get(json);
        MappingJackson2HttpMessageConverter jsonConverter;
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        builder.applicationContext(this.applicationContext);
        builder.configure(jsonMapper);
        jsonConverter = new MappingJackson2HttpMessageConverter(jsonMapper);
        jsonConverter.setObjectMapper(jsonMapper);
        jsonConverter.setSupportedMediaTypes(this.mediaTypes.get(json));
        return jsonConverter;
    }

    protected MappingJackson2XmlHttpMessageConverter createJackson2XmlHttpMessageConverter() {
        ObjectMapper xmlMapper = this.objectMappers.get(xml);
        MappingJackson2XmlHttpMessageConverter xmlConverter;
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.xml();
        builder.applicationContext(this.applicationContext);
        builder.configure(xmlMapper);
        xmlConverter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);
        xmlConverter.setObjectMapper(xmlMapper);
        xmlConverter.setSupportedMediaTypes(this.mediaTypes.get(xml));
        return xmlConverter;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.customerContextNegotiationStrategy.setServletContext(servletContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.customerContextNegotiationStrategy.afterPropertiesSet();
    }


}
