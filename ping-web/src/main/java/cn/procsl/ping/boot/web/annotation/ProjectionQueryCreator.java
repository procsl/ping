package cn.procsl.ping.boot.web.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProjectionQueryCreator {

    String uri() default "";

    String tag() default "";

    String desc() default "";

    boolean isPage() default true;

}
