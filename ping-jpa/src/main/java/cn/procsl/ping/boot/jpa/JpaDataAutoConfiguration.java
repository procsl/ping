package cn.procsl.ping.boot.jpa;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Slf4j
@AutoConfiguration(before = {JpaBaseConfiguration.class})
@ConditionalOnMissingBean(JpaDataAutoConfiguration.class)
@EnableJpaRepositories(bootstrapMode = BootstrapMode.LAZY, basePackages = "cn.procsl.ping.boot.jpa")
@EntityScan(basePackages = "cn.procsl.ping.boot.jpa")
public class JpaDataAutoConfiguration implements ApplicationContextAware {

    public final static String SYSTEM_ERROR_CODE_KEY = "SYSTEM_ERROR_CODE_KEY";

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        ContextHolder.setApplicationContext(applicationContext);
    }


}
