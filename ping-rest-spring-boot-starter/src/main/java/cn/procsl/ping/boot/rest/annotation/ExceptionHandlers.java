package cn.procsl.ping.boot.rest.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionHandlers {

    ExceptionHandler[] value();

}
