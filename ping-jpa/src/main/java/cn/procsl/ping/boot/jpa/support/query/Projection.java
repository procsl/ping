package cn.procsl.ping.boot.jpa.support.query;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(TYPE)
@Retention(value = RUNTIME)
@Repeatable(Projection.Projections.class)
public @interface Projection {

    Class<?> entity() default Object.class;

    String alias() default "";

    @Documented
    @Target(TYPE)
    @Retention(value = RUNTIME)
    @interface Projections {

        Projection[] value();

    }
}
