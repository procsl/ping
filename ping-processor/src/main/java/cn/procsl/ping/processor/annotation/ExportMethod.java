package cn.procsl.ping.processor.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * 导出接口
 */
@Documented
@Target(METHOD)
@Retention(SOURCE)
public @interface ExportMethod {

    /**
     * @return 接口路径
     */
    String path() default "";

    /**
     * @return http方法
     */
    String httpMethod() default "GET";
}
