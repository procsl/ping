package cn.procsl.ping.boot.domain.annotation;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableJpaRepositories
@EntityScan
public @interface EnableDomainRepositories {

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "basePackages")
    String[] repositories() default {};

    @AliasFor(annotation = EntityScan.class, attribute = "basePackages")
    String[] entities() default {};

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "basePackageClasses")
    Class<?>[] basePackageClasses() default {};

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "includeFilters")
    ComponentScan.Filter[] includeFilters() default {};

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "excludeFilters")
    ComponentScan.Filter[] excludeFilters() default {};

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "repositoryImplementationPostfix")
    String repositoryImplementationPostfix() default "Impl";

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "namedQueriesLocation")
    String namedQueriesLocation() default "";

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "queryLookupStrategy")
    QueryLookupStrategy.Key queryLookupStrategy() default QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;

//    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "repositoryFactoryBeanClass")
//    Class<?> repositoryFactoryBeanClass() default DomainRepositoryFactoryBean.class;

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "repositoryBaseClass")
    Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "entityManagerFactoryRef")
    String entityManagerFactoryRef() default "entityManagerFactory";

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "transactionManagerRef")
    String transactionManagerRef() default "transactionManager";

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "considerNestedRepositories")
    boolean considerNestedRepositories() default false;

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "enableDefaultTransactions")
    boolean enableDefaultTransactions() default true;

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "bootstrapMode")
    BootstrapMode bootstrapMode() default BootstrapMode.LAZY;

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "escapeCharacter")
    char escapeCharacter() default '\\';
}
