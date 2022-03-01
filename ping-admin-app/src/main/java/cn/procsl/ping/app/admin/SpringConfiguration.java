package cn.procsl.ping.app.admin;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Getter
@Setter
@Configuration(proxyBeanMethods = false)
@Slf4j
public class SpringConfiguration implements WebMvcConfigurer {

    String protocol = "http";

    String domain = "localhost";

    @Value("${server.port}")
    String port;


    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {

        corsRegistry
            //允许访问的接口地址
            .addMapping("/**")
            //允许发起跨域访问的域名
            .allowedOrigins(String.format("%s://%s:%s", protocol, domain, port))
            //允许跨域访问的方法
            .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE")
            //是否带上cookie信息
            .allowCredentials(true)
            .maxAge(3600)
            .allowedHeaders("*");
    }
}
