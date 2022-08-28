package cn.procsl.ping.boot.common.error;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionResolver {

    String code() default "001";

    String message() default "server error!";

    Class<? extends Exception> matcher() default Exception.class;

}
