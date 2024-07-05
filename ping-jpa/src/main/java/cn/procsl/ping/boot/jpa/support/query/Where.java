package cn.procsl.ping.boot.jpa.support.query;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({TYPE, FIELD})
@Retention(value = RUNTIME)
public @interface Where {

    /**
     * 大于, 小于, 等于, like等等
     */
    WherePredicate predicate() default WherePredicate.equal;

    /**
     * 标识该字段是否仅用于查询
     */
    boolean only() default false;

    /**
     * 查询条件组, 例如 (field1=XX or field2=YY) and (field3=XX or field4=YY)
     * 其中 (field1=XX or field2=YY) 为一组
     * (field3=XX or field4=YY) 为另一组
     * 只需要该字段值相同, 则为视为同一组, 同一组内的条件用 or 连接, 不同组的 条件用 and 连接
     */
    String groupName() default "";

    /**
     * 是否强制作为条件查询, 默认下,为null时或String类型为null或空白字符串时不作为查询条件
     */
    boolean isForce() default false;

    enum WherePredicate {
        equal, like
    }
}
