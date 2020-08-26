package cn.procsl.ping.boot.domain.config;

import cn.procsl.ping.boot.domain.business.dictionary.model.DictPath;
import cn.procsl.ping.boot.domain.business.dictionary.model.Dictionary;
import cn.procsl.ping.boot.domain.business.dictionary.repository.CustomDictionaryRepository;
import cn.procsl.ping.boot.domain.business.dictionary.repository.CustomDictionaryRepositoryImpl;
import cn.procsl.ping.boot.domain.business.dictionary.service.DictionaryService;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.naming.LowCasePhysicalNamingStrategy;
import cn.procsl.ping.boot.domain.naming.NameImplicitNamingStrategy;
import cn.procsl.ping.boot.domain.support.executor.DomainRepositoryFactoryBean;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.config.BootstrapMode;

import javax.persistence.EntityManager;

/**
 * 自动配置 用于注册加载时依赖注入和包扫描
 *
 * @author procsl
 * @date 2020/03/21
 */
@Configuration
@ConditionalOnMissingBean(DomainAutoConfiguration.class)
@EnableConfigurationProperties({DomainProperties.class, HibernateProperties.class})
@AutoConfigureBefore(JpaBaseConfiguration.class)
@Slf4j
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
    "cn.procsl.ping.boot.domain.business.dictionary.repository",
},
    repositoryFactoryBeanClass = DomainRepositoryFactoryBean.class,
    bootstrapMode = BootstrapMode.LAZY
)
@EntityScan(basePackages = {
    "cn.procsl.ping.boot.domain.business.dictionary.model"
})
public class DomainAutoConfiguration {

    final DomainProperties dataProperties;

    final HibernateProperties hibernateProperties;

    @Bean
    @ConditionalOnMissingBean
    public NameImplicitNamingStrategy nameImplicitNamingStrategy() {
        return new NameImplicitNamingStrategy(dataProperties);
    }


    @Bean
    @ConditionalOnMissingBean
    public LowCasePhysicalNamingStrategy lowCasePhysicalNamingStrategy() {
        return new LowCasePhysicalNamingStrategy(dataProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DictionaryService dictionaryService(
        @Autowired AdjacencyTreeRepository<Dictionary, Long, DictPath> dataDictionaryRepository,
        @Autowired JpaRepository<Dictionary, Long> jpaRepository,
        @Autowired QuerydslPredicateExecutor<Dictionary> querydslPredicateExecutor,
        @Autowired CustomDictionaryRepository customDictionaryRepositoryImpl
    ) {
        return new DictionaryService(dataDictionaryRepository,
            jpaRepository,
            querydslPredicateExecutor,
            customDictionaryRepositoryImpl
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomDictionaryRepository customDictionaryRepositoryImpl(@Autowired JPAQueryFactory jpaQueryFactory) {
        return new CustomDictionaryRepositoryImpl(jpaQueryFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public JPAQueryFactory jpaQueryFactory(@Autowired EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }


    public DomainAutoConfiguration(DomainProperties dataProperties, HibernateProperties hibernateProperties) {
        this.dataProperties = dataProperties;
        this.hibernateProperties = hibernateProperties;
        String phy = hibernateProperties.getNaming().getImplicitStrategy();

        if (phy == null || phy.isEmpty()) {
            String name = LowCasePhysicalNamingStrategy.class.getName();
            log.info("spring.jpa.hibernate.naming.physical-strategy:{}", name);
            hibernateProperties.getNaming().setPhysicalStrategy(name);
        }

        String imp = hibernateProperties.getNaming().getImplicitStrategy();
        if (imp == null || imp.isEmpty()) {
            String name = NameImplicitNamingStrategy.class.getName();
            log.info("spring.jpa.hibernate.naming.implicit-strategy:{}", name);
            hibernateProperties.getNaming().setImplicitStrategy(name);
        }

        Dictionary.setDelimit(this::get);

    }

    protected String get() {
        return this.dataProperties.getDirectoryDelimiter();
    }
}
