package cn.procsl.ping.boot.jpa.support;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableJpaRepositories(bootstrapMode = BootstrapMode.LAZY, repositoryBaseClass = JpaSpecificationExecutorWithProjectionImpl.class)
public @interface EnableJpaExtendRepositories {

    @AliasFor(annotation = EnableJpaRepositories.class, attribute = "basePackages")
    String[] basePackages() default {};

}
