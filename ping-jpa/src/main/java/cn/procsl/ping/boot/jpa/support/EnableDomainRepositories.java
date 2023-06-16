package cn.procsl.ping.boot.jpa.support;

import cn.procsl.ping.boot.jpa.support.JpaSpecificationExecutorWithProjectionImpl;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableJpaRepositories(bootstrapMode = BootstrapMode.LAZY, repositoryBaseClass = JpaSpecificationExecutorWithProjectionImpl.class)
public @interface EnableDomainRepositories {


    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "basePackages")
    String[] basePackages() default {};


//    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "repositoryFactoryBeanClass")
//    Class<?> repositoryFactoryBeanClass() default DomainRepositoryFactoryBean.class;

//    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "repositoryBaseClass")
//    Class<?> repositoryBaseClass() default JpaSpecificationExecutorWithProjectionImpl.class;

}
