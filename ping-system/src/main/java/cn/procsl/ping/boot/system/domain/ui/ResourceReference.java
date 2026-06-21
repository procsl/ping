package cn.procsl.ping.boot.system.domain.ui;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResourceReference {

    String name() default "";

}
