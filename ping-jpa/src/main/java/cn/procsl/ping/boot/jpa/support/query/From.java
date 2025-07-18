package cn.procsl.ping.boot.jpa.support.query;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标注映射实体
 */
@Documented
@Target(TYPE)
@Retention(value = RUNTIME)
public @interface From {

    /**
     * 对应的目标的实体
     */
    Class<?> entity() default Object.class;

    /**
     * 实体别名
     */
    String alias() default "";


}
