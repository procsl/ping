package cn.procsl.ping.boot.jpa;

import cn.procsl.ping.boot.jpa.domain.id.IdentifierSegmentRepository;
import cn.procsl.ping.boot.jpa.domain.id.SegmentIdentifierGenerator;
import cn.procsl.ping.boot.jpa.domain.id.TableIdentifierSegmentRepositoryImpl;
import cn.procsl.ping.boot.jpa.support.extension.EnableJpaExtensionRepositories;
import cn.procsl.ping.boot.jpa.support.extension.JpaRepositoryFactoryCustomizer;
import cn.procsl.ping.boot.jpa.support.query.ProjectionSearchRepository;
import cn.procsl.ping.boot.jpa.support.query.ast.jpa.JpaProjectionSearchRepository;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * &#064;date  2020/03/21
 */
@Slf4j
@AutoConfiguration
@ConditionalOnMissingBean(JpaDataAutoConfiguration.class)
@EnableJpaExtensionRepositories(basePackages = {"cn.procsl.ping.boot.jpa.domain"})
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

    @Bean("defaultJpaProjectionSearchRepository")
    @ConditionalOnMissingBean
    public ProjectionSearchRepository jpaProjectionSearchRepository(EntityManager entityManager) {
        return new JpaProjectionSearchRepository(entityManager);
    }

    @ConditionalOnMissingBean
    @Bean(name = "defaultIdentifierSegmentRepository")
    public IdentifierSegmentRepository identifierSegmentRepository(EntityManager entityManager,
                                                                   PlatformTransactionManager manager) {
        return new TableIdentifierSegmentRepositoryImpl(entityManager, manager);
    }

    @ConditionalOnMissingBean
    @Bean(name = "defaultSegmentIdentifierGenerator")
    public SegmentIdentifierGenerator segmentIdentifierGenerator(@Qualifier("defaultIdentifierSegmentRepository")
                                                                 IdentifierSegmentRepository repo) {
        return SegmentIdentifierGenerator.builder().segmentSize(200)
            .retryTimes(5).initValue(1L).repository(repo).build();
    }


}
