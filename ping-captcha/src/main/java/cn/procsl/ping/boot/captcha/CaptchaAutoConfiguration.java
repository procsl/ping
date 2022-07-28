package cn.procsl.ping.boot.captcha;

import cn.procsl.ping.boot.captcha.interceptor.VerifyCaptchaHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ComponentScan(basePackages = "cn.procsl.ping.boot.captcha")
public class CaptchaAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new VerifyCaptchaHandler());
    }
}
