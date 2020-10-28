package cn.procsl.ping.boot.rest.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Ok(status = HttpStatus.CREATED, message = "创建成功")
public @interface Created {

    @AliasFor(annotation = Ok.class, attribute = "status")
    HttpStatus status() default HttpStatus.CREATED;

    @AliasFor(annotation = Ok.class, attribute = "message")
    String message() default "创建成功";
}
