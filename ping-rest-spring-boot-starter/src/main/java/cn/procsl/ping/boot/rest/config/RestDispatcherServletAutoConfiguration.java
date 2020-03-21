package cn.procsl.ping.boot.rest.config;

import cn.procsl.ping.boot.rest.web.RestDispatcherServlet;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author procsl
 * @date 2020/02/19
 */
@Configuration
@AutoConfigureBefore({DispatcherServletAutoConfiguration.class})
public class RestDispatcherServletAutoConfiguration {

    private final WebMvcProperties mvcProperties;

    public RestDispatcherServletAutoConfiguration(WebMvcProperties mvcProperties) {
        this.mvcProperties = mvcProperties;
        if ("/**".equals(this.mvcProperties.getStaticPathPattern())) {
            this.mvcProperties.setStaticPathPattern("/static/**");
        }
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet dispatcherServlet;
        dispatcherServlet = new RestDispatcherServlet();
        return dispatcherServlet;
    }

}
