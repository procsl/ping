package cn.procsl.ping.boot.rest.mapping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.Ordered;
import org.springframework.util.StringValueResolver;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.MatchableHandlerMapping;
import org.springframework.web.servlet.handler.RequestMatchResult;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author procsl
 * @date 2020/03/08
 */
@RequiredArgsConstructor
public class WrapperHandlerMapping implements MatchableHandlerMapping,
    EmbeddedValueResolverAware,
    InitializingBean,
    HandlerMapping,
    Ordered,
    BeanNameAware,
    ApplicationContextAware,
    ServletContextAware {

    @Getter
    final RestRequestMappingHandlerMapping handlerMapping;

    @Override
    public void setBeanName(String name) {
        handlerMapping.setBeanName(name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        handlerMapping.afterPropertiesSet();
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        handlerMapping.setEmbeddedValueResolver(resolver);
    }

    @Override
    public int getOrder() {
        return handlerMapping.getOrder();
    }

    @Override
    public RequestMatchResult match(HttpServletRequest request, String pattern) {
        return handlerMapping.match(request, pattern);
    }

    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        return this.handlerMapping.getHandler(request);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.handlerMapping.setServletContext(servletContext);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.handlerMapping.setApplicationContext(applicationContext);
    }
}
