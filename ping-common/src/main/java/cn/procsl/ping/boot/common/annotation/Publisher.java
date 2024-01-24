package cn.procsl.ping.boot.common.annotation;

import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtensionProperty(name = "X-Event-Publisher", value = "")
public @interface Publisher {

    String eventName() default "";

    String parameter() default "";

    Trigger trigger() default Trigger.complete;

    enum Trigger {
        always, complete, error
    }

}