package cn.procsl.ping.boot.user.config;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.support.executor.DomainRepositoryFactoryBean;
import cn.procsl.ping.boot.user.domain.dictionary.model.DictPath;
import cn.procsl.ping.boot.user.domain.dictionary.model.Dictionary;
import cn.procsl.ping.boot.user.domain.dictionary.repository.CustomDictionaryRepository;
import cn.procsl.ping.boot.user.domain.dictionary.repository.CustomDictionaryRepositoryImpl;
import cn.procsl.ping.boot.user.domain.dictionary.service.DictionaryService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.config.BootstrapMode;

import javax.persistence.EntityManager;


/**
 * 用户模块自动配置项
 *
 * @author procsl
 * @date 2020/04/10
 */
@Configuration
@EnableConfigurationProperties({UserProperties.class})
@RequiredArgsConstructor
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@EnableJpaRepositories(basePackages = {
    "cn.procsl.ping.boot.user.domain.dictionary.repository",
    "cn.procsl.ping.boot.user.domain.dictionary.model.repository",
},
    repositoryFactoryBeanClass = DomainRepositoryFactoryBean.class,
    bootstrapMode = BootstrapMode.LAZY
)
@EntityScan(basePackages = {
    "cn.procsl.ping.boot.user.domain.dictionary",
})

public class UserAutoConfiguration {

    final UserProperties properties;


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
    public JPAQueryFactory jpaQueryFactory(@Autowired EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomDictionaryRepository customDictionaryRepositoryImpl(@Autowired JPAQueryFactory jpaQueryFactory) {
        return new CustomDictionaryRepositoryImpl(jpaQueryFactory);
    }
}
