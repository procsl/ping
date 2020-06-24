package cn.procsl.ping.boot.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author procsl
 * @date 2020/04/10
 */
@ConfigurationProperties(prefix = "ping.business.user")
@Getter
@Setter
public class UserProperties {

    boolean enableInitDefaultData = true;
}
