package cn.procsl.ping.boot.web;

import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import cn.procsl.ping.boot.web.cipher.SimpleCipherLockupService;
import cn.procsl.ping.boot.web.cipher.filter.CipherFilter;
import cn.procsl.ping.boot.web.cipher.id.CipherSecurityBuilder;
import cn.procsl.ping.boot.web.component.CommonErrorAttributes;
import cn.procsl.ping.boot.web.component.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.reflect.Constructor;
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

    @Bean("cipherFilter")
    public FilterRegistrationBean<CipherFilter> accessLoggerFilterFilterRegistrationBean(@Autowired CipherLockupService lockupService){
        FilterRegistrationBean<CipherFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new CipherFilter(lockupService));
        filter.setName("cipherFilter");
        filter.setOrder(Integer.MIN_VALUE + 1);
        filter.setUrlPatterns(List.of("/*"));
        return filter;
    }

    @Bean
    @ConditionalOnBean(name = "cipherFilter", value = FilterRegistrationBean.class)
    public ServletListenerRegistrationBean<ServletRequestListener> cipherCleanRequestListener(@Autowired FilterRegistrationBean<CipherFilter> cipherFilter) {
        ServletListenerRegistrationBean<ServletRequestListener> listener = new ServletListenerRegistrationBean<>();
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
    @SneakyThrows
    public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (bean instanceof ObjectMapper mapper) {
            CipherLockupService server = this.applicationContext.getBean(CipherLockupService.class);
            mapper.setAnnotationIntrospector(CipherSecurityBuilder.buildJacsonIntrospector(server));
        }

        if (beanName.equals("mvcConversionService") && bean instanceof WebConversionService conversionService) {
            return CipherSecurityBuilder.hookMvcConversionService(conversionService);
        }

        return bean;
    }


}
