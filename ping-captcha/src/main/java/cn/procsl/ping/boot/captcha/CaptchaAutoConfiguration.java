package cn.procsl.ping.boot.captcha;

import cn.procsl.ping.boot.captcha.adapter.DefaultEmailSenderAdapter;
import cn.procsl.ping.boot.captcha.adapter.EmailSenderAdapter;
import cn.procsl.ping.boot.captcha.handler.VerifyCaptchaHandlerStrategy;
import cn.procsl.ping.boot.captcha.interceptor.VerifyCaptchaInterceptor;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ComponentScan(basePackages = {
        "cn.procsl.ping.boot.captcha.web",
        "cn.procsl.ping.boot.captcha.handler",
        "cn.procsl.ping.boot.captcha.adapter"
})
@EntityScan(basePackages = "cn.procsl.ping.boot.captcha.domain")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.captcha.domain", bootstrapMode = BootstrapMode.LAZY)
public class CaptchaAutoConfiguration implements WebMvcConfigurer, ApplicationContextAware {

    protected ApplicationContext context;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        VerifyCaptchaHandlerStrategy service = this.context.getBean(VerifyCaptchaHandlerStrategy.class);
        registry.addInterceptor(new VerifyCaptchaInterceptor(service)).addPathPatterns("/**");
    }

    @Bean
    @ConditionalOnMissingBean
    public EmailSenderAdapter emailSenderAdapter() {
        return new DefaultEmailSenderAdapter();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        this.context = context;
    }
}
