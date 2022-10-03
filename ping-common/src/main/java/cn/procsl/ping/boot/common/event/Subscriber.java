package cn.procsl.ping.boot.common.event;

import org.springframework.lang.NonNull;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Repeatable(value = Subscribers.class)
public @interface Subscriber {

    @NonNull
    String name();

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface EventId {

    }

}
