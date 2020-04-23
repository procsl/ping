package cn.procsl.ping.boot.data.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author procsl
 * @date 2020/04/23
 */
@Target({METHOD, FIELD, TYPE})
@Retention(RUNTIME)
public @interface Description {
    String comment() default "";
}
