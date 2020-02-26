package cn.procsl.ping.boot.rest.annotation;

import java.lang.annotation.*;

/**
 * 加入版本管理
 *
 * @author procsl
 * @date 2020/02/21
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiVersion {
    int value() default 1;
}
