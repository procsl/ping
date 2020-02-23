package cn.procsl.ping.boot.rest.annotation;

import java.lang.annotation.*;

/**
 * 表示已经成功创建资源,并且返回新资源的表示
 * 如果配置了资源的位置,将会使用返回值替换占位符
 * 将会返回201
 * location:https://api.procsl.cn/product/xxxx
 *
 * @author procsl
 * @date 2020/01/03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Created {

    /**
     * https://api.procsl.cn/product/{id}
     *
     * @return
     */
    String location() default "";

    /**
     * 占位符的名称
     *
     * @return
     */
    String name() default "id";

}
