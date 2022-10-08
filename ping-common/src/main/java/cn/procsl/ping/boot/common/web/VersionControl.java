package cn.procsl.ping.boot.common.web;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VersionControl {

    String version() default "v1";

}
