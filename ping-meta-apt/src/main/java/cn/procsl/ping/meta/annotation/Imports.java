package cn.procsl.ping.meta.annotation;

/**
 * 导入
 *
 * @author procsl
 * @date 2020/05/17
 */
public @interface Imports {

    String className() default "";

    Class clazz() default Void.class;
}
