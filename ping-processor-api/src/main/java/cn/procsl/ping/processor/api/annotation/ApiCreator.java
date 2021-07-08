package cn.procsl.ping.processor.api.annotation;


import java.lang.annotation.*;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface ApiCreator {

    String path();

    String httpMethod();

    String successResponseCode() default "auto";

    String[] failureResponseCode() default "auto";
}
