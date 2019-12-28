package cn.procsl.business.user.web.components.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Locale;

/**
 * @author procsl
 * @date 2019/12/26
 */
@Slf4j
public class JsonResolver implements ViewResolver, Ordered, InitializingBean, DisposableBean {

    @Autowired
    MappingJackson2JsonView json;

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        return json;
    }

    @Override
    public void destroy() throws Exception {
        log.info("销毁:{}", this.getClass().getName());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("初始化:{}", this.getClass().getName());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
