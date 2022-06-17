package cn.procsl.ping.boot.domain.advice;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Overrideable {

    Class<?> target();

}
