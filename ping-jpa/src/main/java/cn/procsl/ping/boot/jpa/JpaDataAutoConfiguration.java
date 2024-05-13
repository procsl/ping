package cn.procsl.ping.boot.jpa;

import cn.procsl.ping.boot.jpa.domain.id.IdentifierSegmentRepository;
import cn.procsl.ping.boot.jpa.domain.id.TableIdentifierGenerator;
import cn.procsl.ping.boot.jpa.support.IdentifierGenerator;
import cn.procsl.ping.boot.jpa.support.extension.EnableJpaExtensionRepositories;
import cn.procsl.ping.boot.jpa.support.extension.JpaRepositoryFactoryCustomizer;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Slf4j
@AutoConfiguration
@ConditionalOnMissingBean(JpaDataAutoConfiguration.class)
@EnableJpaExtensionRepositories(basePackages = "cn.procsl.ping.boot.jpa.domain")
@EntityScan(basePackages = "cn.procsl.ping.boot.jpa.domain")
public class JpaDataAutoConfiguration implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        return JpaRepositoryFactoryCustomizer.postProcessBeforeInitialization(bean, beanName);
    }

    @Bean
    public JpaRepositoryFactoryCustomizer JpaExtendRepositoryFactoryCustomizer() {
        return JpaRepositoryFactoryCustomizer.instance;
    }


    @Bean
    public IdentifierGenerator<Long> identifierGenerator(IdentifierSegmentRepository repository,
                                                         PlatformTransactionManager transactionManager) {
        return new TableIdentifierGenerator(repository, transactionManager, 10, 3);
    }


}
