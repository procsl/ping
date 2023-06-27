package cn.procsl.ping.boot.web;

import cn.procsl.ping.boot.web.component.AccessLoggerFilter;
import cn.procsl.ping.boot.web.component.CommonErrorAttributes;
import cn.procsl.ping.boot.web.component.GlobalExceptionHandler;
import cn.procsl.ping.boot.web.component.SpringContextHolder;
import cn.procsl.ping.boot.web.encrypt.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Provider;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    public RestWebAutoConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        SpringContextHolder.setContext(applicationContext);
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
        EncryptDecryptService server = this.applicationContext.getBean(EncryptDecryptService.class);
        registry.addConverter(new DecryptConversionService(server));
    }


    @Bean
    @ConditionalOnMissingBean
    public EncryptDecryptService encryptDecryptService() {
        return new EncryptAndDecryptService(
                new EncryptKeyService() {
                    @Override
                    public String getKey(EncryptContext context) {
                        return "1234567890";
                    }
                }, new EncryptContextService() {
            @Override
            public EncryptContext getContext() {
                return null;
            }
        });
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

    @Override
    public Object postProcessBeforeInitialization(@Nonnull Object bean,
                                                  @Nonnull String beanName) throws BeansException {
        if (bean instanceof JsonComponentModule jsonComponent) {
            jsonComponent.setDeserializers(new CollectionSimpleDeserializers());
        }

        return bean;
    }

}
