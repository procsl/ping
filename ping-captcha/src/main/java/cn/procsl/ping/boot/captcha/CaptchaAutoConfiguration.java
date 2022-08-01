package cn.procsl.ping.boot.captcha;

import cn.procsl.ping.boot.captcha.interceptor.VerifyCaptchaHandler;
import cn.procsl.ping.boot.captcha.service.VerifyCaptchaService;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ComponentScan(basePackages = "cn.procsl.ping.boot.captcha")
@EntityScan(basePackages = "cn.procsl.ping.boot.captcha.domain")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.captcha.domain", bootstrapMode = BootstrapMode.LAZY)
public class CaptchaAutoConfiguration implements WebMvcConfigurer, ApplicationContextAware {

    protected ApplicationContext context;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        VerifyCaptchaService service = this.context.getBean(VerifyCaptchaService.class);
        registry.addInterceptor(new VerifyCaptchaHandler(service))
                .addPathPatterns("/v1/**");
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
