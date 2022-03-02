package cn.procsl.ping.boot.domain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Configuration
@ConditionalOnMissingBean(DomainAutoConfiguration.class)
@EnableConfigurationProperties({HibernateProperties.class})
@AutoConfigureBefore(JpaBaseConfiguration.class)
@Slf4j
public class DomainAutoConfiguration {


}
