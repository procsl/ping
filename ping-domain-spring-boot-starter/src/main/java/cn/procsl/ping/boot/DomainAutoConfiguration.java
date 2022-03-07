package cn.procsl.ping.boot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Slf4j
@Configuration
@ConditionalOnMissingBean(DomainAutoConfiguration.class)
@AutoConfigureBefore(JpaBaseConfiguration.class)
public class DomainAutoConfiguration {


}
