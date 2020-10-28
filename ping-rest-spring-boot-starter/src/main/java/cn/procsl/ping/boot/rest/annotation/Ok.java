package cn.procsl.ping.boot.rest.annotation;

import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

/**
 * 将会返回201
 * location:https://api.procsl.cn/product/xxxx
 *
 * @author procsl
 * @date 2020/01/03
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ok {

    HttpStatus status() default HttpStatus.OK;

    String message() default "ok";
}
