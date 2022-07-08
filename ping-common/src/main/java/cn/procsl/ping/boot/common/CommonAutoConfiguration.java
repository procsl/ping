package cn.procsl.ping.boot.common;

import cn.procsl.ping.boot.common.utils.ContextHolder;
import cn.procsl.ping.boot.common.validator.UniqueValidator;
import cn.procsl.ping.boot.common.validator.UniqueValidatorImpl;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

import javax.persistence.EntityManager;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(CommonAutoConfiguration.class)
@AutoConfigureBefore(JpaBaseConfiguration.class)
@EnableJpaRepositories(bootstrapMode = BootstrapMode.LAZY, basePackages = "cn.procsl.ping.boot.domain")
@EntityScan(basePackages = "cn.procsl.ping.boot.domain")
public class CommonAutoConfiguration implements ApplicationContextAware {

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        ContextHolder.setApplicationContext(applicationContext);
    }

    @Bean
    public UniqueValidator uniqueValidation(EntityManager entityManager) {
        return new UniqueValidatorImpl(entityManager);
    }

}
