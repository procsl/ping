package cn.procsl.ping.boot.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@RequiredArgsConstructor
public class UserAutoConfiguration {


}
