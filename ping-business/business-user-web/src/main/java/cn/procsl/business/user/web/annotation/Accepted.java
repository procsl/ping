package cn.procsl.business.user.web.annotation;

import java.lang.annotation.*;

/**
 * 标识指定的请求成功之后返回的状态码
 * 本注解返回204
 *
 * @author procsl
 * @date 2020/01/03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Accepted {
}
