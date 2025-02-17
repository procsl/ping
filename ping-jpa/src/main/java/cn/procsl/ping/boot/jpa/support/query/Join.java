package cn.procsl.ping.boot.jpa.support.query;

import jakarta.persistence.criteria.JoinType;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 不同的位置可能不等价
 */
@Documented
@Target({TYPE, FIELD})
@Retention(value = RUNTIME)
@Repeatable(Join.Joins.class)
public @interface Join {


    String leftJoinField() default "id";

    String rightJoinField() default "id";

    JoinType type() default JoinType.INNER;

    String ref() default "";

    Projection join();


    @Documented
    @Target(TYPE)
    @Retention(value = RUNTIME)
    @interface Joins {

        Join[] value();

    }

}
