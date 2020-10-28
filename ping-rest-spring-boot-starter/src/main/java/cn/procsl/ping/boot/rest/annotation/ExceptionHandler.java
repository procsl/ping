package cn.procsl.ping.boot.rest.annotation;

import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(value = ExceptionHandlers.class)
public @interface ExceptionHandler {

    String message() default "";

    Class<? extends Exception>[] exceptions() default Exception.class;

    HttpStatus status();

    String code();

    MatcherType type() default MatcherType.equals;

    enum MatcherType {
        equals,
        extension
    }

}
