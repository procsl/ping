package cn.procsl.ping.boot.common.error;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionResolver {

    String code() default "001";

    String message() default "server error!";

    Class<? extends Exception> matcher() default Exception.class;

}
