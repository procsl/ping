package cn.procsl.ping.boot.user.config;

import cn.procsl.ping.boot.user.service.UserLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@RequiredArgsConstructor
public class UserAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UserLoginService userLoginService() {
        return new UserLoginService();
    }

}
