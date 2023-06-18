package cn.procsl.ping.boot.jpa;

import cn.procsl.ping.boot.jpa.support.extension.JpaRepositoryFactoryCustomizer;
import cn.procsl.ping.boot.jpa.util.ContextHolder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Slf4j
//@AutoConfiguration(before = {JpaBaseConfiguration.class})
@ConditionalOnMissingBean(JpaDataAutoConfiguration.class)
//@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.jpa.domain", bootstrapMode = BootstrapMode.LAZY)
@EntityScan(basePackages = "cn.procsl.ping.boot.jpa.domain")
public class JpaDataAutoConfiguration implements ApplicationContextAware {


    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        ContextHolder.setApplicationContext(applicationContext);
    }

    @Bean
    public JpaRepositoryFactoryCustomizer jpaExtensionRepositoryFactoryCustomizer() {
        return new JpaRepositoryFactoryCustomizer();
    }

}
