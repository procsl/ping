package cn.procsl.ping.boot.jpa.support.query;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * select 子句字段
 */
@Documented
@Target({FIELD, METHOD})
@Retention(value = RUNTIME)
public @interface SelectField {
    /**
     * 如果被标注为true, 一定会出现在select语句中
     */
    boolean required() default false;

    /**
     * 设置当前字段别名, 如果为空, 则使用被标注的字段字段名
     */
    String alias() default "";

    /**
     * 自定义查询表达式,需要符合语法
     */
    String expression() default "";

}
