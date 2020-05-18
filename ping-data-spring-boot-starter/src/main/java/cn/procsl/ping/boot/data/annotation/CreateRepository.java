package cn.procsl.ping.boot.data.annotation;

import org.springframework.data.repository.Repository;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * jpa repository 生成器
 *
 * @author procsl
 * @date 2020/05/18
 */
@Documented
@Target(TYPE)
@Retention(SOURCE)
public @interface CreateRepository {
    Class[] repositories() default {Repository.class};
}
