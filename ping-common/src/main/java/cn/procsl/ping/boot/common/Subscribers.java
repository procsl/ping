package cn.procsl.ping.boot.common;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface Subscribers {

    Subscriber[] value();

}
