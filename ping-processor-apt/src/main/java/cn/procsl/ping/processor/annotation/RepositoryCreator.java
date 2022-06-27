package cn.procsl.ping.processor.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * jpa repository 生成器
 *
 * @author procsl
 * @date 2020/05/18
 */
@Documented
@Target(TYPE)
@Retention(SOURCE)
public @interface RepositoryCreator {

    /**
     * 可以手动指定当前实体生成的 builder
     * 如果未手动指定,则使用全局的builder
     *
     * @return 代码生成器实现
     */
    String[] builders() default {};

    /**
     * 指定的命名策略
     *
     * @return 返回指定的命名策略
     */
    String strategy() default "cn.procsl.ping.processor.repository.DefaultNamingStrategy";

}
