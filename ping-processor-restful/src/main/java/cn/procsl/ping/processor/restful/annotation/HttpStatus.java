package cn.procsl.ping.processor.restful.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface HttpStatus {

    /**
     * http状态码
     *
     * @return 默认返回200, 可自定义
     */
    int code() default 200;

    /**
     * http状态码对应的message
     *
     * @return 如果不填, 则根据状态码转换
     */
    String message() default "ok";
}
