package cn.procsl.ping.boot.rest.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

/**
 * 当无返回值是使用此注解
 * 会创建204状态码
 *
 * @author procsl
 * @date 2020/01/03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Ok(status = HttpStatus.NO_CONTENT, message = "请求成功")
public @interface NoContent {
    @AliasFor(annotation = Ok.class, attribute = "status")
    HttpStatus status() default HttpStatus.NO_CONTENT;

    @AliasFor(annotation = Ok.class, attribute = "message")
    String message() default "请求成功";
}
