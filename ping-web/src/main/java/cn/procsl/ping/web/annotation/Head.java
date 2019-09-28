package cn.procsl.ping.web.annotation;

import java.lang.annotation.*;

/**
 * @author procsl
 * @date 2019/09/29
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Inherited
@Documented
public @interface Head {

    /**
     * http请求路径
     *
     * @return
     */
    String value() default "/";
}
