package cn.procsl.ping.boot.jpa.support.query;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({FIELD})
@Retention(value = RUNTIME)
public @interface ReferenceBy {

    /**
     * 引用的别名
     * 需要在类上标注类型
     */
    String ref();


    /**
     * 引用的字段名, 如果为 空白字符串, 则取被标注的字段
     */
    String target() default "";


}
