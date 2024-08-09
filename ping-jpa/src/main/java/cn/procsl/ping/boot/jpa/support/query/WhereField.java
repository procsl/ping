package cn.procsl.ping.boot.jpa.support.query;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({TYPE, FIELD})
@Retention(value = RUNTIME)
@Repeatable(value = WhereField.WhereFields.class)
public @interface WhereField {
    /**
     * 查询条件组, 例如 (field1=XX or field2=YY) and (field3=XX or field4=YY)
     * 其中 (field1=XX or field2=YY) 为一组
     * (field3=XX or field4=YY) 为另一组
     * 只需要该字段值相同, 则为视为同一组, 同一组内的条件用 or 连接, 不同组的 条件用 and 连接
     */
    String groupName() default "";

    /**
     * 条件连接符
     */
    String condition() default "=";

    /**
     * 是否强制作为条件查询, 该参数必须传递
     */
    boolean required() default false;

    @Documented
    @Target(TYPE)
    @Retention(value = RUNTIME)
    @interface WhereFields {

        WhereField[] value();

    }

}
