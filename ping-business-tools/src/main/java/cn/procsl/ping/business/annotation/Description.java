package cn.procsl.ping.business.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 字段, 接口 描述注解
 * <p>
 * 可用于描述性元数据生成
 *
 * @author procsl
 * @date 2020/04/23
 */
@Target({METHOD, FIELD, TYPE})
@Retention(RUNTIME)
public @interface Description {
    String comment() default "";
}
