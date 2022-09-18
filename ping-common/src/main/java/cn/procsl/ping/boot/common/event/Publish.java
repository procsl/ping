package cn.procsl.ping.boot.common.event;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Publish {

    String name();

    String parameter() default "";

    Trigger trigger() default Trigger.complete;

    enum Trigger {
        always, complete, error
    }

}
