package cn.procsl.ping.boot.rest.config;

import cn.procsl.ping.boot.rest.exception.resolver.RestViewResolver;
import cn.procsl.ping.boot.rest.serial.PropertyFilterMixin;
import cn.procsl.ping.boot.rest.serial.SerializableFilter;
import cn.procsl.ping.boot.rest.view.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;

import javax.servlet.Servlet;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.json;
import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.xml;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML_VALUE;


@Slf4j
@Configuration
@EnableConfigurationProperties({RestWebProperties.class, VersionStrategyProperties.class, DefaultExceptionResolver.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@AutoConfigureAfter({RestDispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class, ValidationAutoConfiguration.class})
@ConditionalOnMissingBean(RestWebAutoConfiguration.class)
@AutoConfigureBefore({WebMvcAutoConfiguration.class})
public class RestWebAutoConfiguration implements ApplicationContextAware {

    final RestWebProperties properties;

    @Getter
    private EnumMap<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes;

    @Getter
    private EnumMap<RestWebProperties.MetaMediaType, ObjectMapper> objectMappers;

    private ApplicationContext applicationContext;

    private HashMap<String, View> contentTypeMap = new HashMap<>();

    public RestWebAutoConfiguration(RestWebProperties properties) {
        this.properties = properties;

        // 创建支持的媒体类型
        createMediaTypes();

        // 创建ObjectMapper
        createObjectMappers();
    }

    void setMediaTypes(RestWebProperties.MetaMediaType key, MediaType mediaType) {
        if (this.mediaTypes.containsKey(key)) {
            this.mediaTypes.get(key).add(mediaType);
        } else {
            LinkedList<MediaType> list = new LinkedList<>();
            list.push(mediaType);
            this.mediaTypes.put(key, list);
        }
    }

    protected XmlMapper createXmlMapper(SimpleBeanPropertyFilter propertyFilter) {

        XmlMapper mapper = new XmlMapper() {
            @Override
            public ObjectWriter writer() {
                String root = RestWebAutoConfiguration.this.properties.getRootName();
                if (StringUtils.isEmpty(root)) {
                    return super.writer();
                }
                return super.writer().withRootName(root);
            }
        };
        mapper.setDefaultUseWrapper(this.properties.isWrapper());
        if (properties.isIndentOutput()) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, this.properties.isWriteXmlDeclaration());
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(SerializableFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.addMixIn(Object.class, PropertyFilterMixin.class);
        return mapper;
    }

    protected JsonMapper createJsonMapper(SimpleBeanPropertyFilter propertyFilter) {
        JsonMapper mapper = JsonMapper.builder().build();
        if (properties.isIndentOutput()) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(SerializableFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.addMixIn(Object.class, PropertyFilterMixin.class);
        return mapper;
    }


    protected void createObjectMappers() {
        SerializableFilter serializableFilter = new SerializableFilter(null);
        this.objectMappers = new EnumMap<>(RestWebProperties.MetaMediaType.class);
        this.objectMappers.put(json, createJsonMapper(serializableFilter));
        this.objectMappers.put(xml, createXmlMapper(serializableFilter));
    }

    protected void createMediaTypes() {
        mediaTypes = new EnumMap<>(RestWebProperties.MetaMediaType.class);
        Set<RestWebProperties.MetaMediaType> mediaTypes =
            this.properties.getMetaMediaTypes() == null || this.properties.getMetaMediaTypes().isEmpty()
                ? Collections.singleton(json) : this.properties.getMetaMediaTypes();

        switch (this.properties.getRepresentationStrategy()) {
            case system_mime:
                pushSystemMime(mediaTypes);
                break;
            case custom_mime:
                pushCustomMime(mediaTypes);
                break;
        }
    }

    private void pushSystemMime(Set<RestWebProperties.MetaMediaType> mediaTypes) {
        mediaTypes.forEach(type -> {
            switch (type) {
                case json:
                    setMediaTypes(json, APPLICATION_JSON);
                    break;
                case xml:
                    setMediaTypes(xml, APPLICATION_XML);
                    break;
            }
        });
    }

    private void pushCustomMime(Set<RestWebProperties.MetaMediaType> mediaTypes) {
        String mime = this.properties.getMimeSubtype() == null || this.properties.getMimeSubtype().isEmpty() ? "vnd.api" : this.properties.getMimeSubtype();
        mediaTypes.forEach(
            type -> setMediaTypes(
                type,
                new MediaType("application", mime + "+" + type.name(), Charset.defaultCharset())
            )
        );
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public InternalResourceViewResolver internalResourceViewResolver() {
        return new InternalResourceViewResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestViewResolver viewResolver(BeanFactory beanFactory, InternalResourceViewResolver resolver) {
        ContentNegotiationManager bean = beanFactory.getBean(ContentNegotiationManager.class);
        RestViewResolver tmp = new RestViewResolver(this.mediaTypes, resolver);
        tmp.setOrder(Integer.MIN_VALUE);
        tmp.setUseNotAcceptableStatusCode(true);
        tmp.setContentNegotiationManager(bean);

        LinkedList<View> list = new LinkedList<>();
        AtomicBoolean defaultJson = new AtomicBoolean(false);
        AtomicBoolean defaultXml = new AtomicBoolean(false);
        this.mediaTypes.forEach((k, v) -> {
            if (k.equals(json)) {
                for (MediaType mediaType : v) {
                    if (mediaType.isCompatibleWith(MimeTypeUtils.APPLICATION_JSON)) {
                        defaultJson.set(true);
                    }
                    list.add(this.jsonView(mediaType.toString()));
                }
                if (!defaultJson.get()) {
                    list.add(this.jsonView(APPLICATION_JSON_VALUE));
                }
            }

            if (k.equals(xml)) {
                for (MediaType mediaType : v) {
                    if (mediaType.isCompatibleWith(MimeTypeUtils.APPLICATION_XML)) {
                        defaultXml.set(true);
                    }
                    list.add(this.xmlView(mediaType.toString()));
                }

                if (!defaultXml.get()) {
                    list.add(this.xmlView(APPLICATION_XML_VALUE));
                }
            }
        });

        tmp.setDefaultViews(list);

        this.contentTypeMap = null;
        return tmp;
    }

    public JsonView jsonView(String contentType) {
        if (contentTypeMap.containsKey(contentType)) {
            return (JsonView) contentTypeMap.get(contentType);
        }

        JsonView view = new JsonView(this.objectMappers.get(json), RestWebProperties.modelKey);
        view.setContentType(contentType);
        view.setUpdateContentLength(true);

        this.contentTypeMap.put(contentType, view);
        return view;
    }

    public MappingJackson2XmlView xmlView(String contentType) {
        if (contentTypeMap.containsKey(contentType)) {
            return (MappingJackson2XmlView) contentTypeMap.get(contentType);
        }

        MappingJackson2XmlView view = new MappingJackson2XmlView((XmlMapper) this.objectMappers.get(xml));
        view.setModelKey(RestWebProperties.modelKey);
        view.setContentType(contentType);
        view.setUpdateContentLength(true);

        this.contentTypeMap.put(contentType, view);
        return view;
    }

    @Bean
    @ConditionalOnMissingBean
    public RestWebMvcConfigurer restWebMvcConfigurer() {
        return new RestWebMvcConfigurer(this.mediaTypes, this.objectMappers, this.applicationContext);
    }


    @Bean
    public HttpMessageConverters httpMessageConverters() {
        HttpMessageConverter<?>[] converters = new HttpMessageConverter[]{
            new StringHttpMessageConverter(),
            new MappingJackson2HttpMessageConverter(),
            new MappingJackson2XmlHttpMessageConverter(),
            new ByteArrayHttpMessageConverter(),
            new ResourceHttpMessageConverter()
        };
        return new HttpMessageConverters(false, Arrays.asList(converters));
    }

}
