package cn.procsl.ping.boot.jpa.support.query;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * select 子句字段
 */
@Documented
@Target({TYPE})
@Retention(value = RUNTIME)
public @interface Projection {

    /**
     * 查询项
     */
    Item[] value();

    /**
     * 返回类型
     */
    Class<? extends Serializable> returnType() default Serializable.class;

    /**
     * 字段注入方式
     */
    InjectType injectType() default InjectType.setter;

    enum InjectType {
        constructor,
        setter,
        field
    }

    @Documented
    @Target({ANNOTATION_TYPE})
    @Retention(value = RUNTIME)
    @interface Item {
        String value();

        String alias();
    }

}
