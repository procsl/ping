package cn.procsl.ping.boot.common.event;

import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtensionProperty(name = "X-Event-Publisher", value = "")
public @interface Publisher {

    String name() default "";

    String parameter() default "";

    Trigger trigger() default Trigger.complete;

    enum Trigger {
        always, complete, error
    }

}
