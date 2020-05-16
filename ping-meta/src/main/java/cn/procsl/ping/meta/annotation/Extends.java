package cn.procsl.ping.meta.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * 扩展标识注解
 * @author procsl
 * @date 2020/05/16
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD,METHOD,TYPE,})
public @interface Extends {

}
