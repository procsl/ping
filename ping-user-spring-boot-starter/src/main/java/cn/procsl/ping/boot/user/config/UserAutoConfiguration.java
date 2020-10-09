package cn.procsl.ping.boot.user.config;

import cn.procsl.ping.boot.domain.business.state.repository.BooleanStatefulRepository;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.config.DomainProperties;
import cn.procsl.ping.boot.domain.support.executor.DomainRepositoryFactoryBean;
import cn.procsl.ping.boot.user.domain.dictionary.model.DictPath;
import cn.procsl.ping.boot.user.domain.dictionary.model.Dictionary;
import cn.procsl.ping.boot.user.domain.dictionary.repository.CustomDictionaryRepository;
import cn.procsl.ping.boot.user.domain.dictionary.repository.CustomDictionaryRepositoryImpl;
import cn.procsl.ping.boot.user.domain.dictionary.service.DictionaryService;
import cn.procsl.ping.boot.user.domain.rbac.model.Node;
import cn.procsl.ping.boot.user.domain.rbac.model.Permission;
import cn.procsl.ping.boot.user.domain.rbac.model.Role;
import cn.procsl.ping.boot.user.domain.rbac.model.Subject;
import cn.procsl.ping.boot.user.domain.rbac.service.PermissionService;
import cn.procsl.ping.boot.user.domain.rbac.service.RoleService;
import cn.procsl.ping.boot.user.domain.rbac.service.SubjectService;
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
@EnableConfigurationProperties({UserProperties.class, DomainProperties.class})
@ConditionalOnMissingBean({UserAutoConfiguration.class})
@EnableJpaRepositories(basePackages = {
    "cn.procsl.ping.boot.user.domain.dictionary.repository",
    "cn.procsl.ping.boot.user.domain.rbac.repository",
},
    repositoryFactoryBeanClass = DomainRepositoryFactoryBean.class,
    bootstrapMode = BootstrapMode.LAZY
)
@EntityScan(basePackages = {
    "cn.procsl.ping.boot.user.domain.dictionary.model",
    "cn.procsl.ping.boot.user.domain.rbac.model"
})
@RequiredArgsConstructor
public class UserAutoConfiguration {

    final UserProperties properties;

    final DomainProperties domainProperties;


    @Bean
    @ConditionalOnMissingBean
    public DictionaryService dictionaryService(
        @Autowired AdjacencyTreeRepository<Dictionary, Long, DictPath> repository,
        @Autowired JpaRepository<Dictionary, Long> jpaRepository,
        @Autowired QuerydslPredicateExecutor<Dictionary> querydslPredicateExecutor,
        @Autowired CustomDictionaryRepository customDictionaryRepositoryImpl,
        @Autowired BooleanStatefulRepository<Dictionary, Long> booleanStatefulRepository
    ) {
        return new DictionaryService(
            jpaRepository,
            querydslPredicateExecutor,
            repository,
            customDictionaryRepositoryImpl,
            booleanStatefulRepository
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public JPAQueryFactory jpaQueryFactory(@Autowired EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomDictionaryRepository customDictionaryRepositoryImpl(
        @Autowired AdjacencyTreeRepository<Dictionary, Long, DictPath> adjacencyTreeRepository
    ) {
        return new CustomDictionaryRepositoryImpl(adjacencyTreeRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionService permissionService(
        JpaRepository<Permission, Long> jpaRepository,
        QuerydslPredicateExecutor<Permission> querydslRepository,
        EntityManager entityManager
    ) {
        return new PermissionService(jpaRepository, querydslRepository, entityManager);
    }


    @Bean
    @ConditionalOnMissingBean
    public RoleService RoleService(AdjacencyTreeRepository<Role, Long, Node> currentTreeRepository,
                                   QuerydslPredicateExecutor<Role> querydslRepository,
                                   PermissionService permissionService,
                                   JpaRepository<Role, Long> jpaRepository,
                                   BooleanStatefulRepository<Role, Long> booleanStatefulRepository
    ) {
        return new RoleService(currentTreeRepository,
            querydslRepository,
            permissionService,
            jpaRepository,
            booleanStatefulRepository);
    }


    @Bean
    @ConditionalOnMissingBean
    public SubjectService subjectService(JpaRepository<Subject, Long> jpaRepository,
                                         QuerydslPredicateExecutor<Subject> querydslRepository,
                                         RoleService roleService) {
        return new SubjectService(jpaRepository, querydslRepository, roleService);
    }

}
