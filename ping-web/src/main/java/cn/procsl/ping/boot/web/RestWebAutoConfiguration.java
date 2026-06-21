package cn.procsl.ping.boot.web;

import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import cn.procsl.ping.boot.web.cipher.SimpleCipherLockupService;
import cn.procsl.ping.boot.web.cipher.filter.CipherFilter;
import cn.procsl.ping.boot.web.cipher.id.CipherSecurityBuilder;
import cn.procsl.ping.boot.web.component.CommonErrorAttributes;
import cn.procsl.ping.boot.web.component.GlobalExceptionHandler;
import jakarta.annotation.Nonnull;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletRequestListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;

import javax.inject.Named;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 */
@Slf4j
@AutoConfiguration(before = {ErrorMvcAutoConfiguration.class}, beforeName = "org.springdoc.core.configuration.SpringDocConfiguration")
@ComponentScan(basePackages = "cn.procsl.ping.boot.web")
public class RestWebAutoConfiguration implements WebMvcConfigurer, BeanPostProcessor, ApplicationContextInitializer<ConfigurableApplicationContext> {

    final ApplicationContext applicationContext;

    public final static String[] PUBLIC_STATIC_RESOURCES = new String[]{
        "**.css",
        "**.html",
        "**.js",
        "**.jpeg",
        "**.jpg",
        "**.png",
        "**.gif",
        "**.pdf",
        "**.xlsx",
        "**.xls",
    };


    public RestWebAutoConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.jackson.property-naming-strategy", havingValue = "SNAKE_CASE", matchIfMissing = true)
    @ConditionalOnClass(name = {"io.swagger.v3.core.jackson.ModelResolver",
        "com.fasterxml.jackson.databind.ObjectMapper",
        "org.springdoc.core.properties.SpringDocConfigProperties"})
    @ConditionalOnBean(type = "org.springdoc.core.properties.SpringDocConfigProperties")
    public Object modelResolver(ApplicationContext context) throws Exception {
        log.info("加载解析: spring.jackson.property-naming-strategy Springdoc配置项");
        // 2. 动态加载 SpringDocConfigProperties 实例
        Class<?> propertiesClass = Class.forName("org.springdoc.core.properties.SpringDocConfigProperties");
        Object properties = context.getBean(propertiesClass);
        boolean isOpenapi31 = (boolean) propertiesClass.getMethod("isOpenapi31").invoke(properties);

        // 3. 动态创建 Jackson 2 的 ObjectMapper （完全由字符串驱动，躲过任何编译器的眼睛）
        Class<?> objectMapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
        Object mapper = objectMapperClass.getConstructor().newInstance();

        // 4. 动态获取 Jackson 2 的 SNAKE_CASE 策略
        Class<?> namingStrategiesClass = Class.forName("com.fasterxml.jackson.databind.PropertyNamingStrategies");
        Object snakeCaseStrategy = namingStrategiesClass.getField("SNAKE_CASE").get(null);

        // 5. 将策略注入到 Jackson 2 的 ObjectMapper 中
        Class<?> strategyClass = Class.forName("com.fasterxml.jackson.databind.PropertyNamingStrategy");
        objectMapperClass.getMethod("setPropertyNamingStrategy", strategyClass).invoke(mapper, snakeCaseStrategy);

        // 6. 组装并返回 ModelResolver
        Class<?> resolverClass = Class.forName("io.swagger.v3.core.jackson.ModelResolver");
        Object resolver = resolverClass.getConstructor(objectMapperClass).newInstance(mapper);

        return resolverClass.getMethod("openapi31", boolean.class).invoke(resolver, isOpenapi31);
    }

    @Bean("cipherFilter")
    public FilterRegistrationBean<CipherFilter> accessLoggerFilterFilterRegistrationBean(@Autowired CipherLockupService lockupService) {
        FilterRegistrationBean<CipherFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new CipherFilter(lockupService));
        filter.setName("cipherFilter");
        filter.setOrder(Integer.MIN_VALUE + 1);
        filter.setUrlPatterns(List.of("/v1/*", "/s/*", "/a/*"));
        return filter;
    }

    @Bean
    @ConditionalOnBean(name = "cipherFilter", value = FilterRegistrationBean.class)
    public ServletListenerRegistrationBean<ServletRequestListener> cipherCleanRequestListener(@Autowired FilterRegistrationBean<CipherFilter> cipherFilter) {
        ServletListenerRegistrationBean<ServletRequestListener> listener = new ServletListenerRegistrationBean<>();
        assert cipherFilter.getFilter() != null;
        listener.setListener(cipherFilter.getFilter());
        return listener;
    }


    @Override
    public void addInterceptors(@Nonnull InterceptorRegistry registry) {
        try {
            FilterRegistrationBean<?> bean = this.applicationContext.getBean("cipherFilter", FilterRegistrationBean.class);
            Filter filter = bean.getFilter();
            if (filter instanceof CipherFilter cipherFilter) {
                registry.addInterceptor(cipherFilter).excludePathPatterns(PUBLIC_STATIC_RESOURCES).addPathPatterns("/**");
            }
        } catch (BeanNotOfRequiredTypeException e) {

        }
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        CipherLockupService server = this.applicationContext.getBean(CipherLockupService.class);
        registry.addConverter(CipherSecurityBuilder.buildConverter(server));
    }


    @Bean
    @ConditionalOnMissingBean(name = "cipherLockupService")
    public CipherLockupService cipherLockupService() {
        return new SimpleCipherLockupService();
    }

    final static String MODEL_RESOLVER = "io.swagger.v3.core.jackson.ModelResolver";

//    @Bean
//    @ConditionalOnClass(name = MODEL_RESOLVER)
//    @ConditionalOnMissingBean(type = MODEL_RESOLVER)
//    @SneakyThrows
//    public Object modelResolver(ObjectMapper objectMapper) {
//        Class<?> resolver = Class.forName(MODEL_RESOLVER);
//        Constructor<?> constructor = resolver.getConstructor(ObjectMapper.class);
//        return constructor.newInstance(objectMapper);
//    }

    @Bean
    @ConditionalOnMissingBean(name = "globalExceptionHandler")
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
    public CommonErrorAttributes errorAttributes() {
        return new CommonErrorAttributes();
    }

    @Bean
    public JsonMapperBuilderCustomizer customAnnotationIntrospectorCustomizer(CipherLockupService cipherLockupService) {
        return builder -> builder.annotationIntrospector(CipherSecurityBuilder.buildJacksonIntrospector(cipherLockupService));
    }

    @Override
    @SneakyThrows
    public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {

        if (beanName.equals("mvcConversionService") && bean instanceof WebConversionService conversionService) {
            return CipherSecurityBuilder.hookMvcConversionService(conversionService);
        }

        return bean;
    }


    @Override
    public void initialize(@Nonnull ConfigurableApplicationContext context) {
        if (!(context instanceof AnnotationConfigRegistry reg)) {
            log.warn("未注册Admin Server, Context类错误");
            return;
        }

        try {
            Class<?> clazz = Class.forName("de.codecentric.boot.admin.server.config.AdminServerMarkerConfiguration");
            reg.register(clazz);
        } catch (ClassNotFoundException e) {
            log.debug("未注册Admin Server, 找不到配置类");
        }
    }

}
