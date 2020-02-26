package cn.procsl.ping.boot.rest.annotation;

import java.lang.annotation.*;

/**
 * 移除版本管理
 *
 * @author procsl
 * @date 2020/02/21
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotApiVersion {
}
