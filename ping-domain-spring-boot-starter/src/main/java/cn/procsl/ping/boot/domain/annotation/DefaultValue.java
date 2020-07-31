package cn.procsl.ping.boot.domain.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author procsl
 * @date 2020/05/07
 */

@Target({METHOD, FIELD, TYPE})
@Retention(RUNTIME)
public @interface DefaultValue {
    String value() default "";

    String forFiled() default "";
}
