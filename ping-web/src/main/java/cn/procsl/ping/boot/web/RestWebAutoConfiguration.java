package cn.procsl.ping.boot.web;

import cn.procsl.ping.boot.web.component.AccessLoggerFilter;
import cn.procsl.ping.boot.web.component.CommonErrorAttributes;
import cn.procsl.ping.boot.web.component.GlobalExceptionHandler;
import cn.procsl.ping.boot.web.component.SpringContextHolder;
import cn.procsl.ping.boot.web.cipher.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 */
@Slf4j
@AutoConfiguration(before = ErrorMvcAutoConfiguration.class)
@ConditionalOnMissingBean(RestWebAutoConfiguration.class)
@ComponentScan(basePackages = "cn.procsl.ping.boot.web")
public class RestWebAutoConfiguration implements WebMvcConfigurer, BeanPostProcessor {

    final ApplicationContext applicationContext;
    final JacksonSecurityIdAnnotationIntrospector introspector = new JacksonSecurityIdAnnotationIntrospector();


    public RestWebAutoConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        SpringContextHolder.setContext(applicationContext);
        introspector.setApplicationContext(applicationContext);
    }

    @Bean("accessLoggerFilterBean")
    @ConditionalOnMissingBean(name = "accessLoggerFilterBean")
    public FilterRegistrationBean<AccessLoggerFilter> accessLoggerFilterFilterRegistrationBean() {
        FilterRegistrationBean<AccessLoggerFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new AccessLoggerFilter());
        filter.setName("accessLoggerFilter");
        filter.setOrder(Integer.MIN_VALUE);
        filter.setUrlPatterns(List.of("/*"));
        return filter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        CipherSecurityService server = this.applicationContext.getBean(CipherSecurityService.class);
        registry.addConverter(new CipherGenericConverter(server));
    }


    final static String MODEL_RESOLVER = "io.swagger.v3.core.jackson.ModelResolver";

    @Bean
    @ConditionalOnClass(name = MODEL_RESOLVER)
    @ConditionalOnMissingBean(type = MODEL_RESOLVER)
    @SneakyThrows
    public Object modelResolver(ObjectMapper objectMapper) {
        Class<?> resolver = Class.forName(MODEL_RESOLVER);
        Constructor<?> constructor = resolver.getConstructor(ObjectMapper.class);
        return constructor.newInstance(objectMapper);
    }

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
    @ConditionalOnMissingBean(value = CipherSecurityService.class)
    public CipherSecurityService cipherSecurityService() {
        return new SimplerCipherSecurityService();
    }

    @Override
    @SneakyThrows
    public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (bean instanceof ObjectMapper mapper) {
            mapper.setAnnotationIntrospector(introspector);
        }

        if (beanName.equals("mvcConversionService") && bean instanceof WebConversionService conversionService) {
            log.info("hook WebConversionService");
            var wrapper = new CipherGenericConverter.ErrorProcessWevConversionService();

            var targetStart = wrapper.getClass().getSuperclass();
            do {
                copyProperties(conversionService, wrapper, targetStart);
                targetStart = targetStart.getSuperclass();
            } while (targetStart != null);

            return wrapper;
        }

        return bean;
    }

    protected void copyProperties(Object source, Object target, Class<?> clazz) throws IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        for (Field sourceField : fields) {

            if (Modifier.isStatic(sourceField.getModifiers())) {
                log.debug("跳过static属性: {}", sourceField.getName());
                continue;
            }

            boolean able = sourceField.trySetAccessible();
            if (!able) {
                sourceField.setAccessible(true);
            }
            Object value = sourceField.get(source);
            sourceField.set(target, value);
            if (!able) {
                sourceField.setAccessible(false);
            }
        }
    }


}
