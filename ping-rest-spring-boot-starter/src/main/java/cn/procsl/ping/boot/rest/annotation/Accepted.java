package cn.procsl.ping.boot.rest.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

/**
 * 标识指定的请求成功之后返回的状态码
 * 本注解返回202
 *
 * @author procsl
 * @date 2020/01/03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Ok(status = HttpStatus.ACCEPTED, message = "请求已被接受")
public @interface Accepted {

    @AliasFor(annotation = Ok.class, attribute = "status")
    HttpStatus status() default HttpStatus.ACCEPTED;

    @AliasFor(annotation = Ok.class, attribute = "message")
    String message() default "请求已被接受";

}
