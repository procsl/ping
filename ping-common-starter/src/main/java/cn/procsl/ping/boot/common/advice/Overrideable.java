package cn.procsl.ping.boot.common.advice;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Overrideable {

    Class<?> target();

}
