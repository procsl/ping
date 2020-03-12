package cn.procsl.ping.boot.rest.config;

import cn.procsl.ping.boot.rest.adapter.RestRequestMappingHandlerAdapter;
import cn.procsl.ping.boot.rest.hook.RegisterMappingHook;
import cn.procsl.ping.boot.rest.hook.RequestAdapterConfigHook;
import cn.procsl.ping.boot.rest.hook.RequestAdapterHook;
import cn.procsl.ping.boot.rest.hook.RequestMappingBuilderHook;
import cn.procsl.ping.boot.rest.mapping.RestRequestMappingHandlerMapping;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;

import javax.servlet.Servlet;
import java.nio.charset.Charset;
import java.util.*;

import static cn.procsl.ping.boot.rest.config.RestWebProperties.MetaMediaType.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

/**
 * @author procsl
 * @date 2020/02/18
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({RestWebProperties.class, VersionStrategyProperties.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@AutoConfigureAfter({RestDispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class, ValidationAutoConfiguration.class})
@ConditionalOnMissingBean(RestWebAutoConfiguration.class)
public class RestWebAutoConfiguration implements ApplicationContextAware {

    private final RestWebProperties properties;

    @Getter
    private Map<RestWebProperties.MetaMediaType, List<MediaType>> mediaTypes;

    @Getter
    private HashMap<String, ObjectMapper> objectMappers;

    private ApplicationContext applicationContext;


    public RestWebAutoConfiguration(RestWebProperties properties) {
        this.properties = properties;

        // 创建支持的媒体类型
        createMediaTypes();

        // 创建ObjectMapper
        createObjectMappers();

        // 创建Views
        createView();

    }

    private void setMediaTypes(RestWebProperties.MetaMediaType key, MediaType mediaType) {
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


    protected void createView() {
        if (mediaTypes.containsKey(json.name())) {
            JsonView view = new JsonView(this.objectMappers.get(json.name()), this.properties);
            view.setUpdateContentLength(true);
        }

        if (mediaTypes.containsKey(xml.name())) {
            MappingJackson2XmlView view = new MappingJackson2XmlView((XmlMapper) this.objectMappers.get(xml.name()));
            view.setModelKey(this.properties.getModelKey());
            view.setUpdateContentLength(true);
        }

        // TODO yaml
    }

    protected void createObjectMappers() {
        SerializableFilter serializableFilter = new SerializableFilter(null);

        this.objectMappers = new HashMap<>(this.mediaTypes.size());
        if (this.mediaTypes.containsKey(json.name())) {
            this.objectMappers.put(json.name(), createJsonMapper(serializableFilter));
        }

        if (this.mediaTypes.containsKey(xml.name())) {
            this.objectMappers.put(xml.name(), createXmlMapper(serializableFilter));
        }

        if (this.mediaTypes.containsKey(yaml.name())) {
            this.objectMappers.put(yaml.name(), createYamlMapper(serializableFilter));
        }
    }

    protected ObjectMapper createYamlMapper(SerializableFilter serializableFilter) {
        return new ObjectMapper();
    }

    protected void createMediaTypes() {
        mediaTypes = new HashMap<>(3);
        Set<RestWebProperties.MetaMediaType> mediatypes =
                this.properties.getMetaMediaTypes() == null || this.properties.getMetaMediaTypes().isEmpty()
                        ? Collections.singleton(json) : this.properties.getMetaMediaTypes();

        switch (this.properties.getRepresentationStrategy()) {
            case system_mime:
                pushSystemMime(mediatypes);
                break;
            case custom_mime:
                pushCustomMime(mediatypes);
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
                case yaml:
                    MediaType yamlMime = new MediaType("application", yaml.name());
                    setMediaTypes(yaml, yamlMime);
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
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @Primary
    public WebComponentRegister webComponentRegister(@Autowired(required = false) List<RegisterMappingHook> registeRhook,
                                                     @Autowired(required = false) List<RequestAdapterHook> adapterHook) {
        ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
        RestRequestMappingHandlerMapping mapping = new RestRequestMappingHandlerMapping(registeRhook);
        RestRequestMappingHandlerAdapter adapter = new RestRequestMappingHandlerAdapter(adapterHook);
        return new WebComponentRegister(mapping, adapter, resolver);
    }

    /**
     * 基于前缀的版本管理
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public RequestMappingBuilderHook pathVersioningHook() {
        return new RequestMappingBuilderHook(this.properties, this.mediaTypes);
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestAdapterConfigHook requestAdapterConfigHook() {
        return new RequestAdapterConfigHook(this.mediaTypes, this.objectMappers, this.applicationContext);
    }

    @Bean
    public HttpMessageConverters httpMessageConverters() {
        HttpMessageConverter[] converters = new HttpMessageConverter[]{
                new StringHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(),
                new MappingJackson2XmlHttpMessageConverter(),
                new ByteArrayHttpMessageConverter(),
                new ResourceHttpMessageConverter()
        };
        return new HttpMessageConverters(false, Arrays.asList(converters));
    }

}
