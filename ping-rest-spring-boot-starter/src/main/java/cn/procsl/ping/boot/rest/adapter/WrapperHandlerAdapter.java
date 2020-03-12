package cn.procsl.ping.boot.rest.adapter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author procsl
 * @date 2020/03/08
 */
@RequiredArgsConstructor
public class WrapperHandlerAdapter implements BeanFactoryAware,
        InitializingBean,
        HandlerAdapter,
        Ordered,
        ServletContextAware,
        ApplicationContextAware {

    @Getter
    final RestRequestMappingHandlerAdapter adapter;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        adapter.setBeanFactory(beanFactory);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        adapter.afterPropertiesSet();
    }


    @Override
    public int getOrder() {
        return adapter.getOrder();
    }

    @Override
    public boolean supports(Object handler) {
        return adapter.supports(handler);
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return adapter.handle(request, response, handler);
    }

    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return adapter.getLastModified(request, handler);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        adapter.setApplicationContext(applicationContext);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        adapter.setServletContext(servletContext);
    }
}
