package cn.procsl.ping.boot.jpa.support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * jpa repository 生成器
 *
 * @author procsl
 * &#064;date  2020/05/18
 */
@Documented
@Target(TYPE)
@Retention(value = RUNTIME)
@Repeatable(value = RepositoryCreators.class)
public @interface RepositoryCreator {

    Class<?>[] repositories() default {JpaRepository.class, JpaSpecificationExecutor.class};

    String repositoryName() default "";

    String packageName() default "";
}
