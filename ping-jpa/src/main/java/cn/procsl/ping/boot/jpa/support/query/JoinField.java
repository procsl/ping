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
@Repeatable(JoinField.Joins.class)
public @interface JoinField {


    String leftJoinField() default "id";

    String rightJoinField() default "id";

    JoinType joinType() default JoinType.INNER;

    String fromAlias() default "";

    Projection to();


    @Documented
    @Target(TYPE)
    @Retention(value = RUNTIME)
    @interface Joins {

        JoinField[] value();

    }

}
