package cn.procsl.ping.boot.rbac.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author procsl
 * @date 2020/04/10
 */
@ConfigurationProperties(prefix = "ping.business.rbac")
@Getter
@Setter
public class RbacProperties {

    boolean enableInitDefaultData = true;
}
