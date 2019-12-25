package cn.procsl.business.impl.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author procsl
 * @date 2019/12/15
 */
@Component
@Setter
@Getter
public class UserConfig {

    /**
     * 用户账户前缀
     */
    @Value("${ping.business.config.user.defaultAccountPrefix:PA_}")
    private String defaultAccountPrefix;

    /**
     * 超级用户账户名
     */
    @Value("${ping.business.config.user.systemAccount:root}")
    private String systemAccount;

    /**
     * 系统用户账户密码
     */
    @Value("${ping.business.config.user.systemAccountPassword:123456}")
    private String systemAccountPassword;
}
