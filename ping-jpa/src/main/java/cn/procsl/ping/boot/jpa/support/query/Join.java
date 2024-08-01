package cn.procsl.ping.boot.jpa.support.query;

import jakarta.persistence.criteria.JoinType;

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
@Repeatable(Join.Joins.class)
public @interface Join {


    String leftJoinField() default "id";

    String rightJoinField() default "id";

    /**
     * 连接类型
     */
    JoinType joinType() default JoinType.INNER;

    String left();

    String right();


    @Documented
    @Target(TYPE)
    @Retention(value = RUNTIME)
    @interface Joins {

        Join[] value();

    }

}
