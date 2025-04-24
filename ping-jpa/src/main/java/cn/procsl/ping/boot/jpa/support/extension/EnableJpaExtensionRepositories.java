package cn.procsl.ping.boot.jpa.support.extension;

import cn.procsl.ping.boot.jpa.support.query.ProjectionSearchRepository;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableJpaRepositories(bootstrapMode = BootstrapMode.LAZY,
    repositoryBaseClass = JpaExtensionRepositoryImpl.class)
public @interface EnableJpaExtensionRepositories {

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "basePackages")
    String[] basePackages() default {};

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "basePackageClasses")
    Class<?>[] basePackageClasses() default {};


}
