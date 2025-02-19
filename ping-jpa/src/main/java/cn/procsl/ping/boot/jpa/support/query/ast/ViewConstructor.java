package cn.procsl.ping.boot.jpa.support.query.ast;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({CONSTRUCTOR})
@Retention(value = RUNTIME)
public @interface ViewConstructor {
    String[] names() default "";
}
