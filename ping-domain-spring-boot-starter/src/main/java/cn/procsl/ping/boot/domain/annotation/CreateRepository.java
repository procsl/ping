package cn.procsl.ping.boot.domain.annotation;

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

    /**
     * 可以手动指定当前实体生成的 builder
     * 如果未手动指定,则使用全局的builder
     *
     * @return
     */
    String[] builders() default {};

}
