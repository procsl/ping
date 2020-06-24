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

    public static final String RBAC_REPOSITORY_PATH = "cn.procsl.ping.boot.user.rbac.repository";

    public static final String RBAC_COMPONENT_SCAN = "cn.procsl.ping.boot.user.domain.rbac.service";

    public static final String RBAC_ENTITY = "cn.procsl.ping.boot.user.domain.rbac.entity";

    boolean enableInitDefaultData = true;
}
