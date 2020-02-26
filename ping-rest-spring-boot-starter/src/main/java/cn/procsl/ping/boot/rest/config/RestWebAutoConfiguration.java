package cn.procsl.ping.boot.rest.config;

import cn.procsl.ping.boot.rest.resolver.RestHandlerExceptionResolver;
import cn.procsl.ping.boot.rest.resolver.RestViewResolver;
import cn.procsl.ping.boot.rest.serial.PropertyFilterMixin;
import cn.procsl.ping.boot.rest.serial.SerializableFilter;
import cn.procsl.ping.boot.rest.view.JsonView;
import cn.procsl.ping.boot.rest.web.NoContentResponseBodyAdvice;
import cn.procsl.ping.boot.rest.web.RestPathExtensionContentNegotiationStrategy;
import cn.procsl.ping.boot.rest.web.RestRequestMappingHandlerAdapter;
import cn.procsl.ping.boot.rest.web.RestRequestMappingHandlerMapping;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static cn.procsl.ping.boot.rest.utils.MediaTypeUtils.createMediaType;
import static java.util.Arrays.asList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

/**
 * @author procsl
 * @date 2020/02/18
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({RestWebProperties.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 2)
@AutoConfigureAfter({RestDispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class, ValidationAutoConfiguration.class})
@ConditionalOnMissingBean(RestWebAutoConfiguration.class)
public class RestWebAutoConfiguration extends DelegatingWebMvcConfiguration {

    final RestWebProperties properties;

    @Setter
    private MediaType JSON_TYPE;

    @Setter
    private MediaType XML_TYPE;

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(APPLICATION_JSON);
        configurer.mediaType("json", APPLICATION_JSON)
                .mediaType("xml", APPLICATION_XML);
        if (!StringUtils.isEmpty(properties.getMimeSubtype())) {
            configurer
                    .mediaType("json", JSON_TYPE)
                    .mediaType("xml", XML_TYPE)
                    .defaultContentType(JSON_TYPE, APPLICATION_JSON);
        }
        configurer
                .ignoreUnknownPathExtensions(true)
                .favorParameter(true)
                .useRegisteredExtensionsOnly(true)
                .ignoreAcceptHeader(false)
                .favorPathExtension(true)
                .parameterName(properties.getContentNegotiationParameterName());
    }

    @Override
    protected void configurePathMatch(PathMatchConfigurer configurer) {
        //设置是否自动后缀路径模式匹配，如"/user"是否匹配"/user/"，默认true
        configurer.setUseTrailingSlashMatch(false);
        configurer.setUseRegisteredSuffixPatternMatch(true);
    }

    @Override
    protected RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
        return new RestRequestMappingHandlerAdapter();
    }

    @Override
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new RestRequestMappingHandlerMapping(this.properties);
    }

    @Bean
    public MappingJackson2XmlView mappingJackson2XmlView(@Autowired XmlMapper xmlMapper) {
        MappingJackson2XmlView view = new MappingJackson2XmlView(xmlMapper);
        view.setModelKey(this.properties.getModelKey());
        view.setUpdateContentLength(true);
        return view;
    }

    @Bean
    @Primary
    public JsonView mappingJackson2JsonView(@Autowired JsonMapper jsonMapper) {
        JsonView view = new JsonView(jsonMapper, this.properties);
        view.setUpdateContentLength(true);
        return view;
    }

    @Bean
    @ConditionalOnMissingBean(RestHandlerExceptionResolver.class)
    public RestHandlerExceptionResolver restHandlerExceptionResolver() {
        return new RestHandlerExceptionResolver(this.properties);
    }

    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        Map<String, RestHandlerExceptionResolver> rest = getApplicationContext().getBeansOfType(RestHandlerExceptionResolver.class);
        exceptionResolvers.addAll(rest.values());
    }

    @Bean
    public XmlMapper xmlMapper(@Autowired SimpleBeanPropertyFilter propertyFilter) {

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

    @Bean
    public SimpleBeanPropertyFilter simpleBeanPropertyFilter(HttpServletRequest request) {
        SerializableFilter filter = new SerializableFilter();
        filter.setRequest(request);
        filter.setField(this.properties.getFilterParameterName());
        filter.setFilterType(this.properties.getFilterTypeParameterName());
        return filter;
    }

    @Bean
    @Primary
    public JsonMapper jsonMapper(@Autowired SimpleBeanPropertyFilter propertyFilter) {
        JsonMapper mapper = JsonMapper.builder().build();
        if (properties.isIndentOutput()) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter(SerializableFilter.FILTER_ID, propertyFilter);
        mapper.setFilterProvider(filterProvider);
        mapper.addMixIn(Object.class, PropertyFilterMixin.class);
        return mapper;
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        // 配置json 用于自定义的转换器 不包括application/json
        MappingJackson2HttpMessageConverter jsonConverter;
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        builder.applicationContext(this.getApplicationContext());
        JsonMapper jsonMapper = this.getApplicationContext().getBean("jsonMapper", JsonMapper.class);
        builder.configure(jsonMapper);
        Map<String, MappingJackson2HttpMessageConverter> jsonMap = this.getApplicationContext().getBeansOfType(MappingJackson2HttpMessageConverter.class);
        if (jsonMap == null || jsonMap.isEmpty()) {
            jsonConverter = new MappingJackson2HttpMessageConverter(jsonMapper);
        } else {
            jsonConverter = jsonMap.values().iterator().next();
            jsonConverter.setObjectMapper(jsonMapper);
        }

        // 配置xml 用于自定义的 类型 不包括 application/xml
        MappingJackson2XmlHttpMessageConverter xmlConverter;
        builder = Jackson2ObjectMapperBuilder.xml();
        builder.applicationContext(this.getApplicationContext());
        XmlMapper xmlMapper = this.getApplicationContext().getBean("xmlMapper", XmlMapper.class);
        builder.configure(xmlMapper);
        Map<String, MappingJackson2XmlHttpMessageConverter> xmlMap = this.getApplicationContext().getBeansOfType(MappingJackson2XmlHttpMessageConverter.class);
        if (xmlMap == null || xmlMap.isEmpty()) {
            xmlConverter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);
        } else {
            xmlConverter = xmlMap.values().iterator().next();
            xmlConverter.setObjectMapper(xmlMapper);
        }

        // 开启自定义mime时
        if (!ObjectUtils.isEmpty(properties.getMimeSubtype())) {
            jsonConverter.setSupportedMediaTypes(asList(JSON_TYPE));
            converters.add(jsonConverter);
            xmlConverter.setSupportedMediaTypes(asList(XML_TYPE));
            converters.add(xmlConverter);
        }

        // 配置String
        StringHttpMessageConverter httpMessage;
        Map<String, StringHttpMessageConverter> stringMap = this.getApplicationContext().getBeansOfType(StringHttpMessageConverter.class);
        if (stringMap == null || stringMap.isEmpty()) {
            httpMessage = new StringHttpMessageConverter();
        } else {
            httpMessage = stringMap.values().iterator().next();
        }
        converters.add(httpMessage);
        // 用于转换 application/json
        converters.add(new MappingJackson2HttpMessageConverter(jsonMapper));
        // 用于转换 application/xml
        converters.add(new MappingJackson2XmlHttpMessageConverter(xmlMapper));

        // 其他乱七八糟的
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new ResourceRegionHttpMessageConverter());
        converters.add(new AllEncompassingFormHttpMessageConverter());

    }

    @Bean
    @ConditionalOnMissingBean(InternalResourceViewResolver.class)
    public InternalResourceViewResolver internalResourceViewResolver() {
        return new InternalResourceViewResolver();
    }

    @Bean
    @Primary
    public RestViewResolver restViewResolver(
            @Autowired InternalResourceViewResolver internalResourceViewResolver,
            @Autowired List<View> views,
            @Autowired MappingJackson2JsonView view,
            @Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager
    ) {
        RestViewResolver tmp = new RestViewResolver(internalResourceViewResolver, views, view);
        tmp.setDefaultViews(views);
        tmp.setContentNegotiationManager(contentNegotiationManager);
        tmp.setViewResolvers(Collections.singletonList(internalResourceViewResolver));
        tmp.setOrder(-1);
        return tmp;
    }

    public RestWebAutoConfiguration(RestWebProperties properties) {
        this.properties = properties;
        if (!ObjectUtils.isEmpty(properties.getMimeSubtype())) {
            JSON_TYPE = createMediaType("*" + "+json");
            XML_TYPE = createMediaType("*" + "+xml");
        }
    }


    @Bean
    @Override
    public ContentNegotiationManager mvcContentNegotiationManager() {
        ContentNegotiationManager manager = super.mvcContentNegotiationManager();
        List<ContentNegotiationStrategy> strategies = manager.getStrategies();
        ListIterator<ContentNegotiationStrategy> iterator = strategies.listIterator();
        while (iterator.hasNext()) {
            ContentNegotiationStrategy strategy = iterator.next();
            if (strategy instanceof PathExtensionContentNegotiationStrategy) {
                RestPathExtensionContentNegotiationStrategy tmp =
                        new RestPathExtensionContentNegotiationStrategy((PathExtensionContentNegotiationStrategy) strategy);
                if (!ObjectUtils.isEmpty(properties.getMimeSubtype())) {
                    tmp.supportMediaType("json", APPLICATION_JSON);
                    tmp.supportMediaType("xml", APPLICATION_XML);
                }
                iterator.set(tmp);

            }
        }
        return manager;
    }

    @Bean
    @ConditionalOnMissingBean
    public NoContentResponseBodyAdvice noContentResponseBodyAdvice() {
        return new NoContentResponseBodyAdvice(this.properties);
    }
}
