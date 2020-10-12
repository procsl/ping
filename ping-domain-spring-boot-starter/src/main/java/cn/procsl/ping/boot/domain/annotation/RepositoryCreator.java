package cn.procsl.ping.boot.domain.annotation;

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
     * 动态包名, repository与当前包的相对位置
     * 例如 当前包名为 cn.procsl.ping.boot.business.model
     * 若设置值为: 5
     * 则包名为: cn.procsl.ping.boot.business.repository
     * <p>
     * 若设置值为: 4
     * 则包名为: cn.procsl.ping.boot.repository
     * 可以设置全局 配置
     *
     * @return 点分包名位置
     */
    int indexOf() default -1;

    /**
     * 固定包名, 若指定, 则直接使用此包名
     *
     * @return 默认固定的包名
     */
    String packageName() default "";

}
