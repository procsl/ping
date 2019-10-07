package cn.procsl.ping.web.http.annotation;

import java.lang.annotation.*;

/**
 * @author procsl
 * @date 2019/10/07
 */
@Target(ElementType.METHOD)
@Documented
@Inherited
@Retention(RetentionPolicy.SOURCE)
public @interface Get {

    String path() default "/";
}
