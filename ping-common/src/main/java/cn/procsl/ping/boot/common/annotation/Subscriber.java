package cn.procsl.ping.boot.common.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Repeatable(value = Subscribers.class)
public @interface Subscriber {

    String name();

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface EventId {

    }

}
