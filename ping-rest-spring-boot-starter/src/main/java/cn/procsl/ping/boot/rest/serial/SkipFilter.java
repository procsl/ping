package cn.procsl.ping.boot.rest.serial;

import java.lang.annotation.*;

/**
 * @author procsl
 * @date 2020/02/06
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipFilter {
}
