package cn.procsl.ping.app.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Getter
@Setter
@Configuration(proxyBeanMethods = false)
@Slf4j
public class LoginConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry
            //允许访问的接口地址
            .addMapping("/**")
            //允许发起跨域访问的域名
            .allowedOrigins("http://localhost:10000")
            //允许跨域访问的方法
            .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
            //是否带上cookie信息
            .allowCredentials(true)
            .maxAge(3600)
            .allowedHeaders("*");
    }
}
