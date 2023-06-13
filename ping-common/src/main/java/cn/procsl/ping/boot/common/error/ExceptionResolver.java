package cn.procsl.ping.boot.common.error;

import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionResolver {

    HttpStatus status() default HttpStatus.INTERNAL_SERVER_ERROR;

    String code() default "SERVER_ERROR";

    String message() default "server error!";

    Class<? extends Exception> matcher() default Exception.class;

}
