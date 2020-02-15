package cn.procsl.ping.web.annotation;

import java.lang.annotation.*;

/**
 * 当无返回值是使用此注解
 *
 * @author procsl
 * @date 2020/01/03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoContent {
}
