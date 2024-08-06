package cn.procsl.ping.boot.jpa.support.query;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({TYPE, CONSTRUCTOR})
@Retention(value = RUNTIME)
public @interface SelectField {

    String[] fields() default {};

}
