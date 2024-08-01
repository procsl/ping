package cn.procsl.ping.boot.jpa.support.query;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.QueryAnnotation;
import org.springframework.data.jpa.repository.Query;

import java.lang.annotation.*;

@Query
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@QueryAnnotation
@Documented
public @interface QueryForProjection {

    @AliasFor(value = "value", annotation = Query.class)
    String value() default "";

}
