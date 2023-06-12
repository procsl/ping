package cn.procsl.ping.boot.web;

import cn.procsl.ping.boot.web.component.AccessLoggerFilter;
import cn.procsl.ping.boot.web.component.SpringContextHolder;
import cn.procsl.ping.boot.web.encrypt.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 */
@Slf4j
@ConditionalOnMissingBean(RestWebAutoConfiguration.class)
@ComponentScan(basePackages = "cn.procsl.ping.boot.web")
public class RestWebAutoConfiguration implements WebMvcConfigurer {

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
        return new EncryptAndDecryptService(new EncryptKeyService() {
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
    public Object modelResolver(ObjectMapper objectMapper) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> resolver = Class.forName(MODEL_RESOLVER);
        Constructor<?> constructor = resolver.getConstructor(ObjectMapper.class);
        return constructor.newInstance(objectMapper);
    }

}
